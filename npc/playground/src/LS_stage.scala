import chisel3._
import chisel3.util._
import config.Configs._
import Control._

class LS_stage extends Module {
  val LS=IO(new Bundle {
    // val IO    =Input(new ex_to_ls_bus())
    val IO    =Flipped(Decoupled(new ex_to_ls_bus()))
    val bypass_id=Output(new forward_to_id_bus())
    val to_wb =Decoupled(new ls_to_wb_bus())
    val data_sram=Input(new data_sram_bus_ls())
  })
  val rdata_ok=dontTouch(Wire(Bool()))
  val wdata_ok=dontTouch(Wire(Bool()))
  rdata_ok:=LS.data_sram.rdata_ok
  wdata_ok:=LS.data_sram.wdata_ok

  val ls_valid=dontTouch(RegInit(false.B))
  val ls_ready_go=dontTouch(Wire(Bool()))
  ls_ready_go:=true.B
  //未来用rdata_ok或者wdata_ok信号控制ls_ready_go
  LS.IO.ready := !ls_valid || ls_ready_go &&LS.to_wb.ready
  when(LS.IO.ready){
    ls_valid:=LS.IO.valid
  }
  LS.to_wb.valid:=ls_valid && ls_ready_go

  val ram_data=dontTouch(Wire(UInt(32.W)))

  ram_data:=MuxLookup(LS.IO.bits.ld_type,0.U)(Seq(
    LD_LW -> LS.data_sram.rdata,
    LD_LH -> Sext(LS.data_sram.rdata(15,0),32),
    LD_LB -> Sext(LS.data_sram.rdata( 7,0),32),
    LD_LHU-> Zext(LS.data_sram.rdata(15,0),32),
    LD_LBU-> Zext(LS.data_sram.rdata( 7,0),32)
  ))
  
  LS.to_wb.bits.pc_sel:=LS.IO.bits.pc_sel
  LS.to_wb.bits.csr_addr:=LS.IO.bits.csr_addr
  LS.to_wb.bits.csr_cmd :=LS.IO.bits.csr_cmd
  LS.to_wb.bits.rs1_addr:=LS.IO.bits.rs1_addr
  //csr
  LS.to_wb.bits.ebreak_flag:=LS.IO.bits.ebreak_flag
  LS.to_wb.bits.wen:=LS.IO.bits.wen
  LS.to_wb.bits.rd :=LS.IO.bits.rd
  LS.to_wb.bits.result:=MuxLookup(LS.IO.bits.wb_sel,0.U)(Seq(
    WB_ALU ->  LS.IO.bits.result,
    WB_MEM ->  ram_data,
    WB_PC4 -> (LS.IO.bits.pc+4.U),
    WB_CSR ->  LS.IO.bits.result
  ))
  LS.to_wb.bits.pc:=LS.IO.bits.pc
  LS.to_wb.bits.inst:=LS.IO.bits.inst
  LS.to_wb.bits.nextpc:=LS.IO.bits.nextpc

  //前递
  LS.bypass_id.addr:=Mux(ls_valid && LS.to_wb.bits.wen , LS.to_wb.bits.rd , 0.U)
  LS.bypass_id.data:=LS.to_wb.bits.result

  /*---------------------传递信号到wb级再由wb级处理dpi信号----------------------*/
  LS.to_wb.bits.dpic_bundle<>LS.IO.bits.dpic_bundle

}

