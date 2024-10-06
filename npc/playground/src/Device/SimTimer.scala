package Device
import chisel3._
import chisel3.util._
import Axi.Axi4Slave
import CoreConfig.Configs._
import CoreConfig.DeviceConfig

class SimTimer extends Module with DeviceConfig{
  val io=IO(new Axi4Slave())

  val timer=RegInit(0.U(64.W))
  timer:=timer+1.U

  val state_idle :: state_resp :: Nil = Enum(2)
  val timerState=RegInit(state_idle)
  val addrResp=RegInit(0.U(ADDR_WIDTH.W))
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