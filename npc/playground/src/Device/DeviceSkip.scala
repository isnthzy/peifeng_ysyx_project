package Device

import chisel3._
import chisel3.util._
import CoreConfig.Configs.ADDR_WIDTH
import CoreConfig._

class DeviceSkip extends Module with DeviceAddr{
  val io = IO(new Bundle {
    val addr = Input(UInt(ADDR_WIDTH.W))
    val skip = Output(Bool())
  })
  val readSkip  = false.B 
  val writeSkip = false.B
  io.skip := readSkip || writeSkip

  readSkip:=(
    (io.addr===RTC_ADDR || io.addr === RTC_ADDR+4.U)
  || io.addr===KBD_ADDR || io.addr === VGACTL_ADDR
  ||(io.addr >=FB_ADDR  || io.addr  <= FB_ADDR+(SCREEN_SIZE*4).U)
  )

  writeSkip:=(
     io.addr===SERIAL_PORT || io.addr===SYNC_ADDR
  ||(io.addr >=FB_ADDR  || io.addr  <= FB_ADDR+(SCREEN_SIZE*4).U)
  )
}