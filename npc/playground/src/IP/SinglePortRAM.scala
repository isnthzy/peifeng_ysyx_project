package IP

import chisel3._
import chisel3.util._

class SinglePortRAM(val size: Int, val width: Int) extends RawModule {
  val addrWidth = log2Ceil(size)

  val io = FlatIO(new Bundle {
    val clka    = Input(Clock())
    val wea     = Input(Bool())
    val addra   = Input(UInt(addrWidth.W))
    val dina    = Input(UInt(width.W))
    val douta   = Output(UInt(width.W))
  })

  // Use SyncReadMem for memory
  val mem = SyncReadMem(size, UInt(width.W))

  // Port A: Write
  withClock(io.clka) {
    when(io.wea) {
      mem.write(io.addra, io.dina)
    }
    io.douta := mem.read(io.addra, true.B)
  }
}