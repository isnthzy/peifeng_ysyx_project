package ErXCore
import chisel3._
import chisel3.util._

class PfStage extends ErXCoreModule {
  val io=IO(new Bundle {
    val to_if=Decoupled(new Pf2IfBusBundle())
    val from_ib = Input(new Pf4IbBundle())
    val from_bck = Input(new FrontFromBack())
    // val from_id=Input(new Pf4IdBusBundle())
    // val from_ex=Input(new Pf4ExBusBundle())
    // val from_ls=Input(new Pf4LsBusBundle())

    // val csrEntries=Input(new CsrEntriesBundle)
    val al=new Core2AxiReadIO()
    val fenceI=Output(Bool())

  })
  val pfFlush=dontTouch(Wire(Bool()))
  val pfExcpEn=dontTouch(Wire(Bool()))
  pfFlush := io.from_bck.flush || io.from_bck.tk.taken || io.from_ib.br.taken
  val pfReadyGo=dontTouch(Wire(Bool()))
  val fetchReq=dontTouch(Wire(Bool()))
  val fenceICache=RegInit(false.B)
  pfReadyGo:=((io.al.addrOk&& ~fenceICache)&& fetchReq)|| pfExcpEn
  io.to_if.valid:=pfReadyGo
  fetchReq:= ~reset.asBool&& ~pfFlush && io.to_if.ready && ~pfExcpEn

  val regPC  = RegInit(if(GenerateParams.getParam("SOC_MODE").asInstanceOf[Boolean]) SOC_START_ADDR else NPC_START_ADDR)
  val snpc   = dontTouch(Wire(UInt(XLEN.W)))
  val dnpc   = dontTouch(Wire(UInt(XLEN.W)))
  val nextpc = dontTouch(Wire(UInt(XLEN.W)))

  // val flush_sign=io.from_ls.flush.asUInt.orR
  // val flushed_pc=Mux(io.from_ls.flush.excp,io.csrEntries.mtvec,
  //                 Mux(io.from_ls.flush.xret,io.csrEntries.mepc,
  //                   Mux(io.from_ls.flush.refetch,io.from_ls.refetchPC,0.U)))
  snpc:=regPC + 4.U
  // dnpc:=Mux(flush_sign,flushed_pc,
  //         Mux(io.from_ex.brCond.taken,io.from_ex.brCond.target,
  //           Mux(io.from_id.brJump.taken,io.from_id.brJump.target,0.U)))
  dnpc:=Mux(io.from_bck.tk.taken,io.from_bck.tk.target,
          Mux(io.from_ib.br.taken,io.from_ib.br.target,0.U))

  nextpc:=Mux(pfFlush,dnpc,snpc)

  when((pfReadyGo&&io.to_if.ready)||pfFlush){
    regPC:=nextpc
  }


  io.fenceI:=fenceICache
  // when(io.from_ex.fencei){ //TODO: fencei
  //   fenceICache:=true.B
  // }.elsewhen(io.al.addrOk){
  //   fenceICache:=false.B
  // }

  io.al.req :=fetchReq
  io.al.addr:=regPC
  io.al.size:=2.U
//NOTE:excp
  val pfExcpType=Wire(new PfExcpTypeBundle())
  pfExcpType.iam:=(regPC(0)|regPC(1))
  pfExcpEn:=pfExcpType.asUInt.orR
  io.to_if.bits.excpEn:=pfExcpEn
  io.to_if.bits.excpType:=pfExcpType

  io.to_if.bits.pc:=regPC
}