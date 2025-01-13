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
  val writeSkip = Wire(Bool()) 
  io.skip := (readSkip || writeSkip) && io.isLoadStore

  if(GenerateParams.getParam("SOC_MODE").asInstanceOf[Boolean]){
    readSkip:=(
      (io.addr >= UART_BASE && io.addr < UART_BASE+ UART_SIZE)
    ||(io.addr >= SPI_BASE  && io.addr < SPI_BASE + SPI_SIZE)
    ||(io.addr >= RTC_ADDR  && io.addr < RTC_ADDR + RTC_SIZE)
    ||(io.addr >= GPIO_SW_BASE && io.addr < GPIO_SW_BASE + GPIO_SW_SIZE)
    ||(io.addr >= KBD_BASE  && io.addr < KBD_BASE + KBD_SIZE)
    )

    writeSkip:=(
      (io.addr >= SPI_BASE  && io.addr < SPI_BASE +SPI_SIZE)
    )
  }else{
    readSkip:=false.B
    writeSkip:=false.B
  }

}