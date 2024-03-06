import chisel3._
import chisel3.util._
import config.Configs._
import Control._

class LS_stage extends Module {
  val LS=IO(new Bundle {
    // val IO    =Input(new ex_to_ls_bus())
    val IO    =Flipped(Decoupled(new ex_to_ls_bus()))
    val to_wb =Decoupled(new ls_to_wb_bus())

    val to_id =Output(new ls_to_id_bus())
    val r=Flipped(Decoupled(new AxiReadDataBundle()))
  })
  dontTouch(LS.r);
  val data_sram_rdata=dontTouch(WireDefault(0.U(DATA_WIDTH.W)))
  val rdata_valid=dontTouch(Wire(Bool()))
  val ls_clog=dontTouch(Wire(Bool()))
  ls_clog:= ~LS.IO.bits.ld_wen || (LS.IO.bits.ld_wen && rdata_valid)

  val ls_valid=dontTouch(RegInit(false.B))
  val ls_ready_go=dontTouch(Wire(Bool()))
  ls_ready_go:=Mux(ls_clog,true.B,false.B)
  LS.IO.ready := !ls_valid || ls_ready_go &&LS.to_wb.ready
  when(LS.IO.ready){
    ls_valid:=LS.IO.valid
  }
  LS.to_wb.valid:=ls_valid && ls_ready_go

//----------------------AXI4Lite  R Channel----------------------
  LS.r.ready:=true.B
  when(LS.r.fire){
    data_sram_rdata:=LS.r.bits.data
    rdata_valid:=true.B
  }.otherwise{
    rdata_valid:=false.B
  }
  
//----------------------AXI4Lite  R Channel----------------------

  val ram_data=dontTouch(Wire(UInt(32.W)))

  ram_data:=MuxLookup(LS.IO.bits.ld_type,0.U)(Seq(
    LD_LW -> data_sram_rdata,
    LD_LH -> Sext(data_sram_rdata(15,0),32),
    LD_LB -> Sext(data_sram_rdata( 7,0),32),
    LD_LHU-> Zext(data_sram_rdata(15,0),32),
    LD_LBU-> Zext(data_sram_rdata( 7,0),32)
  ))
  

  LS.to_wb.bits.csr_cmd:=LS.IO.bits.csr_cmd
  LS.to_wb.bits.rf_wen :=LS.IO.bits.rf_wen
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
  LS.to_id.clog:=ls_valid && !ls_ready_go && LS.IO.bits.ld_wen

  LS.to_id.fw.addr:=Mux(ls_valid && LS.IO.bits.rf_wen, LS.IO.bits.rd , 0.U)
  LS.to_id.fw.data:=LS.to_wb.bits.result

  /*---------------------传递信号到wb级再由wb级处理dpi信号----------------------*/
  LS.to_wb.bits.csr_commit<>LS.IO.bits.csr_commit
  LS.to_wb.bits.dpic_bundle.id<>LS.IO.bits.dpic_bundle.id
  LS.to_wb.bits.dpic_bundle.ex<>LS.IO.bits.dpic_bundle.ex
  LS.to_wb.bits.dpic_bundle.ls.ld_data:=LS.to_wb.bits.result
}

