package PipeLine
import chisel3._
import chisel3.util._
import Axi._
import Bundles._
import config.Configs._

class PfStage extends Module {
  val pf=IO(new Bundle {
    val to_id=Decoupled(new If2IdBusBundle())

    val al=new AxiBridgeAddrLoad()
  })
  val pfFlush=dontTouch(Wire(Bool()))
  val pfReadyGo=dontTouch(Wire(Bool()))
  val fetchReq=dontTouch(Wire(Bool()))
  pfReadyGo:=pf.al.raddr_ok && fetchReq
  pf.to_id.valid:=pfReadyGo
  fetchReq:= ~reset.asBool&& ~pfFlush && pf.to_id.ready

  val regPC=RegInit(START_ADDR)
  val snpc   = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val dnpc   = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val nextpc = dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  snpc:=regPC

  nextpc:=Mux(pfFlush,dnpc,snpc)
  when(pfReadyGo||pfFlush){
    regPC:=nextpc
  }

  pf.al.ren  :=pfReadyGo
  pf.al.raddr:=regPC

  pf.to_id.bits.pc:=regPC
}