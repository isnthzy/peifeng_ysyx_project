import chisel3._
import chisel3.util._
import config.Configs._
import Control._

class WB_stage extends Module {
  val WB=IO(new Bundle {
    // val IO    =Input(new ls_to_wb_bus())
    val IO    =Flipped(Decoupled(new ls_to_wb_bus()))
    val to_id =Output(new wb_to_id_bus())
    val to_if =Output(new wb_to_if_bus())
    val debug_waddr=Output(UInt(5.W))
    val debug_wdata=Output(UInt(DATA_WIDTH.W))
    val debug_wen  =Output(Bool())
  })

  val wb_valid=dontTouch(RegInit(false.B))
  val wb_ready_go=dontTouch(Wire(Bool()))
  wb_ready_go:=true.B
  WB.IO.ready := !wb_valid || wb_ready_go 
  when(WB.IO.ready){
    wb_valid:=WB.IO.valid
  }


  val Csrfile=Module(new CsrFile())
  Csrfile.io.csr_cmd:=WB.IO.bits.csr_cmd
  Csrfile.io.pc:=WB.IO.bits.pc
  Csrfile.io.csr_addr:=WB.IO.bits.csr_addr
  Csrfile.io.rs1_addr:=WB.IO.bits.rs1_addr
  Csrfile.io.in:=WB.IO.bits.result
  WB.to_if.epc_wen:=(WB.IO.bits.pc_sel===PC_EPC)
  WB.to_if.csr_epc:=Csrfile.io.epc

  WB.to_id.waddr:=WB.IO.bits.rd
  WB.to_id.wdata:=Mux(Csrfile.io.out_wen,Csrfile.io.out,WB.IO.bits.result)
  //如果是csr写入寄存器操作，相应的都要修改成csr寄存器的值
  WB.to_id.wen  :=WB.IO.bits.wen&&wb_valid
  WB.debug_waddr:=WB.to_id.waddr
  WB.debug_wdata:=WB.to_id.wdata
  WB.debug_wen  :=WB.to_id.wen



  val DPI_stage=Module(new DPI_stage())
  DPI_stage.DPI.wb_valid:=wb_valid
  DPI_stage.DPI.pc:=WB.IO.bits.pc
  DPI_stage.DPI.nextpc:=WB.IO.bits.nextpc
  DPI_stage.DPI.inst:=WB.IO.bits.inst
  DPI_stage.DPI.inv_flag:=WB.IO.bits.dpic_bundle.id_inv_flag
  DPI_stage.DPI.func_flag:=WB.IO.bits.dpic_bundle.ex_func_flag
  DPI_stage.DPI.is_jal:=WB.IO.bits.dpic_bundle.ex_is_jal
  DPI_stage.DPI.is_ret:=WB.IO.bits.dpic_bundle.ex_is_ret
  DPI_stage.DPI.is_rd0:=WB.IO.bits.dpic_bundle.ex_is_rd0
  DPI_stage.DPI.is_ebreak:=WB.IO.bits.ebreak_flag
  DPI_stage.DPI.ret_reg_data:=WB.to_id.wdata

}


