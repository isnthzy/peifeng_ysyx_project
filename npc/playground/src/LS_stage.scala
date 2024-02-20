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
    val clog_id=Output(Bool())
  })
  val rdata_ok=dontTouch(Wire(Bool()))
  val wdata_ok=dontTouch(Wire(Bool()))
  

  val ls_valid=dontTouch(RegInit(false.B))
  val ls_ready_go=dontTouch(Wire(Bool()))
  ls_ready_go:=Mux((LS.IO.bits.ld_type.asUInt=/=0.U)&&ls_valid&&(!rdata_ok),false.B,true.B)
  LS.IO.ready := !ls_valid || ls_ready_go &&LS.to_wb.ready
  when(LS.IO.ready){
    ls_valid:=LS.IO.valid
  }
  LS.to_wb.valid:=ls_valid && ls_ready_go

  LS.clog_id:=(LS.IO.bits.ld_type.asUInt=/=0.U)&&ls_valid&&(!rdata_ok)
  //当为load指令时，在ls级前递，但ls级还没有准备好数据，发起阻塞

  val ram_data=dontTouch(Wire(UInt(32.W)))
  val dpi_ls=Module(new dpi_ls())
  dpi_ls.io.clock:=clock
  dpi_ls.io.reset:=reset
  dpi_ls.io.ld_wen:=(LS.IO.bits.ld_type.asUInt=/=0.U)&&ls_valid
  dpi_ls.io.st_wen:=(LS.IO.bits.st_type.asUInt=/=0.U)&&ls_valid
  dpi_ls.io.raddr:=LS.IO.bits.result
  dpi_ls.io.wmask:=LS.IO.bits.st_type
  dpi_ls.io.waddr:=LS.IO.bits.result
  dpi_ls.io.wdata:=LS.IO.bits.rdata2
  rdata_ok:=dpi_ls.io.rdata_ok
  wdata_ok:=dpi_ls.io.wdata_ok

  ram_data:=MuxLookup(LS.IO.bits.ld_type,0.U)(Seq(
    LD_LW -> dpi_ls.io.rdata,
    LD_LH -> Sext(dpi_ls.io.rdata(15,0),32),
    LD_LB -> Sext(dpi_ls.io.rdata( 7,0),32),
    LD_LHU-> Zext(dpi_ls.io.rdata(15,0),32),
    LD_LBU-> Zext(dpi_ls.io.rdata( 7,0),32)
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
  // LS.to_wb.bits.dpic_bundle.id_inv_flag:=LS.IO.bits.dpic_bundle.id_inv_flag
  // LS.to_wb.bits.dpic_bundle.ex_func_flag:=LS.IO.bits.dpic_bundle.ex_func_flag
  // LS.to_wb.bits.dpic_bundle.ex_is_jal:=LS.IO.bits.dpic_bundle.ex_is_jal
  // LS.to_wb.bits.dpic_bundle.ex_is_ret:=LS.IO.bits.dpic_bundle.ex_is_ret
  // LS.to_wb.bits.dpic_bundle.ex_is_rd0:=LS.IO.bits.dpic_bundle.ex_is_rd0
}

class dpi_ls extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val ld_wen=Input(Bool())
    val st_wen=Input(Bool())
    val raddr =Input(UInt(32.W))
    val rdata =Output(UInt(32.W))
    val wmask =Input(UInt(8.W))
    val waddr =Input(UInt(32.W))
    val wdata =Input(UInt(32.W))
    val rdata_ok=Output(Bool())
    val wdata_ok=Output(Bool())
  })
  setInline("dpi_ls.v",
    """
      |import "DPI-C" function void pmem_read (input int raddr, output int rdata);
      |import "DPI-C" function void pmem_write(input int waddr, input  int wdata, input byte wmask);
      |module dpi_ls(
      |   input        clock,
      |   input        reset,
      |   input        ld_wen,
      |   input        st_wen,
      |   input [31:0] raddr,
      |   output[31:0] rdata,
      |   input [ 7:0] wmask,
      |   input [31:0] waddr,
      |   input [31:0] wdata,
      |   output reg rdata_ok,
      |   output reg wdata_ok
      |);
      | 
      |always @(posedge clock) begin
      |  if(~reset)begin
      |    if(ld_wen) begin
      |      pmem_read (raddr,rdata);
      |      rdata_ok<=1;
      |    end
      |    else begin
      |      rdata_ok<=0;
      |      rdata[31:0]=0;
      |    end
      |
      |    if(st_wen) begin
      |      pmem_write(waddr,wdata,wmask);
      |      wdata_ok<=1;
      |    end
      |    else begin
      |      wdata_ok<=0;
      |    end
      | end
      |end
      |endmodule
    """.stripMargin)
}


// //yosys_test
// class dpi_ls extends BlackBox with HasBlackBoxInline {
//   val io = IO(new Bundle {
//     val clock=Input(Clock())
//     val reset=Input(Bool())
//     val ld_wen=Input(Bool())
//     val st_wen=Input(Bool())
//     val raddr =Input(UInt(32.W))
//     val rdata =Output(UInt(32.W))
//     val wmask =Input(UInt(8.W))
//     val waddr =Input(UInt(32.W))
//     val wdata =Input(UInt(32.W))
//   })
//   setInline("dpi_ls.v",
//     """
//       |module dpi_ls(
//       |   input        clock,
//       |   input        reset,
//       |   input        ld_wen,
//       |   input        st_wen,
//       |   input [31:0] raddr,
//       |   output[31:0] rdata,
//       |   input [ 7:0] wmask,
//       |   input [31:0] waddr,
//       |   input [31:0] wdata
//       |);
//       |reg [31:0] mem2[255:0];
//       |always_latch @(*) begin
//       |  if(~reset)begin
//       |    if(ld_wen&&clock) begin
//       |      rdata[31:0]<=mem2[raddr];
//       |    end
//       |    else begin
//       |      rdata[31:0]=0;
//       |    end
//       |
//       |    if(st_wen&&clock) begin
//       |      mem2[waddr]<=wdata;
//       |    end
//       |  end
//       | end
//       |endmodule
//     """.stripMargin)
// }