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

//定义变量
  val Imm=Wire(UInt(32.W))
  val Inst_inv=Wire(Bool())
  val is_jump=Wire(Bool())
  val Inst=Wire(new Inst())
  val IsaR=dontTouch(Wire(new IsaR()))
  val IsaI=dontTouch(Wire(new IsaI()))
  val IsaS=dontTouch(Wire(new IsaS()))
  val IsaB=dontTouch(Wire(new IsaB()))
  val IsaU=dontTouch(Wire(new IsaU())) //避免取指代码被优化，出现波形找不到现象

// IFU begin
  val pc=RegInit(START_ADDR)
  val dnpc=Mux(IsaI.jalr,(pc+Imm)& ~1.U,pc+Imm) //下一条动态指令
  val snpc=pc+4.U //下一条静态指令
  when(is_jump){
    pc := dnpc
  }.otherwise{
    pc := snpc
  }
  io.pc:=pc
  
// IDU begin


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

  IsaU.lui   :=(io.inst===BitPat("b??????? ????? ????? ??? ????? 01101 11"))
  IsaU.auipc :=(io.inst===BitPat("b??????? ????? ????? ??? ????? 00101 11"))
  IsaU.jal   :=(io.inst===BitPat("b??????? ????? ????? ??? ????? 11011 11"))
  IsaI.jalr  :=(io.inst===BitPat("b??????? ????? ????? 000 ????? 11001 11"))
  IsaB.beq   :=(io.inst===BitPat("b??????? ????? ????? 000 ????? 11000 11"))
  IsaB.bne   :=(io.inst===BitPat("b??????? ????? ????? 001 ????? 11000 11"))
  IsaB.blt   :=(io.inst===BitPat("b??????? ????? ????? 100 ????? 11000 11"))
  IsaB.bge   :=(io.inst===BitPat("b??????? ????? ????? 101 ????? 11000 11"))
  IsaB.bltu  :=(io.inst===BitPat("b??????? ????? ????? 110 ????? 11000 11"))
  IsaB.bgeu  :=(io.inst===BitPat("b??????? ????? ????? 111 ????? 11000 11"))
  IsaI.lb    :=(io.inst===BitPat("b??????? ????? ????? 000 ????? 00000 11"))
  IsaI.lh    :=(io.inst===BitPat("b??????? ????? ????? 001 ????? 00000 11"))
  IsaI.lw    :=(io.inst===BitPat("b??????? ????? ????? 010 ????? 00000 11"))
  IsaI.lbu   :=(io.inst===BitPat("b??????? ????? ????? 100 ????? 00000 11"))
  IsaI.lhu   :=(io.inst===BitPat("b??????? ????? ????? 101 ????? 00000 11"))
  IsaS.sb    :=(io.inst===BitPat("b??????? ????? ????? 000 ????? 01000 11"))
  IsaS.sh    :=(io.inst===BitPat("b??????? ????? ????? 001 ????? 01000 11"))
  IsaS.sw    :=(io.inst===BitPat("b??????? ????? ????? 010 ????? 01000 11"))
  IsaI.addi  :=(io.inst===BitPat("b??????? ????? ????? 000 ????? 00100 11"))
  IsaI.slti  :=(io.inst===BitPat("b??????? ????? ????? 010 ????? 00100 11"))
  IsaI.sltiu :=(io.inst===BitPat("b??????? ????? ????? 011 ????? 00100 11"))
  IsaI.xori  :=(io.inst===BitPat("b??????? ????? ????? 100 ????? 00100 11"))
  IsaI.ori   :=(io.inst===BitPat("b??????? ????? ????? 110 ????? 00100 11"))
  IsaI.andi  :=(io.inst===BitPat("b??????? ????? ????? 111 ????? 00100 11"))
  IsaI.slli  :=(io.inst===BitPat("b0000000 ????? ????? 001 ????? 00100 11"))
  IsaI.srli  :=(io.inst===BitPat("b0000000 ????? ????? 101 ????? 00100 11"))
  IsaI.srai  :=(io.inst===BitPat("b0100000 ????? ????? 101 ????? 00100 11"))
  IsaR.add   :=(io.inst===BitPat("b0000000 ????? ????? 000 ????? 01100 11"))
  IsaR.sub   :=(io.inst===BitPat("b0100000 ????? ????? 000 ????? 01100 11"))
  IsaR.sll   :=(io.inst===BitPat("b0000000 ????? ????? 001 ????? 01100 11"))
  IsaR.slt   :=(io.inst===BitPat("b0000000 ????? ????? 010 ????? 01100 11"))
  IsaR.sltu  :=(io.inst===BitPat("b0000000 ????? ????? 011 ????? 01100 11"))
  IsaR.xor   :=(io.inst===BitPat("b0000000 ????? ????? 100 ????? 01100 11"))
  IsaR.srl   :=(io.inst===BitPat("b0000000 ????? ????? 101 ????? 01100 11"))
  IsaR.sra   :=(io.inst===BitPat("b0100000 ????? ????? 101 ????? 01100 11"))
  IsaR.or    :=(io.inst===BitPat("b0000000 ????? ????? 110 ????? 01100 11"))
  IsaR.and   :=(io.inst===BitPat("b0000000 ????? ????? 111 ????? 01100 11"))
  
  Inst_inv   := IsaB.asUInt===0.U & IsaI.asUInt===0.U & IsaR.asUInt===0.U & IsaS.asUInt===0.U & IsaU.asUInt===0.U//inv ->inst not valid
  IsaI.ebreak:=(io.inst===BitPat("b0000000 00001 00000 000 00000 11100 11"))
  //ebreak的过程->为达到取出a0 (reg[10])号寄存器的目的， 把rs1取10，rs2取0 加起来，交给regfile取

  val ImmType=Wire(new ImmType())
  ImmType.ImmIType:=Mux(IsaI.asUInt=/=0.U,1.U,0.U)
  ImmType.ImmSType:=Mux(IsaS.asUInt=/=0.U,1.U,0.U)
  ImmType.ImmBType:=Mux(IsaB.asUInt=/=0.U,1.U,0.U)
  ImmType.ImmUType:=Mux(IsaU.asUInt=/=0.U,1.U,0.U)
  ImmType.ImmJType:=Mux(IsaU.jal,1.U,0.U) //j指令被包在u里
  Imm := MuxLookup(ImmType.asUInt,0.U)(Seq( 
    "b10000".U -> Sext(Inst.immI,32),
    "b01000".U -> Sext(Inst.immS,32),
    "b00100".U -> Sext(Inst.immB,32),
    "b00010".U -> Sext(Inst.immU,32),
    "b00011".U -> Sext(Inst.immJ,32),
  ))
  io.imm:=Imm
  val wen=(IsaI.addi | IsaR.add  | IsaI.andi| IsaR.and  | IsaU.lui | IsaU.auipc
         | IsaR.slt  | IsaR.sltu | IsaR.sub | IsaI.ori  | IsaR.or
         | IsaI.xori | IsaR.xor
         | IsaI.slti | IsaI.sltiu| IsaI.slli| IsaI.srai | IsaI.srli
         | IsaR.slt  | IsaR.sltu | IsaR.sll | IsaR.sra  | IsaR.srl )
  val is_b_jump =ImmType.ImmBType
  val result_is_imm= IsaU.lui
  val result_is_dnpc=IsaU.jal | IsaI.jalr
  val src_is_sign=IsaI.srai | IsaR.sra | IsaR.slt  | IsaB.blt | IsaB.bltu
  val src1_is_pc =IsaU.auipc
  val src2_is_imm=IsaI.addi | IsaI.slti| IsaI.sltiu| IsaI.xori| IsaI.ori | IsaI.andi
  val src2_is_shamt_imm=IsaI.slli | IsaI.srai | IsaI.srli
  val src2_is_shamt_src=IsaR.sll  | IsaR.sra  | IsaR.srl
  io.wen:=wen

