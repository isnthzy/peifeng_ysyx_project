package ErXCore

import chisel3._
import chisel3.util._
import chisel3.util.experimental.BoringUtils
import ErXCore.DecodeSignal.FU_MEM

class Dispatch extends ErXCoreModule {
  val io = IO(new Bundle {
    val in = Vec(DecodeWidth,Flipped(Decoupled(new RenameIO)))
    val from_rob = Input(new RSFromROB)
    val from_dr  = Input(new RSFromRename)
    val fw_rob = Vec(DecodeWidth,Decoupled(new RenameIO))
    val to_pr = Vec(IssueWidth,Decoupled(new RenameIO))
  })
  val memRS = Module(new RS(rsSize = 4,enqWidth = DecodeWidth,deqWidth = 1,StoreSeq = true))
  val intRS = Module(new RS(rsSize = 4,enqWidth = DecodeWidth,deqWidth = DecodeWidth))
  io.fw_rob.zipWithIndex.foreach{case (rob, i) => 
    rob.bits := io.in(i).bits 
    rob.valid := (intRS.io.in(i).fire || memRS.io.in(i).fire) & io.in(i).fire
  }

  io.in.zipWithIndex.foreach{case (in, i) => 
    var rsReady = (0 until DecodeWidth).map(j =>
        (intRS.io.in(j).ready & uopIsInt(j)) || (memRS.io.in(j).ready & uopIsMem(j))
    ).reduce(_ & _)
    in.ready := io.fw_rob.map(_.ready).reduce(_ & _) & rsReady
  } //不允许下标i任意握手，只允许同时握手，防止因为下标握手不同步导致进入rob顺序错误

  val enqRSValid = Wire(Vec(DecodeWidth,Bool()))
  enqRSValid.zipWithIndex.foreach{case (valid, i) => 
    if(i == 0){
      valid := io.in(i).fire & io.fw_rob(i).ready
    }else{
      valid := io.in(i).fire & io.in(i - 1).ready & io.fw_rob(i).ready
    }
  } //主机要同时和两个从机握手，如果rob不及时握手会导致robage等错误，因此需要重构握手逻辑，可以考虑用queue解决

  val uopInt = VecInit(io.in.map(_.bits))
  val uopIntValid = WireDefault(VecInit(Seq.fill(DecodeWidth)(false.B)))
  val uopMem = VecInit(io.in.map(_.bits))
  val uopMemValid = WireDefault(VecInit(Seq.fill(DecodeWidth)(false.B)))
  lazy val uopIsInt = Wire(Vec(DecodeWidth,Bool()))
  lazy val uopIsMem = Wire(Vec(DecodeWidth,Bool()))
  for(i <- 0 until DecodeWidth){
    uopIsInt(i) := uopInt(i).cs.fuType =/= SDEF(FU_MEM)
    uopIsMem(i) := uopInt(i).cs.fuType === SDEF(FU_MEM)
    when(uopIsInt(i)){
      uopIntValid(i) := enqRSValid(i)
    }
    when(uopIsMem(i)){
      uopMemValid(i) := enqRSValid(i)
    }
  }

  intRS.io.in.zipWithIndex.foreach{case (in, i) => 
    in.bits  := uopInt(i)
    in.valid := uopIntValid(i)}
  intRS.io.from_dr  := io.from_dr
  intRS.io.from_rob := io.from_rob
  memRS.io.in.zipWithIndex.foreach{case (in, i) => 
    in.bits  := uopMem(i)
    in.valid := uopMemValid(i)
  }
  memRS.io.from_dr  := io.from_dr
  memRS.io.from_rob := io.from_rob

  for(i <- 0 until IssueWidth){
    if(i < 2){
      io.to_pr(i) <> intRS.io.out(i)
    }else{
      io.to_pr(i) <> memRS.io.out(0)
    }
  }
}
