package PipeLine

import chisel3._
import chisel3.util._
import Bundles.Difftest._
import Bundles._

class WbStage extends Module {
  val wb=IO(new Bundle {
    val in=Flipped(Decoupled(new Ls2WbBusBundle()))
    val fw_id=Output(new Id4WbBusBundle())

    val diffInstrCommit=Output(new DiffInstrBundle())
    val diffExcpCommit =Output(new DiffExcpBundle())
    val diffStoreCommit=Output(new DiffStoreBundle())
    val diffLoadCommit =Output(new DiffLoadBundle())
  })
  val wbValid=dontTouch(Wire(Bool()))
  val wbValidR=RegInit(false.B)
  val wbReadyGo=dontTouch(Wire(Bool()))
  val wbStall=dontTouch(Wire(Bool()))
  wb.in.ready:= !wbValid || wbReadyGo
  when(wb.in.ready){
    wbValidR:=true.B
  }
  wbValid:=wbValidR
  wbReadyGo:=true.B
  val realValid=wbValid && ~wb.in.bits.excpEn

  wb.fw_id.rf.waddr:=wb.in.bits.rd
  wb.fw_id.rf.wdata:=wb.in.bits.result
  wb.fw_id.rf.wen  :=wb.in.bits.rfWen&&wbValid

  wb.diffInstrCommit.index:=0.U
  wb.diffInstrCommit.valid:=realValid
  wb.diffInstrCommit.pc   :=wb.in.bits.pc
  wb.diffInstrCommit.instr:=wb.in.bits.inst
  wb.diffInstrCommit.skip :=false.B
  wb.diffInstrCommit.wen  :=wb.in.bits.rfWen&&wbValid
  wb.diffInstrCommit.wdest:=wb.in.bits.rd
  wb.diffInstrCommit.wdata:=wb.in.bits.result
  wb.diffInstrCommit.csrRstat:=false.B
  wb.diffInstrCommit.csrData :=0.asUInt
  
  wb.diffStoreCommit:=wb.in.bits.diffStore
  wb.diffLoadCommit :=wb.in.bits.diffLoad
  wb.diffExcpCommit :=wb.in.bits.diffExcp
}