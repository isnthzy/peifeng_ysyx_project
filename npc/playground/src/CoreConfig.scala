package CoreConfig
import chisel3._

object Configs {
  // val START_ADDR = "h7ffffffc".U(32.W)  //开始地址，设计成80000000-4=7ffffffc是为了初始化reset
  def START_ADDR = "h80000000".U(ADDR_WIDTH .W)
  def ADDR_WIDTH = 32 // 地址位宽
  def ADDR_BYTE_WIDTH = ADDR_WIDTH / 8    // 地址位宽按字节算
  def DATA_WIDTH = 32 // 数据位宽
  def DATA_WIDTH_H = 16   // 半字数据位宽
  def DATA_WIDTH_B = 8    // 字节数据位宽
  def RISCV32E_ECALLREG = 15.U
  def RISCV32_ECALLREG = 17.U

  def INST_NOP = "h00000013".U(32.W)
}

object ISAConfig{
  def RV32E = false
}

object GenCtrl{
  def VERILATOR_SIM=false
}