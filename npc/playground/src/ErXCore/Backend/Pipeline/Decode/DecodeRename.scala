package ErXCore

import chisel3._
import chisel3.util._


class DecodeRename extends ErXCoreModule{
  val io = IO(new Bundle {
    val in = Vec(DecodeWidth,Flipped(Decoupled(new InstIO)))
    val from_ex = Input(new RenameFromExecuteUpdate(updSize = IssueWidth))
    val from_rob = Input(new RenameFromCommitUpdate(updSize = CommitWidth))
    val fw_ex   = Output(new RSFromRename)
    val to_dp   = Vec(DecodeWidth,Decoupled(new RenameIO))
  })

  val DecodeSignal = Array.fill(DecodeWidth)(Module(new DecodeSignals).io)
  val ImmGen = Array.fill(DecodeWidth)(Module(new ImmGen).io)
  val uop = Wire(Vec(DecodeWidth, new MicroOpIO))
  for(i <- 0 until DecodeWidth){
    //NOTE:历史遗留，需要优化
    DecodeSignal(i).inst := io.in(i).bits.inst
    ImmGen(i).inst       := io.in(i).bits.inst
    ImmGen(i).sel        := DecodeSignal(i).immType

    uop(i).cs.src1Type := DecodeSignal(i).aSel
    uop(i).cs.src2Type := DecodeSignal(i).bSel
    uop(i).cs.fuType   := DecodeSignal(i).fuSel
    uop(i).cs.aluOp    := DecodeSignal(i).aluOp
    uop(i).cs.brType   := DecodeSignal(i).brType
    uop(i).cs.lsType   := DecodeSignal(i).lsType
    uop(i).cs.csrOp    := DecodeSignal(i).csrOp
    uop(i).cs.rfWen    := DecodeSignal(i).rfWen
    uop(i).cs.rfSrc1   := io.in(i).bits.inst(19, 15)
    uop(i).cs.rfSrc2   := io.in(i).bits.inst(24, 20)
    uop(i).cs.rfDest   := io.in(i).bits.inst(11, 7)
    uop(i).cf.pc       := io.in(i).bits.pc
    uop(i).cf.imm      := ImmGen(i).out
  }
  val Rename = Module(new Rename)
  Rename.io.in   := uop
  Rename.io.from_ex := io.from_ex
  Rename.io.from_rob := io.from_rob
  (0 until DecodeWidth).foreach(i => {io.to_dp(i).bits := Rename.io.out(i)})
  io.fw_ex := Rename.io.fw_ex

}


