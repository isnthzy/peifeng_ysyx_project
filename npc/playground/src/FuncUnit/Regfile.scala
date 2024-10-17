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
  val rf=RegInit(Vec(Seq.fill(32)(0.U(32.W))))
  when(io.wen){ 
    when(io.waddr=/=0.U){
      rf(io.waddr):=io.wdata 
    }.otherwise{
      rf(0):=0.U
    }
  }
  io.rdata1:=rf(io.raddr1)
  io.rdata2:=rf(io.raddr2)

  io.diffREG:=rf
} 
