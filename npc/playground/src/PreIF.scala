import chisel3._
import chisel3.util._
import config.Configs._
import os.truncate

class PreIF_s extends Module {
  val PreIF=IO(new Bundle {
    val to_if =Decoupled(new preif_to_if_bus())
    
    val for_id=Input(new id_to_preif_bus())
    val for_ex=Input(new ex_to_preif_bus())

    // val ar=Decoupled(new AxiAddressBundle())
    // val aw=Decoupled(new AxiAddressBundle())
    // val w=Decoupled(new AxiWriteDataBundle())
    // val b=Flipped(Decoupled(new AxiWriteResponseBundle()))
    val mem_addr=Output(UInt(ADDR_WIDTH.W))
    val write_en=Output(Bool())
    val wstrb=Output(UInt(4.W))
    val wdata=Output(UInt(DATA_WIDTH.W))
    val waddr_ok=Input(Bool())
    val wdata_ok=Input(Bool())

    val read_en=Output(Bool())
    val raddr_ok=Input(Bool())
  })
  val PreIF_ready_go=dontTouch(Wire(Bool()))
  val fetch_wen=dontTouch(Wire(Bool()))
  val PreIF_flush=dontTouch(Wire(Bool()))
  val br_modify=dontTouch(Wire(Bool()))
  //因为preif用pc取指，当传入分支跳转的nextpc时，需要修改pc为nextpc
  //并取消发起fetch

  br_modify:=( PreIF.for_ex.Br_B.taken ||
               PreIF.for_ex.epc.taken  ||
               PreIF.for_id.Br_J.taken)


  fetch_wen:=PreIF.to_if.ready && !br_modify

  PreIF_flush:=PreIF.for_ex.flush || PreIF.for_id.flush

  PreIF_ready_go:= fetch_wen && PreIF.raddr_ok
  PreIF.to_if.valid:= Mux(PreIF_flush,false.B, ~reset.asBool && PreIF_ready_go)

  val br=Wire(new br_bus())
  br.taken:=PreIF.for_id.Br_J.taken || PreIF.for_ex.Br_B.taken
  br.target:=Mux(PreIF.for_ex.Br_B.taken, PreIF.for_ex.Br_B.target, PreIF.for_id.Br_J.target)


  val PreIF_pc     = RegInit(START_ADDR)
  val PreIF_snpc   = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val PreIF_dnpc   = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val PreIF_nextpc = dontTouch(Wire(UInt(ADDR_WIDTH.W)))

  PreIF_snpc := PreIF_pc + 4.U
  PreIF_dnpc := Mux(PreIF.for_ex.epc.taken, PreIF.for_ex.epc.target, br.target)
  PreIF_nextpc:= Mux(br.taken || PreIF.for_ex.epc.taken, PreIF_dnpc, PreIF_snpc)

  // //--------------------------AXI4Lite--------------------------


  // //-----------------AXI4Lite AR Channel------------------------
  // val ar_idle :: ar_wait_ready :: Nil = Enum(2)
  // val arvalidReg=RegInit(false.B)
  // val araddrReg=RegInit(0.U(ADDR_WIDTH.W))

  
  // val ReadRequstState=RegInit(ar_idle)
  // when(ReadRequstState===ar_idle){
  //   when(fetch_wen){
  //     ReadRequstState:=ar_wait_ready
  //     araddrReg:=PreIF_pc
  //     arvalidReg:=true.B
  //   }
  // }.elsewhen(ReadRequstState===ar_wait_ready){
  //   when(PreIF.ar.ready){
  //     ReadRequstState:=ar_idle
  //     arvalidReg:=false.B
  //   }
  // }

  // PreIF.ar.valid:=arvalidReg
  // PreIF.ar.bits.addr:=araddrReg
  // PreIF.ar.bits.prot:=0.U
  // //-----------------AXI4Lite AR Channel------------------------
  

  // //-----------------AXI4Lite Other Channel---------------------

  // PreIF.w.valid:=0.U
  // PreIF.w.bits.data:=0.U
  // PreIF.w.bits.strb:=0.U

  // PreIF.aw.valid:=0.U
  // PreIF.aw.bits.addr:=0.U
  // PreIF.aw.bits.prot:=0.U

  // PreIF.b.ready:=0.U

  // //-----------------AXI4Lite Other Channel--------------------


  // //----------------------AXI4Lite-----------------------------

 
  when(PreIF_ready_go || br_modify){ 
    PreIF_pc := PreIF_nextpc //reg类型，更新慢一拍
  }
  //如果遇到阻塞情况，那么if级也要发生阻塞

  PreIF.to_if.bits.pc    :=PreIF_pc
  PreIF.to_if.bits.nextpc:=PreIF_nextpc
}

