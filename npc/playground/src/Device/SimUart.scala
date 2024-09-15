package Device
import chisel3._
import chisel3.util._
import Axi.Axi4LiteSlave
import CoreConfig.Configs._

class SimUart extends Module {
  val io=IO(new Axi4LiteSlave())

  val state_idle :: state_write :: state_bresp :: Nil = Enum(3)
  val uartState=RegInit(state_idle)
  val addrWrite=RegInit(0.U(ADDR_WIDTH.W))
  io.aw.ready:=true.B
  
  io.w.ready:=false.B
  io.b.valid:=false.B

  io.b.bits:=0.U.asTypeOf(io.b.bits)
  switch(uartState){
    is(state_idle){
      io.w.ready:=false.B
      when(io.aw.fire){
        uartState:=state_write
        addrWrite:=io.w.bits.data
      } 
    }
    is(state_write){
      io.w.ready:=true.B
      when(io.w.fire){
        uartState:=state_bresp
        printf("%c",addrWrite(7,0))
      }
    }
    is(state_bresp){
      io.b.valid:=true.B
      when(io.b.fire){
        uartState:=state_idle
      }
    }
  }

  io.ar.ready:=false.B
  io.r.valid:=false.B
  io.r.bits:=0.U.asTypeOf(io.r.bits)
}