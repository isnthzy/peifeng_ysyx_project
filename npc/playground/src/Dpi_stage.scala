import chisel3._
import chisel3.util._
import config.Configs._
import Control._

/* 
  ！！！！
  本级别不参与实际流水，仅提供IO口用于进行dpi与仿真环境交互  
  ！！！！
  */

class DPI_stage extends Module {
  val DPI=IO(new Bundle {
    val wb_valid=Input(Bool())
    val pc=Input(UInt(ADDR_WIDTH.W))
    val nextpc=Input(UInt(ADDR_WIDTH.W))
    val inst=Input(UInt(32.W))
    val inv_flag=Input(Bool())
    val func_flag=Input(Bool())
    val is_jal=Input(Bool())
    val is_ret=Input(Bool())
    val is_rd0=Input(Bool())
    val is_ebreak=Input(Bool())
    val ret_reg_data=Input(Bool())
    val csr_commit=Input(new commit_csr_to_diff())

    val ld_type=Input(UInt(3.W))
    val st_type=Input(UInt(4.W))
    val mem_addr=Input(UInt(ADDR_WIDTH.W))
    val st_data=Input(UInt(DATA_WIDTH.W))
    val ld_data=Input(UInt(DATA_WIDTH.W))
  })
  val dpi_getinfo=Module(new Dpi_GetInfo())
  dpi_getinfo.io.clock:=clock
  dpi_getinfo.io.reset:=reset
  dpi_getinfo.io.dpi_valid:=DPI.wb_valid
  dpi_getinfo.io.pc:=DPI.pc
  dpi_getinfo.io.nextpc:=DPI.nextpc
  dpi_getinfo.io.inst:=DPI.inst

  val dpi_inv=Module(new dpi_inv())
  dpi_inv.io.clock:=clock
  dpi_inv.io.reset:=reset
  dpi_inv.io.dpi_valid:=DPI.wb_valid
  dpi_inv.io.inv_flag:=DPI.inv_flag
  dpi_inv.io.pc:=DPI.pc

  val dpi_func=Module(new Dpi_Func())
  dpi_func.io.clock:=clock
  dpi_func.io.reset:=reset
  dpi_func.io.dpi_valid:=DPI.wb_valid 
  dpi_func.io.func_flag:=DPI.func_flag
  dpi_func.io.is_jal:=DPI.is_jal
  dpi_func.io.pc:=DPI.pc
  dpi_func.io.nextpc:=DPI.nextpc
  dpi_func.io.is_ret:=DPI.is_ret
  dpi_func.io.is_rd0:=DPI.is_rd0

  val dpi_ebreak=Module(new Dpi_Ebreak())
  dpi_ebreak.io.clock:=clock
  dpi_ebreak.io.reset:=reset  
  dpi_ebreak.io.dpi_valid:=DPI.wb_valid
  dpi_ebreak.io.is_ebreak:=DPI.is_ebreak
  dpi_ebreak.io.pc:=DPI.pc
  dpi_ebreak.io.ret_reg_data:=DPI.ret_reg_data

  val dpi_csrcommit=Module(new Dpi_CsrCommit())
  dpi_csrcommit.io.clock:=clock
  dpi_csrcommit.io.reset:=reset
  dpi_csrcommit.io.dpi_valid:=DPI.wb_valid
  dpi_csrcommit.io.csr_wen:=DPI.csr_commit.wen
  dpi_csrcommit.io.waddr:=DPI.csr_commit.waddr
  dpi_csrcommit.io.wdata:=DPI.csr_commit.wdata
  dpi_csrcommit.io.exception_wen:=DPI.csr_commit.exception.wen
  dpi_csrcommit.io.mcause_in:=DPI.csr_commit.exception.mcause_in
  dpi_csrcommit.io.pc_wb:=DPI.csr_commit.exception.pc_wb

