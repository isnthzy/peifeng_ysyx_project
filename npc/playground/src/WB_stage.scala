import chisel3._
import chisel3.util._
import config.Configs._
import Control._

class WB_stage extends Module {
  val WB=IO(new Bundle {
    val IO    =Input(new ls_to_wb_bus())
    val to_id =Output(new wb_bus())
    val debug_waddr=Output(UInt(5.W))
    val debug_wdata=Output(UInt(DATA_WIDTH.W))
    val debug_wen  =Output(Bool())
  })
  WB.to_id.waddr:=WB.IO.waddr
  WB.to_id.wdata:=MuxLookup(WB.IO.wb_sel,0.U)(Seq(
    WB_ALU ->  WB.IO.result,
    WB_PC4 -> (WB.IO.pc+4.U)
  ))
  WB.to_id.wen  :=WB.IO.wen

  WB.debug_waddr:=WB.to_id.waddr
  WB.debug_wdata:=WB.to_id.wdata
  WB.debug_wen  :=WB.to_id.wen
}
