import chisel3._
import chisel3.util._
import config.Configs._

class IF_stage extends Module {
  val IF=IO(new Bundle {
    // val IO    =Output(new if_to_id_bus())
    val IO    =Decoupled(new if_to_id_bus())
    val br_bus=Input(new br_bus())
    val epc_bus=Input(new ex_to_if_bus())
    val flush=Input(Bool())
  })
  val if_valid=dontTouch(RegInit(false.B))
  val if_ready_go=dontTouch(Wire(Bool()))
  if_ready_go:=IF.IO.ready
  when(if_ready_go){
    if_valid:=true.B
  }
  IF.IO.valid:=Mux(IF.flush, false.B ,if_valid && if_ready_go)


  val REGpc   = RegInit(START_ADDR)
  val snpc    = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val dnpc    = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val nextpc  = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val Fetch   = Module(new read_inst())
// pc在这里用dpi-c进行取指
  snpc  := REGpc + 4.U
  dnpc  := Mux(IF.epc_bus.epc_wen, IF.epc_bus.csr_epc, IF.br_bus.target)
  nextpc:= Mux(IF.br_bus.taken||IF.epc_bus.epc_wen, dnpc, snpc)
  
  Fetch.io.clock:=clock
  Fetch.io.reset:=reset
  Fetch.io.nextpc:=nextpc
  Fetch.io.fetch_wen:=if_ready_go

  IF.IO.bits.inst:=Fetch.io.inst
  when(if_ready_go){ //if级控制不用if_valid信号（if级有点特殊）
    REGpc := nextpc //reg类型，更新慢一拍
  }
  //如果遇到阻塞情况，那么if级也要发生阻塞

  IF.IO.bits.pc  :=REGpc
  IF.IO.bits.nextpc:=nextpc
}

class read_inst extends BlackBox with HasBlackBoxPath{
  val io=IO(new Bundle {
    val clock =Input(Clock())
    val reset =Input(Bool())
    val nextpc=Input(UInt(ADDR_WIDTH.W))
    val inst  =Output(UInt(32.W))
    val fetch_wen=Input(Bool())
  })
  addPath("playground/src/dpi-c/read_inst.sv")
}
