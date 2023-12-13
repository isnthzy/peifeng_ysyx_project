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
  
  
  val alu_utha= Mux(io.src1 < io.src2,1.U,0.U)
  val alu_stha= Mux(io.src1.asSInt < io.src2.asSInt,1.U,0.U)

  val sll=Wire(UInt(64.W))
  sll := io.src1 << io.src2(5,0) //左移

  val alu_add= io.src1 + io.src2

  val alu_sub= io.src1 - io.src2 

  val alu_neg = ~io.src1

  val alu_and = io.src1 & io.src2

  val alu_or  = io.src1 | io.src2

  val alu_xor = io.src1 ^ io.src2

  val alu_eq  = Mux(io.src1===io.src2,1.U,0.U)

  val alu_tha = Mux(io.sign,alu_stha.asUInt,alu_utha)

  val alu_sll = sll(31,0)

  val alu_sra = io.src1.asSInt >> io.src2(5,0)

  val alu_srl = io.src1 >> io.src2(5,0)

  io.result := MuxLookup(io.op, 0.U)(Seq(
    "b000000000001".U -> alu_add, 
    "b000000000010".U -> alu_sub,
    "b000000000100".U -> alu_and,
    "b000000001000".U -> alu_or,
    "b000000010000".U -> alu_xor,
    "b000000100000".U -> alu_eq,
    "b000001000000".U -> alu_tha,
    "b000010000000".U -> alu_sll, 
    "b000100000000".U -> alu_sra.asUInt,
    "b001000000000".U -> alu_srl,
  ))
}