// EXU begin
  val alu_op=Wire(Vec(12, Bool()))
  alu_op(0 ):= IsaI.addi | IsaR.add | IsaI.ebreak
  //add加法
  alu_op(1 ):= IsaR.sub
  //sub减法
  alu_op(2 ):= 0.U
  //neg取反
  alu_op(3 ):= IsaI.andi| IsaR.and
  //and &&
  alu_op(4 ):= IsaI.ori | IsaR.or
  //or  ||
  alu_op(5 ):= IsaI.xori| IsaR.xor
  //xor ^
  alu_op(6 ):= IsaB.beq | IsaB.bne 
  //eq === 结果取反就是判断!=
  alu_op(7 ):=(IsaB.blt | IsaB.bltu | IsaB.bge | IsaB.bgeu | IsaR.slt | IsaR.sltu 
             | IsaI.slti| IsaI.sltiu)
  //tha <  结果取反就是判断 >=
  alu_op(8 ):= IsaI.slli| IsaR.sll
  //sll << 左移
  alu_op(9 ):= IsaI.srai| IsaR.sra
  //sra >> 无符号右移
  alu_op(10):= IsaI.srli| IsaR.srl 
  //srl >> 有符号右移 
  alu_op(11):= 0.U
  
  val RegFile=Module(new RegFile())
  RegFile.io.raddr1:=Mux(IsaI.ebreak,10.U,Inst.rs1)
  RegFile.io.raddr2:=Mux(IsaI.ebreak, 0.U,Inst.rs2)
  RegFile.io.waddr:=Inst.rd
  RegFile.io.wen:=wen
  val rf_rdata1=RegFile.io.rdata1
  val rf_rdata2=RegFile.io.rdata2

  val alu =Module(new Alu())
  val src1=Mux(src1_is_pc ,pc,rf_rdata1)
  val src2=Mux(src2_is_imm,Imm,
            Mux(src2_is_shamt_imm,Inst.immI(5,0), //立即数(5,0)的位移量
              Mux(src2_is_shamt_src,rf_rdata2(5,0),rf_rdata2))) //src2(5,0)的位移量

  alu.io.op  :=alu_op.asUInt
  alu.io.src1:=src1
  alu.io.src2:=src2
  alu.io.sign:=src_is_sign 

  is_jump := (IsaU.jal 
            | IsaB.beq &&  alu.io.result(0)
            | IsaB.bne && ~alu.io.result(0)
            | IsaB.blt &&  alu.io.result(0)
            | IsaB.bltu&&  alu.io.result(0)
            | IsaB.bge && ~alu.io.result(0)
            | IsaB.bgeu&& ~alu.io.result(0)
            )
              


  io.result:=Mux(result_is_imm,Imm,
              Mux(result_is_dnpc,dnpc,alu.io.result)) //要往rd中写入dnpc
  RegFile.io.wdata:=io.result

  val singal_dpi=Module(new singal_dpi())
  singal_dpi.io.clock:=clock
  singal_dpi.io.reset:=reset
  singal_dpi.io.pc:=io.pc
  singal_dpi.io.ebreak_flag:=IsaI.ebreak
  singal_dpi.io.inv_flag   :=Inst_inv
  singal_dpi.io.ret_reg    :=alu.io.result
//WB begin
  
}

class singal_dpi extends BlackBox with HasBlackBoxPath{
  val io=IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val pc=Input(UInt(32.W))
    val ebreak_flag=Input(Bool())
    val inv_flag=Input(Bool()) //inv -> inst not vaild 无效的指令
    val ret_reg=Input(UInt(32.W))
  })
  addPath("playground/src/v_resource/dpi.sv")
}

