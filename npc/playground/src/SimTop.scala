import chisel3._
import chisel3.util._  

class SimTop extends Module {
  val io = IO(new Bundle {
    val op = Input(UInt(3.W))
    val a  = Input(UInt(4.W))
    val b  = Input(UInt(4.W))
    val result = Output(UInt(4.W))
    val of = Output(Bool())
  })
  
  val op_add =io.a+io.b
  val overflow_add =(io.a(3)&&io.b(3))&&(op_add(3)=/=io.a(3))
  
  val tmp = (("b1111".U)^io.b)+1.U
  val op_sub =io.a+tmp
  val overflow_sub =(io.a(3)&&io.b(3))&&(op_sub(3)=/=io.a(3))
  
  val op_neg= ~io.a

  val op_and= io.a&io.b
  
  val op_or = io.a|io.b
  
  val op_xor= io.a^io.b
  
  val op_tha= Mux(io.a<io.b, 1.U, 0.U)
  
  val op_eq = Mux(io.a===io.b,1.U,0.U)
  io.result := MuxLookup(io.op, 0.U)(Seq(
    0.U -> op_add, 1.U -> op_sub, 
    2.U -> op_neg, 3.U -> op_and,
    4.U -> op_or , 5.U -> op_xor,
    6.U -> op_tha, 7.U -> op_eq
  ))
  io.of := MuxLookup(io.op, 0.U)(Seq(
    0.U -> overflow_add, 1.U -> overflow_sub, 
  ))
}
