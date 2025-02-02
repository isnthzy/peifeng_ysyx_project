package ErXCore
import chisel3._
import chisel3.util._

class PfStage extends ErXCoreModule {
  val pf=IO(new Bundle {
    val to_if=Decoupled(new Pf2IfBusBundle())

    val from_id=Input(new Pf4IdBusBundle())
    val from_ex=Input(new Pf4ExBusBundle())
    val from_ls=Input(new Pf4LsBusBundle())

    val csrEntries=Input(new CsrEntriesBundle)
    val al=new Core2AxiReadIO()
    val fenceI=Output(Bool())

    val perfMode=Input(Bool()) //飞线...
    val programExit=Input(Bool()) //为了perf的飞线
  })
  val pfFlush=dontTouch(Wire(Bool()))
  val pfExcpEn=dontTouch(Wire(Bool()))
  pfFlush:=(pf.from_id.brJump.taken||pf.from_ex.brCond.taken
          ||pf.from_ls.flush.asUInt.orR)
  val pfReadyGo=dontTouch(Wire(Bool()))
  val fetchReq=dontTouch(Wire(Bool()))
  val fenceICache=RegInit(false.B)
  pfReadyGo:=((pf.al.addrOk&& ~fenceICache)&& fetchReq)|| pfExcpEn
  pf.to_if.valid:=pfReadyGo
  fetchReq:= ~reset.asBool&& ~pfFlush && pf.to_if.ready && ~pfExcpEn

  // val regPC  = RegInit(if(GenerateParams.getParam("SOC_MODE").asInstanceOf[Boolean]) SOC_START_ADDR else NPC_START_ADDR)
  val regPC  = RegInit(NPC_START_ADDR)
  val snpc   = dontTouch(Wire(UInt(XLEN.W)))
  val dnpc   = dontTouch(Wire(UInt(XLEN.W)))
  val nextpc = dontTouch(Wire(UInt(XLEN.W)))

  val flush_sign=pf.from_ls.flush.asUInt.orR
  val flushed_pc=Mux(pf.from_ls.flush.excp,pf.csrEntries.mtvec,
                  Mux(pf.from_ls.flush.xret,pf.csrEntries.mepc,
                    Mux(pf.from_ls.flush.refetch,pf.from_ls.refetchPC,0.U)))
  snpc:=regPC + 4.U
  dnpc:=Mux(flush_sign,flushed_pc,
          Mux(pf.from_ex.brCond.taken,pf.from_ex.brCond.target,
            Mux(pf.from_id.brJump.taken,pf.from_id.brJump.target,0.U)))

  nextpc:=Mux(pfFlush,dnpc,snpc)

  when((pfReadyGo&&pf.to_if.ready)||pfFlush){
    regPC:=nextpc
  }


  pf.fenceI:=fenceICache
  when(pf.from_ex.fencei){
    fenceICache:=true.B
  }.elsewhen(pf.al.addrOk){
    fenceICache:=false.B
  }

  pf.al.req :=fetchReq
  pf.al.addr:=regPC
  pf.al.size:=2.U
//NOTE:excp
  val pfExcpType=Wire(new PfExcpTypeBundle())
  pfExcpType.iam:=(regPC(0)|regPC(1))
  pfExcpEn:=pfExcpType.asUInt.orR
  pf.to_if.bits.excpEn:=pfExcpEn
  pf.to_if.bits.excpType:=pfExcpType

  pf.to_if.bits.pc:=regPC

  // if(GenerateParams.getParam("PERF").asInstanceOf[Boolean]){
  //   val FetchAddrClockCnt=RegInit(0.U(64.W))
  //   val InstCnt=RegInit(0.U(64.W))
  //   when(pf.perfMode){
  //     when(fetchReq){
  //       FetchAddrClockCnt:=FetchAddrClockCnt+1.U
  //       when(pf.to_if.fire){
  //         InstCnt:=InstCnt+1.U
  //       }
  //     }
  //     when(pf.programExit){
  //       var CyclePerFetchAddrResp=(FetchAddrClockCnt.asSInt  * 100.asSInt) / InstCnt.asSInt
  //       printf("Cycle per fetch(addr resp)(%%): %d%%\n",CyclePerFetchAddrResp);
  //     }
  //   }
  // }
}