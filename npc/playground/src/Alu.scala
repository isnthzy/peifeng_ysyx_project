import chisel3._
import chisel3.util._

class Alu extends Module {
  val io = IO(new Bundle {
    val op = Input(UInt(12.W))
    val src1 = Input(UInt(32.W))
    val src2 = Input(UInt(32.W))
    val sign = Input(Bool()) //1为有符号数，0为无符号数
    val result = Output(UInt(32.W))
  })
  val alu_sadd= io.src1.asSInt + io.src2.asSInt
  val alu_ssub= io.src1.asSInt - io.src2.asSInt
  val alu_uadd= io.src1 + io.src2
  val alu_usub= io.src1 - io.src2 
  val alu_utha= Mux(io.src1 < io.src2,1.U,0.U)
  val alu_stha= Mux(io.src1.asSInt < io.src2.asSInt,1.U,0.U)

  val alu_add = Mux(io.sign,alu_sadd.asUInt,alu_uadd)

  val alu_sub = Mux(io.sign,alu_ssub.asUInt,alu_usub)

  val alu_neg = ~io.src1

  val alu_and = io.src1 & io.src2

  val alu_or  = io.src1 | io.src2

  val alu_xor = io.src1 ^ io.src2

  val alu_eq  = Mux(io.src1===io.src2,1.U,0.U)

  val alu_tha = Mux(io.sign,alu_stha.asUInt,alu_utha)

  val alu_sl  = io.src1 << io.src2 //左移

  val alu_sra = io.src1.asSInt >> io.src2

  val alu_srl = io.src1 >> io.src2

  io.result := MuxLookup(io.op, 0.U)(Seq(
    "b000000000001".U -> alu_add, 
    "b000000000010".U -> alu_sub,
    "b000000000100".U -> alu_neg,
    "b000000001000".U -> alu_and,
    "b000000010000".U -> alu_or,
    "b000000100000".U -> alu_xor,
    "b000001000000".U -> alu_eq,
    "b000010000000".U -> alu_tha,
    "b000100000000".U -> alu_sl, 
    "b001000000000".U -> alu_sra.asUInt,
    "b010000000000".U -> alu_srl,
  ))
}