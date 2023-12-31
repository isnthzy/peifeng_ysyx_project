import chisel3._
import chisel3.util._
import Alus._
class Alu extends Module {
  val io = IO(new Bundle {
    val op = Input(UInt(4.W))
    val src1 = Input(UInt(32.W))
    val src2 = Input(UInt(32.W))
    val result = Output(UInt(32.W))
  })
  

  val sll=Wire(UInt(64.W))
  sll := io.src1 << io.src2(5,0) //左移

  val alu_add= io.src1 + io.src2

  val alu_sub= io.src1 - io.src2 

  val alu_neg = ~io.src1

  val alu_and = io.src1 & io.src2

  val alu_or  = io.src1 | io.src2

  val alu_xor = io.src1 ^ io.src2

  val alu_sll = sll(31,0)

  val alu_sra = io.src1.asSInt >> io.src2(5,0)

  val alu_srl = io.src1 >> io.src2(5,0)

  io.result := MuxLookup(io.op, 0.U)(Seq(
    ALU_ADD -> alu_add, 
    ALU_SUB -> alu_sub,
    ALU_AND -> alu_and,
    ALU_OR  -> alu_or,
    ALU_XOR -> alu_xor,
    "b000010000000".U -> alu_sll, 
    "b000100000000".U -> alu_sra.asUInt,
    "b001000000000".U -> alu_srl,
  ))
}