package Device

import chisel3._
import chisel3.util._
import CoreConfig.Configs.ADDR_WIDTH
import CoreConfig._

class DeviceSkip extends Module with DeviceAddr{
  val io = IO(new Bundle {
    val isLoadStore = Input(Bool())
    val addr = Input(UInt(ADDR_WIDTH.W))
    val skip = Output(Bool())
  })
  val readSkip  = Wire(Bool()) 
  val writeSkip = Wire(Bool()) 
  io.skip := ( readSkip || writeSkip ) && io.isLoadStore

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