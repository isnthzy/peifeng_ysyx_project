import chisel3._
import chisel3.util._
import config.Configs._
import Control._

class LS_stage extends Module {
  val LS=IO(new Bundle {
    val IO    =Input(new ex_to_ls_bus())
    val to_wb =Output(new ls_to_wb_bus())
  })
  val ram_data=dontTouch(Wire(UInt(32.W)))
  val dpi_ls=Module(new dpi_ls())
  dpi_ls.io.clock:=clock
  dpi_ls.io.reset:=reset
  dpi_ls.io.ld_wen:=LS.IO.ld_type.asUInt=/=0.U
  dpi_ls.io.st_wen:=LS.IO.st_type.asUInt=/=0.U
  dpi_ls.io.raddr:=LS.IO.result
  dpi_ls.io.wmask:=LS.IO.st_type
  dpi_ls.io.waddr:=LS.IO.result
  dpi_ls.io.wdata:=LS.IO.rdata2

  ram_data:=MuxLookup(LS.IO.ld_type,0.U)(Seq(
    LD_LW -> dpi_ls.io.rdata,
    LD_LH -> Sext(dpi_ls.io.rdata(15,0),32),
    LD_LB -> Sext(dpi_ls.io.rdata( 7,0),32),
    LD_LHU-> Zext(dpi_ls.io.rdata(15,0),32),
    LD_LBU-> Zext(dpi_ls.io.rdata( 7,0),32)
  ))
  
  LS.to_wb.pc_sel:=LS.IO.pc_sel
  LS.to_wb.csr_addr:=LS.IO.csr_addr
  LS.to_wb.csr_cmd :=LS.IO.csr_cmd
  LS.to_wb.rs1_addr:=LS.IO.rs1_addr
  //csr
  LS.to_wb.ebreak_flag:=LS.IO.ebreak_flag
  LS.to_wb.wen:=LS.IO.wen
  LS.to_wb.rd :=LS.IO.rd
  LS.to_wb.result:=MuxLookup(LS.IO.wb_sel,0.U)(Seq(
    WB_ALU ->  LS.IO.result,
    WB_MEM ->  ram_data,
    WB_PC4 -> (LS.IO.pc+4.U),
    WB_CSR ->  LS.IO.result
  ))
  LS.to_wb.pc:=LS.IO.pc
  LS.to_wb.inst:=LS.IO.inst
  LS.to_wb.nextpc:=LS.IO.nextpc


}

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
//       |import "DPI-C" function void pmem_read (input int raddr, output int rdata);
//       |import "DPI-C" function void pmem_write(input int waddr, input  int wdata, input byte wmask);
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
//       | 
//       |always_latch @(*) begin
//       |  if(~reset)begin
//       |    if(ld_wen&&clock) begin
//       |      pmem_read (raddr,rdata);
//       |    end
//       |    else begin
//       |      rdata[31:0]=0;
//       |    end
//       |
//       |    if(st_wen&&clock) begin
//       |      pmem_write(waddr,wdata,wmask);
//       |    end
//       |  end
//       | end
//       |endmodule
//     """.stripMargin)
// }


//yosys_test
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
  })
  setInline("dpi_ls.v",
    """
      |module dpi_ls(
      |   input        clock,
      |   input        reset,
      |   input        ld_wen,
      |   input        st_wen,
      |   input [31:0] raddr,
      |   output[31:0] rdata,
      |   input [ 7:0] wmask,
      |   input [31:0] waddr,
      |   input [31:0] wdata
      |);
      |reg [31:0] mem2[255:0];
      |always_latch @(*) begin
      |  if(~reset)begin
      |    if(ld_wen&&clock) begin
      |      rdata[31:0]<=mem2[raddr];
      |    end
      |    else begin
      |      rdata[31:0]=0;
      |    end
      |
      |    if(st_wen&&clock) begin
      |      mem2[waddr]<=wdata;
      |    end
      |  end
      | end
      |endmodule
    """.stripMargin)
}