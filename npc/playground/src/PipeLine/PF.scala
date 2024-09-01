package PipeLine
import chisel3._
import chisel3.util._
import Axi._
import Bundles._
import config.Configs._

class PfStage extends Module {
  val pf=IO(new Bundle {
    val to_if=Decoupled(new If2IdBusBundle())

    val from_id=Input(new Pf4IdBusBundle())
    val from_ex=Input(new Pf4ExBusBundle())
    val from_ls=Input(new Pf4LsBusBundle())

    val csrEntries=Input(new CsrEntriesBundle)
    val al=new AxiBridgeAddrLoad()
    val s =new AxiBridgeStore()
  })
  val pfFlush=dontTouch(Wire(Bool()))
  pfFlush:=(pf.from_id.brJump.taken||pf.from_ex.brCond.taken
          ||pf.from_ls.flush.asUInt.orR)
  val pfReadyGo=dontTouch(Wire(Bool()))
  val fetchReq=dontTouch(Wire(Bool()))
  pfReadyGo:=pf.al.raddr_ok && fetchReq
  pf.to_if.valid:=pfReadyGo
  fetchReq:= ~reset.asBool&& ~pfFlush && pf.to_if.ready

  val regPC=RegInit(START_ADDR)
  val snpc   = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val dnpc   = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val nextpc = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  snpc:=regPC
  dnpc:=Mux(pf.from_id.brJump.taken,pf.from_id.brJump.target,
          Mux(pf.from_ex.brCond.taken,pf.from_ex.brCond.target,0.U))

  nextpc:=Mux(pfFlush,dnpc,snpc)
  when(pfReadyGo||pfFlush){
    regPC:=nextpc
  }

  pf.al.ren  :=pfReadyGo
  pf.al.raddr:=regPC

  pf.to_if.bits.pc:=regPC
}