import chisel3._
import chisel3.util._
import config.Configs._


class IF_stage extends Module {
  val IF=IO(new Bundle {
    val IO = Flipped(Decoupled(new pf_to_if_bus()))
    val to_id =Decoupled(new if_to_id_bus())
    
    val for_id=Input(new id_to_if_bus())
    val for_ex=Input(new ex_to_if_bus())
    
    val dl=new AxiBridgeDataLoad()
    // val rdata=Input(UInt(DATA_WIDTH.W))
    // val rdata_ok=Input(Bool())
  })
  val if_clog=dontTouch(Wire(Bool()))
  val if_inst=dontTouch(WireDefault(0.U(32.W)))
  val if_inst_ok=dontTouch(Wire(Bool()))
  val if_flush=dontTouch(Wire(Bool()))
  if_flush:= IF.for_ex.flush || IF.for_id.flush

  val if_valid=dontTouch(RegInit(false.B))
  val if_ready_go=dontTouch(Wire(Bool()))
  if_clog:= ~if_inst_ok&&if_valid
  if_ready_go:=Mux(if_clog,false.B,true.B)
  IF.IO.ready:= !if_valid || if_ready_go && IF.to_id.ready 
  when(IF.IO.ready){
    if_valid:=IF.IO.valid
  }
  IF.to_id.valid:=Mux(if_flush, false.B , if_valid && if_ready_go)

  
  val if_inst_ok_buff=dontTouch(RegInit(false.B))
  val if_inst_buff=dontTouch(RegInit(0.U(32.W)))
  val if_use_inst_buff=dontTouch(RegInit(false.B))
  if_inst:=Mux(if_use_inst_buff,if_inst_buff,IF.dl.rdata)
  if_inst_ok:=if_inst_ok_buff
  when(IF.IO.fire){
    if_use_inst_buff:=false.B
    if_inst_ok_buff:=false.B
  }
  when(IF.dl.rdata_ok){
    if_inst_buff:=IF.dl.rdata
    if_use_inst_buff:=true.B
    if_inst_ok_buff:=true.B
    if_inst_ok:=true.B
  }
  //加buffer是为了在if级暂存取到的指令，避免因为流水与下一级握手失败if级失去指
  //当接收到新的数据时，取消使用buffer
  //效果大概是
  /**
     pc     pc      pc    pc+4   pc+4
    inst  buffer  buffer  inst  buffer
    */

  IF.to_id.bits.pc:=IF.IO.bits.pc
  IF.to_id.bits.nextpc:=IF.IO.bits.nextpc
  IF.to_id.bits.inst:=if_inst
}


