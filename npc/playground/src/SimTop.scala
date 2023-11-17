import chisel3._
import chisel3.util._  

class SimTop extends Module {
  val io = IO(new Bundle {
    val in=Input(UInt(4.W))
    val out=Output(UInt(4.W))
  })  
  val i1=Module(new Reg(1,1.U))
  i1.reg.din := io.in
  io.out := i1.reg.dout
  i1.reg.wen := io.out(0)
}

class Reg(val WIDTH:Int=1,val RESET_VAL:UInt=0.U) extends Module{
  val reg=IO(new Bundle {
    val din=Input(UInt(WIDTH.W))
    val dout=Output(UInt(WIDTH.W))
    val wen=Input(Bool())
  })
  val reg_dout=RegInit(RESET_VAL)
  when(reg.wen){reg_dout:=reg.din}
  reg.dout:=reg_dout
}

class RegFile extends Module{
  val io=IO(new Bundle {
    val wdata=Input(UInt(32.W))
    val waddr=Input(UInt(32.W))
    val wen=Input(Bool())
  })
  RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
}