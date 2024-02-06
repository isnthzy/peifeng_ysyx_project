import chisel3._
import chisel3.util._
import config.Configs._
import Control._
class EX_stage extends Module {
  val EX=IO(new Bundle {
    val IO    =Input(new id_to_ex_bus())
    val to_ls =Output(new ex_to_ls_bus())
    val br_bus=Output(new br_bus())
  })
  
  val Alu=Module(new Alu())
  Alu.io.op:=EX.IO.alu_op
  Alu.io.src1:=EX.IO.src1
  Alu.io.src2:=EX.IO.src2
  
  //分支跳转
  val rs1_eq_rs2   = EX.IO.rdata1 === EX.IO.rdata2
  val rs1_lt_rs2_s = EX.IO.rdata1.asSInt < EX.IO.rdata2.asSInt
  val rs1_lt_rs2_u = EX.IO.rdata1  <  EX.IO.rdata2

  EX.br_bus.is_jump := ((EX.IO.br_type===BR_JAL)
                      | (EX.IO.br_type===BR_JR)
                      | ((EX.IO.br_type===BR_EQ) && rs1_eq_rs2)
                      | ((EX.IO.br_type===BR_NE) && !rs1_eq_rs2)
                      | ((EX.IO.br_type===BR_LT) && rs1_lt_rs2_s)
                      | ((EX.IO.br_type===BR_LTU)&& rs1_lt_rs2_u)
                      | ((EX.IO.br_type===BR_GE) && !rs1_lt_rs2_s)
                      | ((EX.IO.br_type===BR_GEU)&& !rs1_lt_rs2_u))
  EX.br_bus.dnpc:=MuxLookup(EX.IO.br_type,0.U)(Seq(
    BR_XXX -> 0.U,
    BR_LTU -> Alu.io.result,
    BR_LT  -> Alu.io.result,
    BR_EQ  -> Alu.io.result,
    BR_GEU -> Alu.io.result,
    BR_GE  -> Alu.io.result,
    BR_NE  -> Alu.io.result,
    BR_JAL -> Alu.io.result,
    BR_JR  -> Cat(Alu.io.result(31,1),0.U(1.W))
  ))
  
  EX.to_ls.pc_sel:=EX.IO.pc_sel
  EX.to_ls.csr_addr:=EX.IO.csr_addr
  EX.to_ls.csr_cmd:=EX.IO.csr_cmd
  EX.to_ls.rs1_addr:=EX.IO.rs1_addr
  //csr
  EX.to_ls.st_type:=EX.IO.st_type
  EX.to_ls.ld_type:=EX.IO.ld_type
  EX.to_ls.ebreak_flag:=EX.IO.ebreak_flag
  EX.to_ls.wb_sel:=EX.IO.wb_sel
  EX.to_ls.wen :=EX.IO.wen
  EX.to_ls.rd:=EX.IO.rd
  EX.to_ls.rdata2:=EX.IO.rdata2
  EX.to_ls.result:=Alu.io.result
  EX.to_ls.pc  :=EX.IO.pc
  EX.to_ls.inst:=EX.IO.inst
  EX.to_ls.nextpc:=EX.IO.nextpc

  val dpi_func=Module(new dpi_func())
  dpi_func.io.clock:=clock
  dpi_func.io.reset:=reset
  dpi_func.io.func_flag:=(EX.IO.br_type===BR_JAL)|(EX.IO.br_type===BR_JR)
  dpi_func.io.is_jal:=(EX.IO.br_type===BR_JAL)
  dpi_func.io.pc:=EX.IO.nextpc
  dpi_func.io.nextpc:=0.U
  dpi_func.io.rd:=EX.IO.rd
  dpi_func.io.inst:=EX.IO.inst

}

class dpi_func extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val func_flag=Input(Bool())
    val is_jal   =Input(Bool())
    val pc       =Input(UInt(32.W))
    val nextpc   =Input(UInt(32.W))
    val rd       =Input(UInt(32.W))
    val inst     =Input(UInt(32.W))
  })
  setInline("dpi_func.v",
    """
      |import "DPI-C" function void cpu_use_func(input int pc,input int nextpc,input int inst,input bit is_jal,input int rd);
      |module dpi_func(
      |    input        clock,
      |    input        reset,
      |    input        func_flag,
      |    input        is_jal,
      |    input [31:0] pc,
      |    input [31:0] nextpc,
      |    input [31:0] rd,
      |    input [31:0] inst
      |);
      | always @(posedge clock)begin
      |   if(~reset)begin
      |     if(func_flag)   cpu_use_func(pc,nextpc,inst,is_jal,rd);
      |   end
      |  end
      |endmodule
    """.stripMargin)
}