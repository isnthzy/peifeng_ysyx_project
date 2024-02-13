import chisel3._
import chisel3.util._
import config.Configs._
import Control._

class ID_stage extends Module {
  val ID=IO(new Bundle {
    // val IO    =Input(new if_to_id_bus())
    val IO = Flipped(Decoupled(new if_to_id_bus()))
    val to_ex =Decoupled(new id_to_ex_bus())
    val wb_bus=Input(new wb_to_id_bus())
  })
  val dc=Module(new Decode())
  val ImmGen=Module(new ImmGen())
  val imm=dontTouch(Wire(UInt(32.W)))
  val rs1=dontTouch(Wire(UInt(5.W)))
  val rs2=dontTouch(Wire(UInt(5.W)))
  val funct3=dontTouch(Wire(UInt(3.W)))
  val rd=dontTouch(Wire(UInt(5.W)))
  val opcode=dontTouch(Wire(UInt(7.W)))
  val csr_addr=dontTouch(Wire(UInt(12.W)))


  dc.io.inst:=ID.IO.bits.inst

  ImmGen.io.inst:=ID.IO.bits.inst
  ImmGen.io.sel :=dc.io.imm_sel

  imm := ImmGen.io.out
  rs2 := ID.IO.bits.inst(24, 20)
  rs1 := ID.IO.bits.inst(19, 15)
  funct3 := ID.IO.bits.inst(14, 12)
  rd := ID.IO.bits.inst(11, 7)
  opcode := ID.IO.bits.inst(6, 0)
  csr_addr := ID.IO.bits.inst(31, 20)

  val Regfile=Module(new RegFile())
  Regfile.io.raddr1:=rs1
  Regfile.io.raddr2:=MuxLookup(dc.io.csr_cmd,rs2)(Seq(
    CSR.BREAK->10.U,
    CSR.ECALL->RISCV32E_ECALLREG
  ))
  //当ebreak时，算出reg(10)+0的结果并通知dpi-c，即reg(10)==return
  //当ecall时，算出reg(ECALL_REG)+0的结果并传递给WB的csr处理

  val src1=MuxLookup(dc.io.A_sel,0.U)(Seq(
    A_RS1 -> Regfile.io.rdata1,
    A_PC  -> ID.IO.bits.pc
  ))
  val src2=MuxLookup(dc.io.B_sel,0.U)(Seq(
    B_RS2 -> Regfile.io.rdata2,
    B_IMM -> imm
  ))
  Regfile.io.waddr:=ID.wb_bus.waddr
  Regfile.io.wdata:=ID.wb_bus.wdata
  Regfile.io.wen  :=ID.wb_bus.wen

  ID.to_ex.bits.pc_sel:=dc.io.pc_sel
  ID.to_ex.bits.csr_addr:=csr_addr
  ID.to_ex.bits.csr_cmd:=dc.io.csr_cmd
  ID.to_ex.bits.rs1_addr:=rs1
  //csr
  ID.to_ex.bits.st_type:=dc.io.st_type
  ID.to_ex.bits.ld_type:=dc.io.ld_type
  ID.to_ex.bits.ebreak_flag:=(dc.io.csr_cmd===CSR.BREAK)
  ID.to_ex.bits.wb_sel :=dc.io.wb_sel
  ID.to_ex.bits.br_type:=dc.io.br_type
  ID.to_ex.bits.wen :=dc.io.wb_en
  ID.to_ex.bits.rd  :=rd
  ID.to_ex.bits.alu_op:=dc.io.alu_op
  ID.to_ex.bits.src1:=src1
  ID.to_ex.bits.src2:=src2
  ID.to_ex.bits.rdata1:=Regfile.io.rdata1
  ID.to_ex.bits.rdata2:=Regfile.io.rdata2
  ID.to_ex.bits.inst:=ID.IO.bits.inst
  ID.to_ex.bits.pc  :=ID.IO.bits.pc
  ID.to_ex.bits.nextpc:=ID.IO.bits.nextpc


  
  val inv_break=Module(new inv_break())
  val inv_flag=Reg(Bool())
  inv_flag:=(dc.io.illegal&&ID.IO.bits.nextpc=/="h80000000".U)
  inv_break.io.clock:=clock
  inv_break.io.reset:=reset
  inv_break.io.pc:=ID.IO.bits.nextpc
  inv_break.io.inv_flag:=inv_flag
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
      |     if(inv_flag)  inv_break(pc);
      |   end
      |  end
      |endmodule
    """.stripMargin)
}  