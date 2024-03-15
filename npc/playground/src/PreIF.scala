import chisel3._
import chisel3.util._
import config.Configs._

class PreIF_stage extends Module {
  val PF=IO(new Bundle {
    val to_if =Decoupled(new pf_to_if_bus())
    
    val for_id=Input(new id_to_pf_bus())
    val for_ex=Input(new ex_to_pf_bus())

    val al=new AxiBridgeAddrLoad()
    val s=new AxiBridgeStore()
    // val mem_addr=Output(UInt(ADDR_WIDTH.W))

    // val write_en=Output(Bool())
    // val wstrb=Output(UInt(4.W))
    // val wdata=Output(UInt(DATA_WIDTH.W))
    // val waddr_ok=Input(Bool())
    // val wdata_ok=Input(Bool())

    // val read_en=Output(Bool())
    // val raddr_ok=Input(Bool())
  })
  val pf_ready_go=dontTouch(Wire(Bool()))
  val fetch_wen=dontTouch(Wire(Bool()))
  val pf_flush=dontTouch(Wire(Bool()))
  val pf_raddr_ok=dontTouch(Wire(Bool()))
  val wait_br_addr_ok=dontTouch(RegInit(false.B))
  val br=Wire(new br_bus())
  //因为preif用pc取指，当传入分支跳转的nextpc时，需要修改pc为nextpc
  //并取消发起fetch
  br.stall:=PF.for_ex.Br_B.stall || PF.for_id.Br_J.stall 
  val br_modify=( PF.for_ex.Br_B.taken ||
                  PF.for_ex.epc.taken  ||
                  PF.for_id.Br_J.taken)

  pf_raddr_ok:=(PF.al.raddr_ok&& ~br_modify)&&(Mux(wait_br_addr_ok,false.B,PF.al.raddr_ok))
  //这段代码看着难以理解，所以我补充了这段话，以便我和可能会来学习的后人理解
  /* 
  当id,ex发起分支跳转后
  我们发现，加了总线后的preif会发出错误的取指请求，这时候这个取错误的请求应不应该接受完全要看
  pf_ready_go的放行，如果发起错误的取指请求后pf_ready_go放行，那么if级的状况是
      nextpc
      inst(pc+4)
  nextpc对应着pc+4的取的指令，那么很显然pf_ready_go不应该放行
  根源上是addr_ok可能对应的是错误的pc的取指请求
  所以，我们要写个正确的raddr_ok，来反应正确放行
  考虑以下两种情况
  1.raddr_ok的时候,正好接受到分支传输，这时候pf_raddr_ok置低，表明读地址通道没有真正的ok
  2.pf已经发起ar通道，此时等待raddr_ok，这时候pf_raddr_ok置低，表明读地址通道没有真正的ok
  3.pf提前发起ar通道，raddr_ok提前传输，流入下一级。这种情况不会出现，因为取指请求只有在if级ready了才能发起
  所以，解决1,2我们需要等待真正的读地址通道ok，当接受到分支传输时，至高wait_br_addr_ok寄存器信号，表明需要等待分支的addr_ok返回
  当分支的addr_ok返回后，说明取到了分支的跳转后的指令，pf_raddr_ok至高此时放行ready_go，流向下一级
   */

  fetch_wen:=PF.to_if.ready && !br.stall

  pf_flush:=PF.for_ex.flush || PF.for_id.flush

  pf_ready_go:= fetch_wen && pf_raddr_ok
  PF.to_if.valid:= Mux(pf_flush,false.B, ~reset.asBool && pf_ready_go)

  br.taken:=PF.for_id.Br_J.taken || PF.for_ex.Br_B.taken
  br.target:=Mux(PF.for_ex.Br_B.taken, PF.for_ex.Br_B.target, PF.for_id.Br_J.target)


  val pf_pc     = RegInit(START_ADDR)
  val pf_snpc   = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val pf_dnpc   = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val pf_nextpc = dontTouch(Wire(UInt(ADDR_WIDTH.W)))

  pf_snpc := pf_pc + 4.U
  pf_dnpc := Mux(PF.for_ex.epc.taken, PF.for_ex.epc.target, br.target)
  pf_nextpc:= Mux(br.taken || PF.for_ex.epc.taken, pf_dnpc, pf_snpc)

//---------------------------AXI BRIDEG---------------------------
  PF.al.raddr:=pf_pc
  PF.al.ren:=fetch_wen

  PF.s.waddr:=pf_pc
  PF.s.wen:=false.B
  PF.s.wstrb:=0.U
  PF.s.wdata:=0.U
//---------------------------AXI BRIDEG---------------------------
 
  when(pf_ready_go || br_modify){ 
    pf_pc := pf_nextpc //reg类型，更新慢一拍
    wait_br_addr_ok:=true.B
    //这个信号的作用配合上面的注释理解
  }
  when(PF.al.raddr_ok){
    wait_br_addr_ok:=false.B
  }

  PF.to_if.bits.pc    :=pf_pc
  PF.to_if.bits.nextpc:=pf_nextpc
}

