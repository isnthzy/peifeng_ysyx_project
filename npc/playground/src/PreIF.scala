import chisel3._
import chisel3.util._
import config.Configs._
import os.truncate

class PreIF_s extends Module {
  val PreIF=IO(new Bundle {
    val to_if =Decoupled(new preif_to_if_bus())
    
    val for_id=Input(new id_to_preif_bus())
    val for_ex=Input(new ex_to_preif_bus())

    val ar=Decoupled(new AxiAddressBundle())
    val aw=Decoupled(new AxiAddressBundle())
    val w=Decoupled(new AxiWriteDataBundle())
    val b=Flipped(Decoupled(new AxiWriteResponseBundle()))
  })
  val PreIF_ready_go=dontTouch(Wire(Bool()))
  val fetch_wen=dontTouch(Wire(Bool()))
  val PreIF_flush=dontTouch(Wire(Bool()))
  val br=Wire(new br_bus())
  

  br.nextpc_stall:=(PreIF.for_id.Br_J.nextpc_stall 
                   || PreIF.for_ex.Br_B.nextpc_stall
                   || PreIF.for_ex.epc.nextpc_stall )
  fetch_wen:=PreIF.to_if.ready && !br.nextpc_stall

  PreIF_flush:=PreIF.for_ex.flush || PreIF.for_id.flush

  PreIF_ready_go:= fetch_wen && PreIF.ar.fire
  PreIF.to_if.valid:= Mux(PreIF_flush,false.B, ~reset.asBool && PreIF_ready_go)

  
  br.taken:=PreIF.for_id.Br_J.taken || PreIF.for_ex.Br_B.taken
  br.target:=Mux(PreIF.for_id.Br_J.taken, PreIF.for_id.Br_J.target, PreIF.for_ex.Br_B.target)


  val PreIF_pc     = RegInit(START_ADDR)
  val PreIF_snpc   = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val PreIF_dnpc   = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val PreIF_nextpc = dontTouch(Wire(UInt(ADDR_WIDTH.W)))

  PreIF_snpc := PreIF_pc + 4.U
  PreIF_dnpc := Mux(PreIF.for_ex.epc.taken, PreIF.for_ex.epc.target, br.target)
  PreIF_nextpc:= Mux(PreIF_nextpcStall, PreIF_nextpc ,
                  Mux(br.taken || PreIF.for_ex.epc.taken, PreIF_dnpc, PreIF_snpc))

  //--------------------------AXI4Lite--------------------------


  //-----------------AXI4Lite AR Channel------------------------
  val s_idle :: s_wait_ready :: Nil = Enum(2)
  val arvalidReg=RegInit(false.B)
  val araddrReg=RegInit(0.U(ADDR_WIDTH.W))

  
  val ReadRequstState=RegInit(s_idle)
  when(ReadRequstState===s_idle){
    when(fetch_wen){
      ReadRequstState:=s_wait_ready
      araddrReg:=PreIF_pc
      arvalidReg:=true.B
    }
  }.elsewhen(ReadRequstState===s_wait_ready){
    when(PreIF.ar.ready){
      ReadRequstState:=s_idle
      arvalidReg:=false.B
    }
  }

  PreIF.ar.valid:=arvalidReg
  PreIF.ar.bits.addr:=araddrReg
  PreIF.ar.bits.prot:=0.U
  //-----------------AXI4Lite AR Channel------------------------
  

  //-----------------AXI4Lite Other Channel---------------------

  PreIF.w.valid:=0.U
  PreIF.w.bits.data:=0.U
  PreIF.w.bits.strb:=0.U

  PreIF.aw.valid:=0.U
  PreIF.aw.bits.addr:=0.U
  PreIF.aw.bits.prot:=0.U

  PreIF.b.ready:=0.U

  //-----------------AXI4Lite Other Channel--------------------


  //----------------------AXI4Lite-----------------------------

 
  when(PreIF_ready_go){ //if级控制不用if_valid信号（if级有点特殊）
    // printf("PreIF: pc=%x, nextpc=%x , is_fire=%d\n", PreIF_pc, PreIF_nextpc,PreIF.to_if.fire)
    PreIF_pc := PreIF_nextpc //reg类型，更新慢一拍
  }
  //如果遇到阻塞情况，那么if级也要发生阻塞

  PreIF.to_if.bits.pc    :=PreIF_pc
  PreIF.to_if.bits.nextpc:=PreIF_nextpc
}

