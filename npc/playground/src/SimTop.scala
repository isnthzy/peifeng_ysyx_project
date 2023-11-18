import chisel3._
import chisel3.util._  
import config.Configs._

class SimTop extends Module {
  val io = IO(new Bundle {
    val inst=Input(UInt(32.W))
    val pc=Output(UInt(32.W))
    val result=Output(UInt(32.W))
    val wen=Output(Bool())
    val imm=Output(UInt(32.W))
  })
  
// IFU begin
  val pc=RegInit(START_ADDR)
  pc:=pc+4.U
  io.pc:=pc
// IDU begin
  val Inst=Wire(new Inst())
  val Isa =Wire(new Isa())
  Inst.immI  :=io.inst(31,20)
  Inst.immS  :=Cat(io.inst(31,25),io.inst(11,7))
  Inst.immB  :=Cat(io.inst(31),io.inst(7),io.inst(30,25),io.inst(11,8),0.U(1.W))
  Inst.immU  :=Cat(io.inst(31,12),0.U(12.W))
  Inst.immJ  :=Cat(io.inst(31),io.inst(19,12),io.inst(20),io.inst(30,21),0.U(1.W))
  Inst.rs2   :=io.inst(24,20)
  Inst.rs1   :=io.inst(19,15)
  Inst.funct3:=io.inst(14,12)
  Inst.rd    :=io.inst(11,7)
  Inst.opcode:=io.inst( 6,0)

  Isa.addi  :=(io.inst===BitPat("b??????? ????? ????? 000 ????? 00100 11"))
  Isa.ebreak:=(io.inst===BitPat("b0000000 00001 00000 000 00000 11100 11"))
  val singal_ebreak=Module(new singal_ebreak())
  singal_ebreak.io.flag:=Isa.ebreak
  singal_ebreak.io.clock:=clock

  val ImmType=Wire(new ImmType())
  ImmType.ImmIType:=Isa.addi | Isa.ebreak 
  ImmType.ImmSType:=0.U
  ImmType.ImmBType:=0.U
  ImmType.ImmUType:=0.U
  ImmType.ImmJType:=0.U
  val Imm=MuxLookup(ImmType.asUInt,0.U)(Seq( 
    "b00001".U -> Sext(Inst.immI,32),
    "b00010".U -> Sext(Inst.immS,32),
    "b00100".U -> Sext(Inst.immB,32),
    "b01000".U -> Sext(Inst.immU,32),
    "b10000".U -> Sext(Inst.immJ,32),
  ))
  io.imm:=Imm
  val wen=Isa.addi
  io.wen:=wen
  val src2_is_imm=Isa.addi
// EXU begin

  val alu_op=Wire(Vec(12, Bool()))
  alu_op(0 ):=Isa.addi
  alu_op(1 ):=0.U
  alu_op(2 ):=0.U
  alu_op(3 ):=0.U
  alu_op(4 ):=0.U
  alu_op(5 ):=0.U
  alu_op(6 ):=0.U
  alu_op(7 ):=0.U
  alu_op(8 ):=0.U
  alu_op(9 ):=0.U
  alu_op(10):=0.U
  alu_op(11):=0.U
  
  val RegFile=Module(new RegFile())
  RegFile.io.raddr1:=Inst.rs1
  RegFile.io.raddr2:=Inst.rs2
  RegFile.io.waddr:=Inst.rd
  RegFile.io.wen:=wen
  val rf_rdata1=RegFile.io.rdata1
  val rf_rdata2=RegFile.io.rdata2

  val alu =Module(new Alu())
  val src1=rf_rdata1
  val src2=Mux(src2_is_imm,Imm,rf_rdata2)

  alu.io.op  :=alu_op.asUInt
  alu.io.src1:=src1
  alu.io.src2:=src2
  io.result:=alu.io.result
  RegFile.io.wdata:=io.result
//WB begin
  
}

class singal_ebreak extends BlackBox with HasBlackBoxPath{
  val io=IO(new Bundle {
    val clock=Input(Clock())
    val flag=Input(Bool())
  })
  addPath("playground/src/v_resource/ebreak.sv")
}

