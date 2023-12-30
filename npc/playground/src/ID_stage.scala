import chisel3._
import chisel3.util._
import config.Configs._
import Control._

class ID_stage extends Module {
  val ID=IO(new Bundle {
    val IO    =Input(new if_to_id_bus())
    val to_ex =Output(new id_to_ex_bus())
    val wb_bus=Input(new wb_bus())
  })
  val dc=Module(new Decode())
  val ImmGen=Module(new ImmGen())

  dc.io.inst:=ID.IO.inst

  ImmGen.io.inst:=ID.IO.inst
  ImmGen.io.sel :=dc.io.imm_sel

  val imm    = ImmGen.io.out
  val rs2    = ID.IO.inst(24, 20)
  val rs1    = ID.IO.inst(19, 15)
  val funct3 = ID.IO.inst(14, 12)
  val rd     = ID.IO.inst(11, 7)
  val opcode = ID.IO.inst(6, 0)

  val Regfile=Module(new RegFile())
  Regfile.io.raddr1:=rs1
  Regfile.io.raddr2:=Mux(dc.io.csr_cmd===CSR.B,10.U,rs2)
  //当ebreak时，算出reg(10)+0的结果并通知dpi-c，即reg(10)==return

  val src1=MuxLookup(dc.io.A_sel,0.U)(Seq(
    A_RS1 -> Regfile.io.rdata1,
    A_PC  -> ID.IO.pc
  ))
  val src2=MuxLookup(dc.io.B_sel,0.U)(Seq(
    B_RS2 -> Regfile.io.rdata2,
    B_IMM -> imm
  ))
  Regfile.io.waddr:=ID.wb_bus.waddr
  Regfile.io.wdata:=ID.wb_bus.wdata
  Regfile.io.wen  :=ID.wb_bus.wen

  ID.to_ex.ebreak_flag:=(dc.io.csr_cmd===CSR.B)
  ID.to_ex.wb_sel :=dc.io.wb_sel
  ID.to_ex.br_type:=dc.io.br_type
  ID.to_ex.wen :=dc.io.wb_en
  ID.to_ex.rd  :=rd
  ID.to_ex.alu_op:=dc.io.alu_op
  ID.to_ex.src1:=src1
  ID.to_ex.src2:=src2
  ID.to_ex.inst:=ID.IO.inst
  ID.to_ex.pc  :=ID.IO.pc

  val inv_break=Module(new inv_break())
  inv_break.io.clock:=clock
  inv_break.io.reset:=reset
  inv_break.io.pc:=ID.IO.pc
  inv_break.io.inv_flag:=dc.io.illegal
}


class inv_break extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val inv_flag=Input(Bool())
    val pc      =Input(UInt(32.W))
  })
  setInline("inv_break.v",
    """
      |import "DPI-C" function void inv_break(input int pc);
      |module inv_break(
      |    input        clock,
      |    input        reset,
      |    input        inv_flag,
      |    input [31:0] pc
      |);
      | always @(posedge clock)begin
      |   if(~reset)begin
      |     if(inv_flag)  inv_break(nextpc);
      |   end
      |  end
      |endmodule
    """.stripMargin)
}