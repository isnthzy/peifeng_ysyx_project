import chisel3._
import chisel3.util._
import config.Configs._

class IF_stage extends Module {
  val IF=IO(new Bundle {
    val IO    =Output(new if_to_id_bus())
    val br_bus=Input(new br_bus())
  })
  val REGpc   = RegInit(START_ADDR)
  val snpc    = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val nextpc  = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val Fetch   = Module(new read_inst())
// pc在这里用dpi-c进行取指
  snpc  := REGpc + 4.U
  nextpc:= Mux(IF.br_bus.is_jump, IF.br_bus.dnpc, snpc)
  
  Fetch.io.clock:=clock
  Fetch.io.reset:=reset
  Fetch.io.nextpc:=nextpc
  IF.IO.inst:=Fetch.io.inst

  REGpc := nextpc //reg类型，更新慢一拍
  IF.IO.pc  :=REGpc
  IF.IO.nextpc:=nextpc
}

class read_inst extends BlackBox with HasBlackBoxPath{
  val io=IO(new Bundle {
    val clock =Input(Clock())
    val reset =Input(Bool())
    val nextpc=Input(UInt(ADDR_WIDTH.W))
    val pc    =Output(UInt(ADDR_WIDTH.W))
    val inst  =Output(UInt(32.W))
  })
  addPath("playground/src/dpi-c/read_inst.sv")
}
