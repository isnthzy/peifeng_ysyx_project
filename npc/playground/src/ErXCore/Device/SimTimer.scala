package ErXCore
import chisel3._
import chisel3.util._

trait DeviceConfig{
  //NOTE:警告，请确保设备地址与协同仿真框架一致，避免skip信号提交错误
  def RTC_ADDR  = "h02000000".U(32.W)
  def RTC_SIZE  = 8.U

  def UART_BASE = "h10000000".U(32.W)
  def UART_SIZE = "h1000".U

  def SPI_BASE  = "h10001000".U(32.W)
  def SPI_SIZE  = "h1000".U

  def GPIO_SW_BASE = "h10002004".U(32.W)
  def GPIO_SW_SIZE = 4.U

  def KBD_BASE = "h10011000".U(32.W)
  def KBD_SIZE = 8.U
}

class SimTimer extends ErXCoreModule with DeviceConfig{
  val io=IO(new Axi4Slave())

  val timer=RegInit(0.U(64.W))
  timer:=timer+1.U

  val state_idle :: state_resp :: Nil = Enum(2)
  val timerState=RegInit(state_idle)
  val addrResp=RegInit(0.U(XLEN.W))
  io.ar.ready:=true.B
  io.r.valid :=false.B
  io.r.bits:=0.U.asTypeOf(io.r.bits)

  switch(timerState){
    is(state_idle){
      when(io.ar.fire){
        timerState:=state_resp
        addrResp:=io.ar.bits.addr
      }
    }
    is(state_resp){
      io.r.valid:=true.B
      io.r.bits.last:=true.B
      when(io.r.fire){
        when(addrResp===RTC_ADDR){
          io.r.bits.data:=timer(31,0)
        }.elsewhen(addrResp===RTC_ADDR+4.U){
          io.r.bits.data:=timer(63,32)
        }.otherwise{
          io.r.bits.data:="hffffffff".U
        }
        timerState:=state_idle
      }
    }
  }

  io.aw.ready:=false.B
  io.w.ready:=false.B
  io.b.valid:=false.B
  io.b.bits:=0.U.asTypeOf(io.b.bits)
}