  val dpi_mtrace=Module(new Dpi_Mtrace())
  dpi_mtrace.io.clock:=clock
  dpi_mtrace.io.reset:=reset
  dpi_mtrace.io.dpi_valid:=DPI.wb_valid
  dpi_mtrace.io.ld_wen:=DPI.ld_type=/=0.U
  dpi_mtrace.io.st_wen:=DPI.st_type=/=0.U
  dpi_mtrace.io.ld_len:=MuxLookup(DPI.ld_type,0.U)(Seq(
    LD_XXX -> 0.U,
    LD_LB  -> 1.U,
    LD_LH  -> 2.U,
    LD_LW  -> 4.U
  ))
  dpi_mtrace.io.st_len:=MuxLookup(DPI.st_type,0.U)(Seq(
    ST_XXX -> 0.U,
    ST_SB  -> 1.U,
    ST_SH  -> 2.U,
    ST_SW  -> 4.U
  ))
  dpi_mtrace.io.st_len:=DPI.st_type
  dpi_mtrace.io.mem_addr:=DPI.mem_addr
  dpi_mtrace.io.st_data:=DPI.st_data
  dpi_mtrace.io.ld_data:=DPI.ld_data
  
}



class Dpi_GetInfo extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val dpi_valid=Input(Bool())
    val pc      =Input(UInt(ADDR_WIDTH.W))
    val nextpc  =Input(UInt(ADDR_WIDTH.W))
    val inst    =Input(UInt(32.W))
  })
  setInline("dpic/DpiGetInfo.v",
    """
      |import "DPI-C" function void get_info(input int pc,input int nextpc,input int inst,input bit dpi_valid);
      |module Dpi_GetInfo(
      |    input        clock,
      |    input        reset,
      |    input        dpi_valid,
      |    input [31:0] pc,
      |    input [31:0] nextpc,
      |    input [31:0] inst
      |    
      |);
      | always @(*)begin
      |   if(~reset)begin
      |     get_info(pc,nextpc,inst,dpi_valid);
      |     //有可能因为阻塞等传递了无效的数据，需要在仿真环境中处理这些情况
      |   end
      |  end
      |endmodule
    """.stripMargin)
}  

class dpi_inv extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val dpi_valid=Input(Bool())
    val inv_flag=Input(Bool())
    val pc      =Input(UInt(ADDR_WIDTH.W))
  })
  setInline("dpic/DpiInv.v",
    """
      |import "DPI-C" function void inv_break(input int pc);
      |module dpi_inv(
      |    input        clock,
      |    input        reset,
      |    input        dpi_valid,
      |    input        inv_flag,
      |    input [31:0] pc
      |);
      | always @(posedge clock)begin
      |   if(~reset)begin
      |     if(inv_flag&&dpi_valid)  inv_break(pc);
      |   end
      |  end
      |endmodule
    """.stripMargin)
}  

class Dpi_Func extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val dpi_valid=Input(Bool())
    val func_flag=Input(Bool())
    val is_jal   =Input(Bool())
    val pc       =Input(UInt(ADDR_WIDTH.W))
    val nextpc   =Input(UInt(ADDR_WIDTH.W))
    val is_rd0   =Input(Bool())
    val is_ret   =Input(Bool())
  })
  setInline("dpic/DpiFunc.v",
    """
      |import "DPI-C" function void cpu_use_func(input int pc,input int nextpc,input bit is_ret,input bit is_jal,input bit is_rd0);
      |module Dpi_Func(
      |    input        clock,
      |    input        reset,
      |    input        dpi_valid,
      |    input        func_flag,
      |    input        is_jal,
      |    input [31:0] pc,
      |    input [31:0] nextpc,
      |    input        is_rd0,
      |    input        is_ret
      |);
      | always @(posedge clock)begin
      |   if(~reset)begin
      |     if(func_flag&&dpi_valid)   cpu_use_func(pc,nextpc,is_ret,is_jal,is_rd0);
      |   end
      |  end
      |endmodule
    """.stripMargin)
}


