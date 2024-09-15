package CoreConfig
import chisel3._

object Configs {
  // val START_ADDR = "h7ffffffc".U(32.W)  //开始地址，设计成80000000-4=7ffffffc是为了初始化reset
  def START_ADDR = 0x80000000
  def ADDR_WIDTH = 32 // 地址位宽
  def ADDR_BYTE_WIDTH = ADDR_WIDTH / 8    // 地址位宽按字节算
  def DATA_WIDTH = 32 // 数据位宽
  def DATA_WIDTH_H = 16   // 半字数据位宽
  def DATA_WIDTH_B = 8    // 字节数据位宽
  def RISCV32E_ECALLREG = 15.U
  def RISCV32_ECALLREG = 17.U

  def INST_NOP = "h00000013".U(32.W)
}

trait DeviceConfig{
  //NOTE:警告，请确保设备地址与协同仿真框架一致，避免skip信号提交错误

  def DEVICE_BASE = 0xa0000000
  def SERIAL_PORT = DEVICE_BASE + 0x00003f8
  def SERIAL_SIZE = 0x0

  def RTC_ADDR    = DEVICE_BASE + 0x0000048
  def RTC_SIZE    = 0x8

  def KBD_ADDR    = DEVICE_BASE + 0x0000060
  def VGACTL_ADDR = DEVICE_BASE + 0x0000100
  def SYNC_ADDR   = VGACTL_ADDR + 0x4
  def FB_ADDR     = DEVICE_BASE + 0x1000000

  def SCREEN_SIZE = 400*300
}

object ISAConfig{
  def RV32E = false
}

object GenCtrl{
  def VERILATOR_SIM=true
}