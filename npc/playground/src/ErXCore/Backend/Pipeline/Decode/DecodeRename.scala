package ErXCore

import chisel3._
import chisel3.util._


class DecodeRename extends ErXCoreModule{
  val io = IO(new Bundle {
    val in = Vec(DecodeWidth,Flipped(Decoupled(new InstIO)))
    val from_ex = Input(new RenameFromExecuteUpdate(updSize = IssueWidth))
    val from_rob = Input(new RenameFromCommitUpdate(updSize = CommitWidth))
    val fw_dp   = Output(new RSFromRename)
    val to_dp   = Vec(DecodeWidth,Decoupled(new RenameIO))
  })
  // val drValid = RegInit(VecInit(Seq.fill(IssueWidth)(false.B)))
  // for(i <- 0 until DecodeWidth){
  //   when(io.in(i).ready){
  //     drValid(i) := io.in(i).valid
  //   }
  //   io.in(i).ready := ~drValid(i) || io.to_dp(i).ready
  //   io.to_dp(i).valid := drValid(i)
  // }
  val renameValid = Wire(Vec(DecodeWidth, Bool()))
  for(i <- 0 until DecodeWidth){
    io.in(i).ready := io.to_dp(i).ready
    io.to_dp(i).valid := io.in(i).valid
    renameValid(i) := io.to_dp(i).fire
  }

  val DecodeSignal = Array.fill(DecodeWidth)(Module(new DecodeSignals).io)
  val ImmGen = Array.fill(DecodeWidth)(Module(new ImmGen).io)
  val uop = Wire(Vec(DecodeWidth, new MicroOpIO))

  for(i <- 0 until DecodeWidth){
    //NOTE:历史遗留，需要优化
    val rfSrc1 = if(UseRV32E) io.in(i).bits.inst(19 - 1, 15) else io.in(i).bits.inst(19, 15)
    val rfSrc2 = if(UseRV32E) io.in(i).bits.inst(24 - 1, 20) else io.in(i).bits.inst(24, 20)
    val rfDest = if(UseRV32E) io.in(i).bits.inst(11 - 1, 7) else io.in(i).bits.inst(11, 7)
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
    uop(i).cs.rfWen    := DecodeSignal(i).rfWen.asBool && renameValid(i)
    uop(i).cs.rfSrc1   := rfSrc1
    uop(i).cs.rfSrc2   := rfSrc2
    uop(i).cs.rfDest   := rfDest
    uop(i).cf.pc       := io.in(i).bits.pc
    uop(i).cf.imm      := ImmGen(i).out
    uop(i).cf.inst     := io.in(i).bits.inst
  }
  val Rename = Module(new Rename)
  Rename.io.in   := uop
  Rename.io.from_ex := io.from_ex
  Rename.io.from_rob := io.from_rob
  (0 until DecodeWidth).foreach(i => {io.to_dp(i).bits := Rename.io.out(i)})
  io.fw_dp := Rename.io.fw_dp

}


