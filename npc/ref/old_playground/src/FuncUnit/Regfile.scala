package FuncUnit

import chisel3._
import chisel3.util._  
import CoreConfig.Configs._

class RegFile extends Module{
  val io=IO(new Bundle {
    val waddr =Input(UInt(5.W))
    val wdata =Input(UInt(DATA_WIDTH.W))
    val raddr1=Input(UInt(5.W))
    val rdata1=Output(UInt(DATA_WIDTH.W))
    val raddr2=Input(UInt(5.W))
    val rdata2=Output(UInt(DATA_WIDTH.W))
    val wen=Input(Bool())
    val diffREG=Output((Vec(32, UInt(32.W))))
  })
  val rf=RegInit(VecInit(Seq.fill(16)(0.U(32.W))))
  when(io.wen){ 
    when(io.waddr=/=0.U){
      rf(io.waddr(3,0)):=io.wdata 
    }.otherwise{
      rf(0):=0.U
    }
  }
  io.rdata1:=rf(io.raddr1(3,0))
  io.rdata2:=rf(io.raddr2(3,0))

  io.diffREG:=rf.asTypeOf(io.diffREG)
} 
