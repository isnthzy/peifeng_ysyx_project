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
  when(io.waddr===1.U){
    rf(1):=wdata 
  }
  when(io.waddr===2.U){
    rf(3):=wdata 
  }
  when(io.wen){ 
    // rf(io.waddr):=wdata 
    printf("waddr= %x wdata= %x\n",io.waddr,wdata)
    printf("rf_1 %x ,rf_2 %x\n",rf(1),rf(2))
  }
  
  io.rdata1:=Mux(io.raddr1=/=0.U,rf(io.raddr1),0.U)
  io.rdata2:=Mux(io.raddr2=/=0.U,rf(io.raddr2),0.U)
}