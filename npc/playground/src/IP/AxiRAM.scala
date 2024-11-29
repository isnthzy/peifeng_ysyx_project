package IP
//NOTE:用于DPIC与mem交互

import chisel3._
import chisel3.util._
import CoreConfig.Configs._
import FuncUnit.Control._
import Axi.Axi4Slave
import CoreConfig.GenerateParams
import Util.RandomDelay
class Axi4FullSram extends Module {
  val io=IO(new Axi4Slave())
  dontTouch(io);
  if(GenerateParams.getParam("YOSYS_MODE").asInstanceOf[Boolean]){
    val dpi_sram=Module(new reg_sram())
    // io.ar.ready:=RandomDelay(true.B,15.U)
    io.ar.ready:=true.B
    dpi_sram.io.clock:=clock
    dpi_sram.io.addr:=Mux(io.ar.fire,io.ar.bits.addr,
                        Mux((io.aw.fire && io.w.fire),io.aw.bits.addr,0.U))
    dpi_sram.io.req:=io.ar.fire || (io.aw.fire && io.w.fire)
    dpi_sram.io.wr:=(io.aw.fire && io.w.fire)

    val readDataValidReg=RegInit(false.B)
    when(io.ar.fire){
      readDataValidReg:=true.B
    }.otherwise{
      readDataValidReg:=false.B
    }
    io.r.valid:=readDataValidReg
    io.r.bits:=0.U.asTypeOf(io.r.bits)
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
    io.b.bits:=0.U.asTypeOf(io.b.bits)
  }else{
    val r_idle :: r_respond :: Nil = Enum(2)
    val readState=RegInit(r_idle)
    val readAddrReg=RegInit(0.U(32.W))
    val readLenReg=RegInit(0.U(8.W))
    val burstReq=readState===r_respond&&readLenReg>0.U
    val readReq=io.ar.fire || burstReq

    val dpi_sram=Module(new dpi_sram())
    dpi_sram.io.clock:=clock
    dpi_sram.io.raddr:=Mux(io.ar.fire,io.ar.bits.addr,readAddrReg)
    dpi_sram.io.ren:=readReq
    io.ar.ready:=true.B
    io.ar.ready:=RandomDelay(true.B,15.U)
    io.r.valid:=RegNext(readReq)
    io.r.bits.last:=readLenReg===0.U
    io.r.bits.data:=dpi_sram.io.rdata
    io.r.bits.resp:=0.U
    io.r.bits.id:=0.U

    switch(readState){
      is(r_idle){
        when(io.ar.fire){
          readAddrReg:=Cat(io.ar.bits.addr(31,2),0.U(2.W))+4.U
          readLenReg:=io.ar.bits.len
          readState:=r_respond
        }
      }
      is(r_respond){
        when(io.r.fire){
          when(io.r.bits.last.asBool){
            readState:=r_idle
          }.otherwise{
            readLenReg:=readLenReg-1.U
            readAddrReg:=Cat(readAddrReg(31,2),0.U(2.W))+4.U
          }
        }
      }
    }


    val w_idle :: w_write :: w_respond :: Nil = Enum(3)
    val writeState=RegInit(w_idle)
    val writeAddrReg=RegInit(0.U(32.W))
    val writeLenReg=RegInit(0.U(8.W))
    val writeLenLast=writeLenReg===0.U
    dpi_sram.io.waddr:=Mux(io.aw.fire,io.aw.bits.addr,writeAddrReg)
    dpi_sram.io.wen:=io.w.fire
    dpi_sram.io.wdata:=io.w.bits.data
    dpi_sram.io.wmask:=io.w.bits.strb
    io.aw.ready:=true.B
    io.aw.ready:=RandomDelay(true.B,15.U)
    io.w.ready:=true.B
    io.b.valid:=writeState===w_respond
    io.b.bits.resp:=0.U
    io.b.bits.id:=0.U
    switch(writeState){
      is(w_idle){
        when(io.aw.fire&&io.w.fire){
          when(io.aw.bits.len===0.U){
            writeState:=w_respond
          }.otherwise{
            writeAddrReg:=io.aw.bits.addr
            writeLenReg:=io.aw.bits.len
            writeState:=w_write
          }
        }.elsewhen(io.aw.fire){
          writeAddrReg:=io.aw.bits.addr
          writeLenReg:=io.aw.bits.len
          writeState:=w_write
        }
      }
      is(w_write){
        when(io.w.fire){
          when(io.w.bits.last.asBool||writeLenLast){
            writeState:=w_respond
          }.otherwise{
            writeLenReg:=writeLenReg-1.U
            writeAddrReg:=writeAddrReg(31,2)+4.U
          }
        }
      }
      is(w_respond){
        when(io.b.fire){
          writeState:=w_idle
        }
      }
    }
  }

}

class dpi_sram extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val ren  =Input(Bool())
    val raddr=Input(UInt(ADDR_WIDTH.W))
    val rdata=Output(UInt(DATA_WIDTH.W))
    val wen  =Input(Bool())
    val waddr=Input(UInt(ADDR_WIDTH.W))
    val wdata=Input(UInt(DATA_WIDTH.W))
    val wmask=Input(UInt(8.W))
  })
  setInline("dpic/DpiSram.v",
    """
      |import "DPI-C" function  int pmem_read (input int raddr);
      |import "DPI-C" function void pmem_write(input int waddr, input  int wdata, input byte wmask);
      |module dpi_sram(
      |   input             clock,
      |   input             ren,
      |   input      [31:0] raddr,
      |   output reg [31:0] rdata,
      |   input             wen,
      |   input      [31:0] waddr,
      |   input      [31:0] wdata,
      |   input      [ 7:0] wmask
      |);
      | 
      |always @(posedge clock) begin
      |    if(wen) begin 
      |     pmem_write (waddr,wdata,wmask);
      |    end
      |    if(ren) begin
      |     rdata<=pmem_read (raddr);
      |    end
      |end
      |endmodule
    """.stripMargin)
}

class reg_sram extends Module {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val addr=Input(UInt(ADDR_WIDTH.W))
    val wdata=Input(UInt(DATA_WIDTH.W))
    val wmask=Input(UInt(4.W))
    val req=Input(Bool())
    val wr=Input(Bool())
    val rdata=Output(UInt(DATA_WIDTH.W))
  })
  val mem=SyncReadMem(1,Vec(4,UInt(8.W)))
  io.rdata:=0.U
  when(io.req){
    when(io.wr){
      mem.write(io.addr,io.wdata.asTypeOf(Vec(4,UInt(8.W))))
      // mem.write(0.U,io.wdata.asTypeOf(Vec(4,UInt(8.W))),io.wmask)
    }.otherwise{
      io.rdata:=mem.read(0.U).asTypeOf(UInt(DATA_WIDTH.W))
    }
  }
}