class Dpi_Ebreak extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val dpi_valid=Input(Bool())
    val is_ebreak=Input(Bool())
    val pc       =Input(UInt(ADDR_WIDTH.W))
    val ret_reg_data=Input(UInt(ADDR_WIDTH.W))
  })
  setInline("dpic/DpiEbreak.v",
    """
      |import "DPI-C" function void sim_break(input int pc,input int ret_reg_data);
      |module Dpi_Ebreak(
      |    input        clock,
      |    input        reset,
      |    input        dpi_valid,
      |    input        is_ebreak,
      |    input [31:0] pc,
      |    input [31:0] ret_reg_data
      |);
      | always @(posedge clock)begin
      |   if(~reset)begin
      |     if(is_ebreak&&dpi_valid)  sim_break(pc,ret_reg_data);
      |   end
      |  end
      |endmodule
    """.stripMargin)
}


class Dpi_CsrCommit extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val dpi_valid=Input(Bool())
    val csr_wen=Input(Bool())
    val waddr=Input(UInt(32.W))
    val wdata=Input(UInt(DATA_WIDTH.W))
    val exception_wen=Input(Bool())
    val mcause_in=Input(UInt(DATA_WIDTH.W))
    val pc_wb=Input(UInt(DATA_WIDTH.W))
  })
  setInline("dpic/DpiCsrCommit.v",
    """
      |import "DPI-C" function void sync_csrfile_regs(input int waddr,input int wdata);
      |import "DPI-C" function void sync_csr_exception_regs(input int mcause_in,input int pc_wb);
      |module Dpi_CsrCommit(
      |    input        clock,
      |    input        reset,
      |    input        dpi_valid,
      |    input        csr_wen,
      |    input [31:0] waddr,
      |    input [31:0] wdata,
      |    input        exception_wen,
      |    input [31:0] mcause_in,
      |    input [31:0] pc_wb
      |);
      | always @(posedge clock)begin
      |   if(~reset)begin
      |     if(csr_wen&&dpi_valid) sync_csrfile_regs(waddr,wdata);
      |     if(exception_wen&&dpi_valid) sync_csr_exception_regs(mcause_in,pc_wb);
      |   end
      |  end
      |endmodule
    """.stripMargin)
}


class Dpi_Mtrace extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val dpi_valid=Input(Bool())
    val pc    =Input(UInt(ADDR_WIDTH.W))
    val ld_wen=Input(Bool())
    val st_wen=Input(Bool())
    val ld_len=Input(UInt(3.W))
    val st_len=Input(UInt(4.W))
    val mem_addr=Input(UInt(ADDR_WIDTH.W))
    val st_data=Input(UInt(DATA_WIDTH.W))
    val ld_data=Input(UInt(DATA_WIDTH.W))
  })
  setInline("dpic/DpiMtrace.v",
    """
      |import "DPI-C" function void mtrace_store(input int pc,input int addr,input int data,input int len);
      |import "DPI-C" function void mtrace_load (input int pc,input int addr,input int data,input int len);
      |module Dpi_Mtrace(
      |    input        clock,
      |    input        reset,
      |    input        dpi_valid,
      |    input [31:0] pc,
      |    input        ld_wen,
      |    input        st_wen,
      |    input [ 4:0] ld_len,
      |    input [ 4:0] st_len,
      |    input [31:0] mem_addr,
      |    input [31:0] st_data,
      |    input [31:0] ld_data
      |);
      | always @(posedge clock)begin
      |   if(~reset)begin
      |     if(ld_wen&&dpi_valid) mtrace_load (pc,mem_addr,ld_data,ld_len);
      |     if(st_wen&&dpi_valid) mtrace_store(pc,mem_addr,st_data,st_len);
      |   end
      |  end
      |endmodule
    """.stripMargin)
}