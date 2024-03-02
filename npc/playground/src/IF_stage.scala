import chisel3._
import chisel3.util._
import config.Configs._


class IF_stage extends Module {
  val IF=IO(new Bundle {
    val IO = Flipped(Decoupled(new preif_to_if_bus()))
    val to_id =Decoupled(new if_to_id_bus())
    
    val for_id=Input(new id_to_if_bus())
    val for_ex=Input(new ex_to_if_bus())

    val r=Flipped(Decoupled(new AxiReadDataBundle()))
  })
  val if_inst=dontTouch(WireDefault(0.U(32.W)))
  val if_inst_is_valid=dontTouch(Wire(Bool()))

  val if_flush=dontTouch(Wire(Bool()))
  if_flush:= IF.for_ex.flush || IF.for_id.flush

  val resetnReg=RegInit(false.B)
  resetnReg:= ~reset.asBool

  val if_valid=dontTouch(RegInit(false.B))
  val if_ready_go=dontTouch(Wire(Bool()))
  if_ready_go:=Mux(if_inst_is_valid,true.B,false.B)
  IF.IO.ready :=( !if_valid || if_ready_go && IF.to_id.ready )&& resetnReg
  when(IF.IO.ready){
    if_valid:=IF.IO.valid
  }
  IF.to_id.valid:=Mux(if_flush, false.B , if_valid && if_ready_go)
  

  //-----------------AXI4Lite R  Channel------------------------
  IF.r.ready:=true.B

  when(IF.r.fire){
    if_inst:=IF.r.bits.data
  }
  if_inst_is_valid:=IF.r.fire
  //-----------------AXI4Lite R  Channel------------------------

  IF.to_id.bits.pc:=IF.IO.bits.pc
  IF.to_id.bits.nextpc:=IF.IO.bits.nextpc
  IF.to_id.bits.inst:=if_inst
}


