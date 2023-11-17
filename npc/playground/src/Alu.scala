import chisel3._
import chisel3.util._

class Alu extends Module {
  val io = IO(new Bundle {
    val op = Input(UInt(12.W))
    val src1 = Input(SInt(4.W))
    val src2 = Input(SInt(4.W))
    val result = Output(SInt(4.W))
  })
  val op_add= io.src1 + io.src2
  val op_sub= io.src1 - io.src2
  val op_neg= ~io.src1
  val op_and= io.src1 & io.src2
  val op_or = io.src1 | io.src2
  val op_xor= io.src1 ^ io.src2
  val op_tha= Mux(io.src1  <  io.src2, 1.S, 0.S)
  val op_eq = Mux(io.src1 === io.src2, 1.S, 0.S)
  io.result := MuxLookup(io.op, 0.S)(Seq(
    "b000000000001".U -> op_add.asSInt, 
    "b000000000010".U -> op_sub.asSInt,
    "b000000000100".U -> op_neg,
    "b000000001000".U -> op_and,
    "b000000010000".U -> op_or,
    "b000000100000".U -> op_xor,
    "b000001000000".U -> op_tha, 
    "b000010000000".U -> op_eq,
  ))
}