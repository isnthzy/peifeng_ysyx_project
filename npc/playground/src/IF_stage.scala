import chisel3._
import chisel3.util._
import config.Configs._

class IF_stage extends Module {
  val IF=IO(new Bundle {
    val to_id =Decoupled(new if_to_id_bus())
    
    val for_id=Input(new id_to_if_bus())
    val for_ex=Input(new ex_to_if_bus())

    val ar=Decoupled(new AxiAddressBundle())
    val r=Flipped(Decoupled(new AxiReadDataBundle()))
    val aw=Decoupled(new AxiAddressBundle())
    val w=Decoupled(new AxiWriteDataBundle())
    val b=Flipped(Decoupled(new AxiWriteResponseBundle()))
  })
  dontTouch(IF.ar);
  dontTouch(IF.r);
  dontTouch(IF.aw);
  dontTouch(IF.w);
  dontTouch(IF.b);
  val if_flush=dontTouch(Wire(Bool()))
  if_flush:=IF.for_ex.flush || IF.for_id.flush

  val if_valid=dontTouch(RegInit(false.B))
  val if_ready_go=dontTouch(Wire(Bool()))
  if_ready_go:=IF.to_id.ready
  when(if_ready_go){
    if_valid:=true.B
  }
  IF.to_id.valid:=Mux(if_flush, false.B , if_valid && if_ready_go)


  val br=Wire(new br_bus())
  br.taken:=IF.for_id.Br_J.taken || IF.for_ex.Br_B.taken
  br.target:=Mux(IF.for_id.Br_J.taken, IF.for_id.Br_J.target, IF.for_ex.Br_B.target)


  val if_pc     = RegInit(START_ADDR)
  val if_snpc   = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val if_dnpc   = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val if_nextpc = dontTouch(Wire(UInt(ADDR_WIDTH.W)))

  val if_inst=dontTouch(WireDefault(0.U(DATA_WIDTH.W)))
// pc通过axi结构访问sram取指
  if_snpc := if_pc + 4.U
  if_dnpc := Mux(IF.for_ex.epc.taken, IF.for_ex.epc.target, br.target)
  if_nextpc:= Mux(br.taken || IF.for_ex.epc.taken, if_dnpc, if_snpc)
  
  val ResetNReg=RegInit(false.B)
  ResetNReg:=  ~reset.asBool
  IF.ar.valid:= ResetNReg
  IF.ar.bits.addr:=if_nextpc
  IF.ar.bits.prot:=0.U
  IF.r.ready:=if_valid
  when(IF.r.fire){
    if_inst:=IF.r.bits.data
  }
  IF.to_id.bits.inst:=if_inst

  IF.w.valid:=0.U
  IF.w.bits.data:=0.U
  IF.w.bits.strb:=0.U

  IF.aw.valid:=0.U
  IF.aw.bits.addr:=0.U
  IF.aw.bits.prot:=0.U

  IF.b.ready:=0.U

  when(if_ready_go){ //if级控制不用if_valid信号（if级有点特殊）
    if_pc := if_nextpc //reg类型，更新慢一拍
  }
  //如果遇到阻塞情况，那么if级也要发生阻塞

  IF.to_id.bits.pc    :=if_pc
  IF.to_id.bits.nextpc:=if_nextpc
}

  // val Fetch=Module(new read_inst())
  // Fetch.io.clock:=clock
  // Fetch.io.reset:=reset
  // Fetch.io.nextpc:=if_nextpc
  // Fetch.io.fetch_wen:=if_ready_go

  // IF.to_id.bits.inst:=Fetch.io.inst


// class read_inst extends BlackBox with HasBlackBoxPath{
//   val io=IO(new Bundle {
//     val clock =Input(Clock())
//     val reset =Input(Bool())
//     val nextpc=Input(UInt(ADDR_WIDTH.W))
//     val inst  =Output(UInt(32.W))
//     val fetch_wen=Input(Bool())
//   })
//   addPath("playground/src/dpi-c/read_inst.sv")
// }
