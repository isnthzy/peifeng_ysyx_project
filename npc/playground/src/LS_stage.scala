import chisel3._
import chisel3.util._
import config.Configs._

class LS_stage extends Module {
  val LS=IO(new Bundle {
    val IO    =Input(new ex_to_ls_bus())
    val to_wb =Output(new ls_to_wb_bus())
  })
  LS.to_wb.wb_sel:=LS.IO.wb_sel
  LS.to_wb.wen:=LS.IO.wen
  LS.to_wb.waddr:=LS.IO.waddr
  LS.to_wb.result:=LS.IO.result
  LS.to_wb.pc:=LS.IO.pc
  LS.to_wb.inst:=LS.IO.inst
}