import chisel3._
import chisel3.util._
import config.Configs._
import os.truncate

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
  val inst_is_valid=dontTouch(Wire(Bool()))
  
  val if_flush=dontTouch(Wire(Bool()))
  if_flush:=IF.for_ex.flush || IF.for_id.flush

  val if_valid=dontTouch(RegInit(false.B))
  val if_ready_go=dontTouch(Wire(Bool()))
  if_ready_go:=Mux(inst_is_valid,IF.to_id.ready,false.B)
  //如果取指为未完成，发起阻塞
  if_valid:=true.B
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

  //--------------------------AXI4Lite--------------------------
  val s_idle :: s_wait_ready :: Nil = Enum(2)
  //-----------------AXI4Lite AR Channel------------------------
  val arvalidReg=RegInit(false.B)
  val araddrReg=RegInit(0.U(ADDR_WIDTH.W))

  
  val ReadRequstState=RegInit(s_idle)
  when(ReadRequstState===s_idle){
    // when(~reset.asBool){
      ReadRequstState:=s_wait_ready
      araddrReg:=if_nextpc
      arvalidReg:=true.B
    // }
  }.elsewhen(ReadRequstState===s_wait_ready){
    when(IF.ar.ready){
      ReadRequstState:=s_idle
      arvalidReg:=false.B
    }
  }

  IF.ar.valid:=arvalidReg
  IF.ar.bits.addr:=araddrReg
  IF.ar.bits.prot:=0.U
  //-----------------AXI4Lite AR Channel------------------------


  //-----------------AXI4Lite R  Channel------------------------
  IF.r.ready:=true.B

  when(IF.r.fire){
    if_inst:=IF.r.bits.data
  }
  inst_is_valid:=IF.r.fire
  //-----------------AXI4Lite R  Channel------------------------
  //-----------------AXI4Lite Other Channel------------------------
  IF.w.valid:=0.U
  IF.w.bits.data:=0.U
  IF.w.bits.strb:=0.U

  IF.aw.valid:=0.U
  IF.aw.bits.addr:=0.U
  IF.aw.bits.prot:=0.U

  IF.b.ready:=0.U

  //--------------------------AXI4Lite--------------------------
  when(if_ready_go){ //if级控制不用if_valid信号（if级有点特殊）
    if_pc := if_nextpc //reg类型，更新慢一拍
  }
  //如果遇到阻塞情况，那么if级也要发生阻塞

  IF.to_id.bits.pc    :=if_pc
  IF.to_id.bits.nextpc:=if_nextpc
  IF.to_id.bits.inst  :=if_inst

}


