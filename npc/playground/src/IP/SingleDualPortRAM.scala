package IP

import chisel3._
import chisel3.util._
import Util.LYDebugLog

class SingleDualPortRAM(val size: Int, val width: Int) extends RawModule {
  val addrWidth = log2Ceil(size)

  val io = FlatIO(new Bundle {
    val clka    = Input(Clock())
    val wea     = Input(Bool())
    val addra   = Input(UInt(addrWidth.W))
    val dina    = Input(UInt(width.W))
    val clkb    = Input(Clock())
    val addrb   = Input(UInt(addrWidth.W))
    val doutb   = Output(UInt(width.W))
  })

  // Use SyncReadMem for memory
  val mem = SyncReadMem(size, UInt(width.W))

  // Port A: Write
  withClock(io.clka) {
    when(io.wea) {
      mem.write(io.addra, io.dina)
    }
  }

  // Port B: Read
  withClock(io.clkb) {
    when(io.addra===io.addrb){
      LYDebugLog("IP found read store conflict\n")
      io.doutb:=Fill(width,false.B)
    }.otherwise{
      io.doutb := mem.read(io.addrb, true.B)
    }
  }
}