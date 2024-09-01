package PipeLine

import chisel3._
import chisel3.util._
import config.Configs._
import Bundles._
import Axi.AxiBridgeDataLoad
import Util.{Mux1hMap,Mux1hDefMap,Sext,Zext}
import FuncUnit.Control._

class LsStage extends Module {
  val ls=IO(new Bundle {
    val in=Flipped(Decoupled(new Ex2LsBusBundle()))
    val to_wb=Decoupled(new Ls2WbBusBundle())

    val fw_pf=Output(new Pf4LsBusBundle())
    val fw_if=Output(new If4LsBusBundle())
    val fw_id=Output(new Id4LsBusBundle())
    val fw_ex=Output(new Ex4LsBusBundle())

    val to_csr=Output(new Ls2CsrBundle())
    val dl=Output(new AxiBridgeDataLoad())
  })
  val lsValid=dontTouch(Wire(Bool()))
  val lsValidR=RegInit(false.B)
  val lsReadyGo=dontTouch(Wire(Bool()))
  val lsStall=dontTouch(Wire(Bool()))
  ls.in.ready:=ls.to_wb.ready&& ~lsValidR || lsReadyGo
  when(ls.in.ready){
    lsValidR:=ls.in.valid
  }
  lsValid:=lsValidR
  lsReadyGo:= ~lsStall
  ls.to_wb.valid:= lsValid&&lsReadyGo

  lsStall:=ls.in.bits.loadEn&& ~ls.dl.rdata_ok && lsValid

  val loadByteData=Mux1hMap(ls.in.bits.addrLow2Bit,Map(
    "b00".U -> ls.dl.rdata(7 , 0),
    "b01".U -> ls.dl.rdata(15, 8),
    "b10".U -> ls.dl.rdata(23,16),
    "b11".U -> ls.dl.rdata(31,24)
  ))
  val loadHalfData=Mux1hMap(ls.in.bits.addrLow2Bit,Map(
    "b00".U -> ls.dl.rdata(15, 0), 
    "b01".U -> ls.dl.rdata(15, 0),
    "b10".U -> ls.dl.rdata(31,16), 
    "b11".U -> ls.dl.rdata(31,16)
  ))
  val loadDataResult=Mux1hDefMap(ls.in.bits.ldType,Map(
    LD_LW -> ls.dl.rdata,
    LD_LH -> Sext(loadHalfData,32),
    LD_LB -> Sext(loadByteData,32),
    LD_LHU-> Zext(loadHalfData,32),
    LD_LBU-> Zext(loadByteData,32),
  ))

  val ls_result=Mux1hDefMap(ls.in.bits.wbSel,Map(
    WB_ALU -> ls.in.bits.result,
    WB_MEM -> loadDataResult,
  ))


  ls.fw_id.dataUnReady:=ls.in.bits.rfWen&& ~lsReadyGo
  ls.fw_id.rf.wen:=ls.in.bits.rfWen
  ls.fw_id.rf.waddr:=ls.in.bits.rd
  ls.fw_id.rf.wdata:=ls_result

  ls.to_csr.wen:=ls.in.bits.csrWen
  ls.to_csr.wrAddr:=ls.in.bits.csrWrAddr
  ls.to_csr.wrData:=ls.in.bits.csrWrData

  val refetchFlush=ls.in.bits.csrWen
  ls.fw_pf.flush.refetch:=refetchFlush
  ls.fw_pf.refetchPC:=ls.in.bits.pc+4.U
  ls.fw_if.flush:=refetchFlush
  ls.fw_id.flush:=refetchFlush
  ls.fw_ex.flush:=refetchFlush

//NOTE:
  ls.to_wb.bits.rd:=ls.in.bits.rd
  ls.to_wb.bits.result:=ls_result
  ls.to_wb.bits.rfWen:=ls.in.bits.rfWen
}