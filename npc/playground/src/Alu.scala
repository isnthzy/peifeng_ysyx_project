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
  sll := io.src1 << io.src2(4,0) //左移

  val alu_add= io.src1 + io.src2

  val alu_sub= io.src1 - io.src2 

  val alu_and = io.src1 & io.src2

  val alu_or  = io.src1 | io.src2

  val alu_xor = io.src1 ^ io.src2

  val alu_sll = sll(31,0)

  val alu_sra = (io.src1.asSInt >> io.src2(4,0).asUInt).asUInt

  val alu_srl = (io.src1        >> io.src2(4,0)       ).asUInt

  val alu_slt = (io.src1.asSInt < io.src2.asSInt).asUInt
  
  val alu_sltu= (io.src1.asUInt < io.src2.asUInt).asUInt

  val alu_lui = Cat(io.src2(31,12),0.U(12.W))
  io.result := MuxLookup(io.op, 0.U)(Seq(
    ALU_ADD -> alu_add, 
    ALU_SUB -> alu_sub,
    ALU_AND -> alu_and,
    ALU_OR  -> alu_or,
    ALU_XOR -> alu_xor,
    ALU_SLL -> alu_sll, 
    ALU_SRL -> alu_srl,
    ALU_SRA -> alu_sra,
    ALU_SLT -> alu_slt,
    ALU_SLTU-> alu_sltu,
    ALU_COPY_B -> io.src2,
    ALU_LUI -> alu_lui
  ))
}