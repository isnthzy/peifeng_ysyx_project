import chisel3._
import chisel3.util._
import config.Configs._

class IF_stage extends Module {
  val IF=IO(new Bundle {
    val to_id =Decoupled(new if_to_id_bus())
    
    val for_id=Input(new id_to_if_bus())
    val for_ex=Input(new ex_to_if_bus())
  })
  val if_flush=dontTouch(Wire(Bool()))
  if_flush:=IF.for_ex.flush || IF.for_id.flush

  val if_valid=dontTouch(RegInit(false.B))
  val if_ready_go=dontTouch(Wire(Bool()))
  if_ready_go:=IF.to_id.ready
  when(if_ready_go){
    if_valid:=true.B
  }
  IF.to_id.valid:=Mux(if_flush, false.B ,if_valid && if_ready_go)


  val br=Wire(new br_bus())
  br.taken:=IF.for_id.Br_J.taken || IF.for_ex.Br_B.taken
  br.target:=Mux(IF.for_id.Br_J.taken, IF.for_id.Br_J.target, IF.for_ex.Br_B.target)


  val REGpc   = RegInit(START_ADDR)
  val snpc    = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val dnpc    = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val nextpc  = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val Fetch   = Module(new read_inst())
// pc在这里用dpi-c进行取指
  snpc  := REGpc + 4.U
  dnpc  := Mux(IF.for_ex.epc.taken, IF.for_ex.epc.target, br.target)
  nextpc:= Mux(br.taken || IF.for_ex.epc.taken, dnpc, snpc)
  
  Fetch.io.clock:=clock
  Fetch.io.reset:=reset
  Fetch.io.nextpc:=nextpc
  Fetch.io.fetch_wen:=if_ready_go

  IF.to_id.bits.inst:=Fetch.io.inst
  when(if_ready_go){ //if级控制不用if_valid信号（if级有点特殊）
    REGpc := nextpc //reg类型，更新慢一拍
  }
  //如果遇到阻塞情况，那么if级也要发生阻塞

  IF.to_id.bits.pc  :=REGpc
  IF.to_id.bits.nextpc:=nextpc
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
