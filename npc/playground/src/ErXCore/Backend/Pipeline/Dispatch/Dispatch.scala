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
  io.fw_rob.zipWithIndex.foreach{case (rob, i) => 
    rob.bits := io.in(i).bits 
    rob.valid := io.in(i).valid
  }
  io.in.zipWithIndex.foreach{case (in, i) => 
    in.ready := io.fw_rob.map(_.ready).reduce(_&&_) & intRS.io.in.map(_.ready).reduce(_&&_) & memRS.io.in.map(_.ready).reduce(_&&_)
  }

  val uopInt = VecInit(io.in.map(_.bits))
  val uopIntValid = WireDefault(Vec(DecodeWidth,false.B))
  val uopMem = VecInit(io.in.map(_.bits))
  val uopMemValid = WireDefault(Vec(DecodeWidth,false.B))

  for(i <- 0 until DecodeWidth){
    when(uopInt(i).cs.fuType =/= SDEF(FU_MEM)){
      uopIntValid(i) := io.in(i).valid
    }
    when(uopMem(i).cs.fuType === SDEF(FU_MEM)){
      uopMemValid(i) := io.in(i).valid
    }
  }
  val memRS = Module(new RS(rsSize = 2,deqWidth = DecodeWidth))
  val intRS = Module(new RS(rsSize = 2,deqWidth = DecodeWidth))

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
      io.to_pr(i).bits <> intRS.io.out(i)
    }else{
      io.to_pr(i).bits <> memRS.io.out(3)
    }
  }
}
