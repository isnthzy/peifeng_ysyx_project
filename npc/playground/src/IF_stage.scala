import chisel3._
import chisel3.util._
import config.Configs._

class IF_stage extends Module {
  val IF=IO(new Bundle {
    val IO    =Output(new if_to_id_bus())
    val br_bus=Input(new br_bus())
    val epc_bus=Input(new wb_to_if_bus())
  })
  val REGpc   = RegInit(START_ADDR)
  val snpc    = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val dnpc    = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val nextpc  = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val Fetch   = Module(new read_inst())
// pc在这里用dpi-c进行取指
  snpc  := REGpc + 4.U
  dnpc  := Mux(IF.epc_bus.epc_wen, IF.epc_bus.csr_epc, IF.br_bus.dnpc)
  nextpc:= Mux(IF.br_bus.is_jump||IF.epc_bus.epc_wen, dnpc, snpc)
  
  Fetch.io.clock:=clock
  Fetch.io.reset:=reset
  Fetch.io.pc   :=REGpc
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
    val pc    =Input(UInt(ADDR_WIDTH.W))
    val inst  =Output(UInt(32.W))
  })
  addPath("playground/src/dpi-c/read_inst.sv")
}
