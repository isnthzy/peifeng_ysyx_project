import chisel3._
import chisel3.util._
import config.Configs._
import Control._
class Sram extends Module {
  val io=IO(new Bundle {
    val in=Input(new data_sram_bus_ex())
    val out=Output(new data_sram_bus_ls())

  })
  //添加一个sram抽象层，为来再此进行抽象
  val dpi_sram=Module(new dpi_sram())
  dpi_sram.io.clock:=clock
  dpi_sram.io.reset:=reset
  dpi_sram.io.ld_wen:=io.in.ld_wen
  dpi_sram.io.st_wen:=io.in.st_wen
  dpi_sram.io.raddr:=io.in.addr
  dpi_sram.io.wmask:=io.in.wmask
  dpi_sram.io.waddr:=io.in.addr
  dpi_sram.io.wdata:=io.in.wdata
  io.out.rdata:=dpi_sram.io.rdata
  io.out.rdata_ok:=dpi_sram.io.rdata_ok
  io.out.wdata_ok:=dpi_sram.io.wdata_ok

}

class dpi_sram extends BlackBox with HasBlackBoxInline {
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
  setInline("dpi_sram.v",
    """
      |import "DPI-C" function void pmem_read (input int raddr, output int rdata);
      |import "DPI-C" function void pmem_write(input int waddr, input  int wdata, input byte wmask);
      |module dpi_sram(
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