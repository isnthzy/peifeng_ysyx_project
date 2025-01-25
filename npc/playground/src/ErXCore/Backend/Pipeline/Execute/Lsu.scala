package ErXCore

import chisel3._
import chisel3.util._
import DecodeSignal._

class LSU extends ErXCoreModule{
  val io = IO(new Bundle{
    val valid  = Input(Bool())
    val busy   = Output(Bool())
    val lsType = Input(UInt(2.W))
    val addr   = Input(UInt(XLEN.W))
    val stData = Input(UInt(XLEN.W))
    val ldData = Output(UInt(XLEN.W))

    val DMemStore = new Bundle {}
    val DMemLoad  = new Bundle {}
  })
  
  val isMem = isLoadStore(io.lsType)
  

}