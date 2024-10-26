package FuncUnit

import chisel3._
import chisel3.util._
import Util.{Sext,Zext,Mux1hDefMap}
import Control._

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

  val alu_eq  = io.src1===io.src2

  val alu_pc4 = io.src1+4.U

  val alu_lui = Cat(io.src2(31,12),0.U(12.W))

  dontTouch(alu_add)
  dontTouch(alu_sub)
  dontTouch(alu_and)
  dontTouch(alu_or)
  dontTouch(alu_xor)
  dontTouch(alu_sll)
  dontTouch(alu_srl)
  dontTouch(alu_sra)
  dontTouch(alu_slt)
  dontTouch(alu_sltu)
  dontTouch(alu_eq)
  dontTouch(alu_pc4)
  dontTouch(alu_lui)

  
  io.result := Mux1hDefMap(io.op,Map(
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
    ALU_LUI -> alu_lui,
    ALU_EQ  -> alu_eq,
    ALU_PC4 -> alu_pc4,
    ALU_COPY_B -> io.src2,
  ))
}