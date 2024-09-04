package PipeLine

import chisel3._
import chisel3.util._
import Bundles._

class WbStage extends Module {
  val wb=IO(new Bundle {
    val in=Flipped(Decoupled(new Ls2WbBusBundle()))
    val fw_id=Output(new Id4WbBusBundle())
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

  wb.fw_id.rf.waddr:=wb.in.bits.rd
  wb.fw_id.rf.wdata:=wb.in.bits.result
  wb.fw_id.rf.wen  :=wb.in.bits.rfWen&&wbValid
}