import chisel3._
import chisel3.util._
import config.Configs._


/* 
  ！！！！
  本级别不参与实际流水，仅提供IO口用于进行dpi与仿真环境交互  
  ！！！！
  */

class DPI_stage extends Module {
  val DPI=IO(new Bundle {
    val wb_valid=Input(Bool())
    val pc=Input(UInt(32.W))
    val nextpc=Input(UInt(32.W))
    val inv_flag=Input(Bool())
    val func_flag=Input(Bool())
    val is_jal=Input(Bool())
    val is_ret=Input(Bool())
    val is_rd0=Input(Bool())
    val is_ebreak=Input(Bool())
    val ret_reg_data=Input(Bool())
  })
  val dpi_getpc=Module(new dpi_getpc())
  dpi_getpc.io.clock:=clock
  dpi_getpc.io.reset:=reset
  dpi_getpc.io.dpi_valid:=DPI.wb_valid
  dpi_getpc.io.pc:=DPI.pc
  dpi_getpc.io.nextpc:=DPI.nextpc

  val dpi_inv=Module(new dpi_inv())
  dpi_inv.io.clock:=clock
  dpi_inv.io.reset:=reset
  dpi_getpc.io.dpi_valid:=DPI.wb_valid
  dpi_inv.io.inv_flag:=DPI.inv_flag
  dpi_inv.io.pc:=DPI.nextpc

  val dpi_func=Module(new dpi_func())
  dpi_func.io.clock:=clock
  dpi_func.io.reset:=reset
  dpi_getpc.io.dpi_valid:=DPI.wb_valid 
  dpi_func.io.func_flag:=DPI.func_flag
  dpi_func.io.is_jal:=DPI.is_jal
  dpi_func.io.pc:=DPI.pc
  dpi_func.io.nextpc:=DPI.nextpc
  dpi_func.io.is_ret:=DPI.is_ret
  dpi_func.io.is_rd0:=DPI.is_rd0

  val dpi_ebreak=Module(new dpi_ebreak())
  dpi_ebreak.io.clock:=clock
  dpi_ebreak.io.reset:=reset  
  dpi_getpc.io.dpi_valid:=DPI.wb_valid
  dpi_ebreak.io.is_ebreak:=DPI.is_ebreak
  dpi_ebreak.io.pc:=DPI.nextpc
  dpi_ebreak.io.ret_reg_data:=DPI.ret_reg_data
}



class dpi_getpc extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val dpi_valid=Input(Bool())
    val pc      =Input(UInt(32.W))
    val nextpc  =Input(UInt(32.W))
  })
  setInline("dpi_getpc.v",
    """
      |import "DPI-C" function void dpi_getpc(input int pc,input int nextpc);
      |module dpi_getpc(
      |    input        clock,
      |    input        reset,
      |    input        dpi_valid,
      |    input [31:0] pc,
      |    input [31:0] nextpc
      |    
      |);
      | always @(posedge clock)begin
      |   if(~reset)begin
      |     if(dpi_valid) get_pc(pc,nextpc);
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
    val pc      =Input(UInt(32.W))
  })
  setInline("dpi_inv.v",
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

class dpi_func extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val dpi_valid=Input(Bool())
    val func_flag=Input(Bool())
    val is_jal   =Input(Bool())
    val pc       =Input(UInt(32.W))
    val nextpc   =Input(UInt(32.W))
    val is_rd0   =Input(Bool())
    val is_ret   =Input(Bool())
  })
  setInline("dpi_func.v",
    """
      |import "DPI-C" function void cpu_use_func(input int pc,input int nextpc,input bit is_ret,input bit is_jal,input bit is_rd0);
      |module dpi_func(
      |    input        clock,
      |    input        reset,
      |    input        dpi_valid,
      |    input        func_flag,
      |    input        is_jal,
      |    input [31:0] pc,
      |    input [31:0] nextpc,
      |    input [31:0] is_rd0,
      |    input [31:0] is_ret
      |);
      | always @(posedge clock)begin
      |   if(~reset)begin
      |     if(func_flag&&dpi_valid)   cpu_use_func(pc,nextpc,is_ret,is_jal,is_rd0);
      |   end
      |  end
      |endmodule
    """.stripMargin)
}


class dpi_ebreak extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val dpi_valid=Input(Bool())
    val is_ebreak=Input(Bool())
    val pc       =Input(UInt(32.W))
    val ret_reg_data=Input(UInt(32.W))
  })
  setInline("dpi_ebreak.v",
    """
      |import "DPI-C" function void sim_break(input int pc,input int ret_reg_data);
      |module dpi_ebreak(
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

