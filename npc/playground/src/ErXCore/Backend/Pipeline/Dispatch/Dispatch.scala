package ErXCore

import chisel3._
import chisel3.util._
import chisel3.util.experimental.BoringUtils
import ErXCore.DecodeSignal.FU_MEM

class Dispatch extends ErXCoreModule {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(Vec(DecodeWidth,new RenameIO)))
    val to_ex = Vec(IssueWidth,Decoupled(new RenameIO))
  })

  val uopInt = Wire(Vec(DecodeWidth,new RenameIO))
  val uopIntValid = WireDefault(Vec(DecodeWidth,false.B))
  val uopMem = Wire(Vec(DecodeWidth,new RenameIO))
  val uopMemValid = WireDefault(Vec(DecodeWidth,false.B))

  uopInt := io.in.bits
  uopMem := io.in.bits
  for(i <- 0 until DecodeWidth){
    when(uopInt(i).cs.fuType =/= SDEF(FU_MEM)){
      uopIntValid(i) := io.in.valid
    }
    when(uopMem(i).cs.fuType === SDEF(FU_MEM)){
      uopMemValid(i) := io.in.valid
    }
  }
  val memRS = Module(new RS(rsSize = 2,deqWidth = DecodeWidth))
  val intRS = Module(new RS(rsSize = 2,deqWidth = DecodeWidth))
  io.in.ready := intRS.io.in.map(_.ready).reduce(_&&_) & memRS.io.in.map(_.ready).reduce(_&&_)
  intRS.io.in := uopInt
  memRS.io.in := uopMem
  for(i <- 0 until DecodeWidth){
    intRS.io.in(i).valid := uopIntValid(i)
    memRS.io.in(i).valid := uopMemValid(i)
  }

  // memRS.io.in := uopMem
  // intRS.io.in(i).valid  := uopIntValid
  for(i <- 0 until IssueWidth){
    if(i < 2){
      io.to_ex(i).bits <> intRS.io.out(i)
    }else{
      io.to_ex(i).bits <> memRS.io.out(i - 2)
    }
  }
}
