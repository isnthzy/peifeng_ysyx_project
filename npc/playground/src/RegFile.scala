import chisel3._
import chisel3.util._  

class RegFile extends Module{
  val io=IO(new Bundle {
    val waddr =Input(UInt(5.W))
    val wdata =Input(UInt(32.W))
    val raddr1=Input(UInt(5.W))
    val rdata1=Output(UInt(32.W))
    val raddr2=Input(UInt(5.W))
    val rdata2=Output(UInt(32.W))
    val wen=Input(Bool())
  })
  val rf=RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
  val wdata=Mux(io.waddr===0.U,0.U,io.wdata)
  when(io.wen){ rf(io.waddr):=wdata }
  io.rdata1:=Mux(io.raddr1=/=0.U,rf(io.raddr1),0.U)
  io.rdata2:=Mux(io.raddr2=/=0.U,rf(io.raddr2),0.U)
  val debug = Module(new debug())
  debug.io.clock:=clock
  debug.io.reset:=reset
  debug.io.debug_1:=wdata
  debug.io.debug_2:=io.waddr
} 

class debug extends BlackBox with HasBlackBoxPath {
  val io = IO(new Bundle {
    val clock       = Input(Clock())
    val reset       = Input(Bool())
    val debug_1     = Input(UInt(32.W))
    val debug_2     = Input(UInt(32.W))
  })
  addPath("playground/src/v_resource/dpi.sv")
}
