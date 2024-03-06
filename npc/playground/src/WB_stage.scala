import chisel3._
import chisel3.util._
import config.Configs._
import Control._

class WB_stage extends Module {
  val WB=IO(new Bundle {
    // val IO    =Input(new ls_to_wb_bus())
    val IO    =Flipped(Decoupled(new ls_to_wb_bus()))
    val to_id =Output(new wb_to_id_bus())
    
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


  WB.to_id.rf.waddr:=WB.IO.bits.rd
  WB.to_id.rf.wdata:=WB.IO.bits.result
  WB.to_id.rf.wen  :=WB.IO.bits.rf_wen&&wb_valid
  WB.debug_waddr:=WB.to_id.rf.waddr
  WB.debug_wdata:=WB.to_id.rf.wdata
  WB.debug_wen  :=WB.to_id.rf.wen



  val DPI_stage=Module(new DPI_stage())
  DPI_stage.DPI.wb_valid:=wb_valid //只需要知道wb_valid信号是否有效就知道这条信号用不用与difftest通信
  DPI_stage.DPI.pc:=WB.IO.bits.pc
  DPI_stage.DPI.nextpc:=WB.IO.bits.nextpc
  DPI_stage.DPI.inst:=WB.IO.bits.inst
  DPI_stage.DPI.inv_flag:=WB.IO.bits.dpic_bundle.id.inv_flag
  DPI_stage.DPI.func_flag:=WB.IO.bits.dpic_bundle.ex.func_flag
  DPI_stage.DPI.is_jal:=WB.IO.bits.dpic_bundle.ex.is_jal
  DPI_stage.DPI.is_ret:=WB.IO.bits.dpic_bundle.ex.is_ret
  DPI_stage.DPI.is_rd0:=WB.IO.bits.dpic_bundle.ex.is_rd0
  DPI_stage.DPI.is_ebreak:=(WB.IO.bits.csr_cmd===CSR.BREAK)
  DPI_stage.DPI.ret_reg_data:=WB.IO.bits.result
  DPI_stage.DPI.csr_commit<>WB.IO.bits.csr_commit

  DPI_stage.DPI.ld_type:=WB.IO.bits.dpic_bundle.ex.ld_type
  DPI_stage.DPI.st_type:=WB.IO.bits.dpic_bundle.ex.st_type
  DPI_stage.DPI.mem_addr:=WB.IO.bits.dpic_bundle.ex.mem_addr
  DPI_stage.DPI.st_data:=WB.IO.bits.dpic_bundle.ex.st_data
  DPI_stage.DPI.ld_data:=WB.IO.bits.dpic_bundle.ls.ld_data
  
}


