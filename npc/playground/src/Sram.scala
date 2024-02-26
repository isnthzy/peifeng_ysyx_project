import chisel3._
import chisel3.util._
import config.Configs._
import Control._
class Sram extends Module {
  val io=IO(new Bundle {
    val in=Input(new data_sram_ex_bus())
    val out=Output(new data_sram_ls_bus())
  })
  //添加一个sram抽象层，为来再此进行抽象
  val dpi_sram=Module(new dpi_sram())
  dpi_sram.io.clock:=clock
  dpi_sram.io.addr:=io.in.addr
  dpi_sram.io.wdata:=io.in.wdata
  dpi_sram.io.wmask:=io.in.wmask
  dpi_sram.io.ena:=io.in.ld_wen&&io.in.st_wen&& ~reset.asBool
  dpi_sram.io.wen:=io.in.st_wen
  io.out.rdata:=dpi_sram.io.rdata
  io.out.rdata_ok:=true.B
  io.out.wdata_ok:=true.B
}

class dpi_sram extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val addr=Input(UInt(ADDR_WIDTH.W))
    val wdata=Input(UInt(DATA_WIDTH.W))
    val wmask=Input(UInt(8.W))
    val ena=Input(Bool())
    val wen=Input(Bool())
    val rdata=Output(UInt(DATA_WIDTH.W))
  })
  setInline("dpic/DpiSram.v",
    """
      |import "DPI-C" function void pmem_read (input int raddr, output int rdata);
      |import "DPI-C" function void pmem_write(input int waddr, input  int wdata, input byte wmask);
      |module dpi_sram(
      |   input        clock,
      |   input [31:0] addr,
      |   input [31:0] wdata,
      |   input [ 7:0] wmask,
      |   input        ena,
      |   input        wen,
      |   Output [31:0] rdata
      |);
      | 
      |always @(posedge clock) begin
      |    if(ena) begin
      |      pmem_read (addr,rdata);
      |      if(wen) begin
      |        pmem_write (addr,wdata,wmask);
      |      end
      |    end
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