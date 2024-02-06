package config
import chisel3._

object Configs {
  val START_ADDR = "h7ffffffc".U(32.W)  //开始地址，设计成80000000-4=7ffffffc是为了初始化reset
  val ADDR_WIDTH = 32 // 地址位宽
  val ADDR_BYTE_WIDTH = ADDR_WIDTH / 8    // 地址位宽按字节算
  val DATA_WIDTH = 32 // 数据位宽
  val DATA_WIDTH_H = 16   // 半字数据位宽
  val DATA_WIDTH_B = 8    // 字节数据位宽
  val RISCV32E_ECALLREG = 15.U
  val RISCV32_ECALLREG = 17.U
}