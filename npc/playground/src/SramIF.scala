//为了验证方便，添加两个sram
//一个用于取指，另一个用于访存
import chisel3._
import chisel3.util._
import config.Configs._
import Control._
class Axi4Lite_Sram_If extends Module {
  val io=IO(new Axi4LiteSlave())
  val dpi_sram=Module(new dpi_sram())
  
  dontTouch(io);
  
  // io.ar.ready:=RandomDelay(true.B,3.U)
  io.ar.ready:=true.B
  dpi_sram.io.clock:=clock
  dpi_sram.io.addr:=io.ar.bits.addr
  dpi_sram.io.req:=io.ar.fire
  dpi_sram.io.wr:=(io.aw.fire && io.w.fire)

  val readDataValidReg=RegInit(false.B)
  when(io.ar.fire){
    readDataValidReg:=true.B
  }.otherwise{
    readDataValidReg:=false.B
  }
  io.r.valid:=readDataValidReg

  io.r.bits.resp:=0.U
  io.r.bits.data:=dpi_sram.io.rdata

  io.aw.ready:=true.B
  io.w.ready:=true.B
  dpi_sram.io.wdata:=io.w.bits.data
  dpi_sram.io.wmask:=io.w.bits.strb

  val writeRespValidReg=RegInit(false.B)
  when(io.aw.fire&&io.w.fire){
    writeRespValidReg:=true.B
  }.otherwise{
    writeRespValidReg:=false.B
  }
  io.b.valid:=writeRespValidReg
  io.b.bits.resp:=0.U


}

class dpi_sram_if extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val addr=Input(UInt(ADDR_WIDTH.W))
    val wdata=Input(UInt(DATA_WIDTH.W))
    val wmask=Input(UInt(8.W))
    val req=Input(Bool())
    val wr=Input(Bool())
    val rdata=Output(UInt(DATA_WIDTH.W))
  })
  setInline("dpic/DpiSramIF.v",
    """
      |import "DPI-C" function void pmem_read (input int raddr, output int rdata);
      |import "DPI-C" function void pmem_write(input int waddr, input  int wdata, input byte wmask);
      |module dpi_sram_if(
      |   input        clock,
      |   input [31:0] addr,
      |   input [31:0] wdata,
      |   input [ 7:0] wmask,
      |   input        req,
      |   input        wr,
      |   output reg [31:0] rdata
      |);
      | 
      |always @(posedge clock) begin
      |    if(req) begin
      |      if(wr) begin 
      |       pmem_write (addr,wdata,wmask);
      |      end
      |      else begin
      |       pmem_read (addr,rdata);
      |      end
      |    end
      |end
      |endmodule
    """.stripMargin)
}

