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
  io.rdata1:=rf(io.raddr1)
  io.rdata2:=rf(io.raddr2)
}