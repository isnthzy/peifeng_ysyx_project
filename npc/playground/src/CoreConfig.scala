package CoreConfig
import chisel3._

object Configs {
  // val START_ADDR = "h7ffffffc".U(32.W)  //开始地址，设计成80000000-4=7ffffffc是为了初始化reset
  def START_ADDR = "h30000000".U(ADDR_WIDTH.W)
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

  // def DEVICE_BASE = "ha0000000".U(32.W)
  // def SERIAL_PORT = DEVICE_BASE + "h00003f8".U(32.W)
  // def SERIAL_SIZE = 0.U


  // def KBD_ADDR    = DEVICE_BASE + "h0000060".U(32.W)
  // def VGACTL_ADDR = DEVICE_BASE + "h0000100".U(32.W)
  // def SYNC_ADDR   = VGACTL_ADDR +  4.U(32.W)
  // def FB_ADDR     = DEVICE_BASE + "h1000000".U(32.W)

  // def SCREEN_SIZE = 400*300

  def RTC_ADDR  = "h02000000".U(32.W)
  def RTC_SIZE  = 8.U

  def UART_BASE = "h10000000".U(32.W)
  def UART_SIZE = "h1000".U

  def SPI_BASE  = "h10001000".U(32.W)
  def SPI_SIZE  = "h1000".U

  def GPIO_SW_BASE = "h10002004".U(32.W)
  def GPIO_SW_SIZE = 4.U
}

object ISAConfig{
  def RV32E = false
}

object GenCtrl{
  def VERILATOR_SIM=true
}