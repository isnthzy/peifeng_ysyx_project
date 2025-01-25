package ErXCore

import chisel3._
import chisel3.util._


class Decode extends ErXCoreModule{
  val io = IO(new Bundle {
    val in  = Vec(DecodeWidth,Flipped(Decoupled(new InstIO)))
    val out = Vec(DecodeWidth,Decoupled(new MicroOpIO))
  })
  for(i <- 0 until DecodeWidth){
    io.in(i).ready <> io.out(i).ready
    io.in(i).valid <> io.out(i).valid
  }

  val DecodeSignal = Array.fill(DecodeWidth)(Module(new DecodeSignals).io)
  val ImmGen = Array.fill(DecodeWidth)(Module(new ImmGen).io)
  for(i <- 0 until DecodeWidth){
    //NOTE:历史遗留，需要优化
    DecodeSignal(i).inst := io.in(i).bits.inst
    ImmGen(i).inst       := io.in(i).bits.inst
    ImmGen(i).sel        := DecodeSignal(i).immType

    io.out(i).bits.cs.src1Type := DecodeSignal(i).aSel
    io.out(i).bits.cs.src2Type := DecodeSignal(i).bSel
    io.out(i).bits.cs.fuType   := DecodeSignal(i).fuSel
    io.out(i).bits.cs.aluOp    := DecodeSignal(i).aluOp
    io.out(i).bits.cs.brType   := DecodeSignal(i).brType
    io.out(i).bits.cs.lsType   := DecodeSignal(i).lsType
    io.out(i).bits.cs.csrOp    := DecodeSignal(i).csrOp
    io.out(i).bits.cs.rfWen    := DecodeSignal(i).rfWen
    io.out(i).bits.cs.rfSrc1   := io.in(i).bits.inst(19, 15)
    io.out(i).bits.cs.rfSrc2   := io.in(i).bits.inst(24, 20)
    io.out(i).bits.cs.rfDest   := io.in(i).bits.inst(11, 7)

    io.out(i).bits.cf.pc       := io.in(i).bits.pc
    io.out(i).bits.cf.imm      := ImmGen(i).out
  }
}


