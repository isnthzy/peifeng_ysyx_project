package Device

import chisel3._
import chisel3.util._
import CoreConfig.Configs.ADDR_WIDTH
import CoreConfig._

class DeviceSkip extends Module with DeviceConfig{
  val io = IO(new Bundle {
    val isLoadStore = Input(Bool())
    val addr = Input(UInt(ADDR_WIDTH.W))
    val skip = Output(Bool())
  })
  val readSkip  = Wire(Bool()) 
  // val writeSkip = Wire(Bool()) 
  io.skip := readSkip  && io.isLoadStore

  readSkip:=(
    (io.addr===RTC_ADDR.U || io.addr === RTC_ADDR.U+4.U)
  || io.addr===KBD_ADDR.U || io.addr === VGACTL_ADDR.U
  ||(io.addr >=FB_ADDR.U  && io.addr  <= FB_ADDR.U+(SCREEN_SIZE*4).U)
  )

  // writeSkip:=(
  //    io.addr===SERIAL_PORT || io.addr===SYNC_ADDR
  // ||(io.addr >=FB_ADDR     && io.addr  <= FB_ADDR+(SCREEN_SIZE*4).U)
  // )
}