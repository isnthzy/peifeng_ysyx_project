package PipeLine
import chisel3._
import chisel3.util._
import Axi._
import Bundles._
import CoreConfig.Configs._

class PfStage extends Module {
  val pf=IO(new Bundle {
    val to_if=Decoupled(new Pf2IfBusBundle())

    val from_id=Input(new Pf4IdBusBundle())
    val from_ex=Input(new Pf4ExBusBundle())
    val from_ls=Input(new Pf4LsBusBundle())

    val csrEntries=Input(new CsrEntriesBundle)
    val al=new AxiBridgeAddrLoad()
    val s =new AxiBridgeStore()
  })
  val pfFlush=dontTouch(Wire(Bool()))
  val pfExcpEn=dontTouch(Wire(Bool()))
  pfFlush:=(pf.from_id.brJump.taken||pf.from_ex.brCond.taken
          ||pf.from_ls.flush.asUInt.orR)
  val pfReadyGo=dontTouch(Wire(Bool()))
  val fetchReq=dontTouch(Wire(Bool()))
  pfReadyGo:=(pf.al.raddr_ok && fetchReq)|| pfExcpEn
  pf.to_if.valid:=pfReadyGo
  fetchReq:= ~reset.asBool&& ~pfFlush && pf.to_if.ready && ~pfExcpEn

  val regPC  = RegInit(START_ADDR)
  val snpc   = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val dnpc   = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val nextpc = dontTouch(Wire(UInt(ADDR_WIDTH.W)))

  val flush_sign=pf.from_ls.flush.asUInt.orR
  val flushed_pc=Mux(pf.from_ls.flush.excp,pf.csrEntries.mtvec,
                  Mux(pf.from_ls.flush.xret,pf.csrEntries.mepc,
                    Mux(pf.from_ls.flush.refetch,pf.from_ls.refetchPC,0.U)))
  snpc:=regPC
  dnpc:=Mux(flush_sign,flushed_pc,
          Mux(pf.from_id.brJump.taken,pf.from_id.brJump.target,
            Mux(pf.from_ex.brCond.taken,pf.from_ex.brCond.target,0.U)))

  nextpc:=Mux(pfFlush,dnpc,snpc)
  when(pfReadyGo||pfFlush){
    regPC:=nextpc
  }

  pf.al.ren  :=fetchReq
  pf.al.raddr:=regPC
  pf.s.wen:=DontCare
  pf.s.waddr:=DontCare
  pf.s.wstrb:=DontCare
  pf.s.wdata:=DontCare
//NOTE:excp
  val pfExcpType=Wire(new PfExcpTypeBundle())
  pfExcpType.iam:=(regPC(0)|regPC(1))
  pfExcpEn:=pfExcpType.asUInt.orR
  pf.to_if.bits.excpEn:=pfExcpEn
  pf.to_if.bits.excpType:=pfExcpType

  pf.to_if.bits.pc:=regPC
}