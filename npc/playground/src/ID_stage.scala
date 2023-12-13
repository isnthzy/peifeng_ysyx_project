import chisel3._
import chisel3.util._  
import config.Configs._

class ID_stage extends Module{
  val io=IO(new Bundle {
    val pc=Input(UInt(32.W))
    val nextpc=Input(UInt(32.W))
    val inst=Input(UInt(32.W))
    val result=Input(UInt(32.W))
    val f_dbus=Input(new if_to_id_bus())
    val Imm=Output(UInt(32.W))
    val is_not_jalr=Output(Bool())
    val is_jump=Output(Bool())
    val d_ebus=Output(new id_to_es_bus())
  })
  //定义
  val Inst=Wire(new Inst())
  val Inst_inv=Wire(Bool())
  val IsaR=dontTouch(Wire(new IsaR()))
  val IsaI=dontTouch(Wire(new IsaI()))
  val IsaS=dontTouch(Wire(new IsaS()))
  val IsaB=dontTouch(Wire(new IsaB()))
  val IsaU=dontTouch(Wire(new IsaU())) //避免取指代码被优化，出现波形找不到现象
  //初始化
  
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

  when(reset===false.B){
    Inst_inv :=io.inst.asUInt =/=0.U &IsaB.asUInt===0.U & IsaI.asUInt===0.U & IsaR.asUInt===0.U & IsaS.asUInt===0.U & IsaU.asUInt===0.U//inv ->inst not valid
  }
  
  IsaI.ebreak:=(io.inst===BitPat("b0000000 00001 00000 000 00000 11100 11"))
  //ebreak的过程->为达到取出a0 (reg[10])号寄存器的目的， 把rs1取10，rs2取0 加起来，交给regfile取

  val ImmType=Wire(new ImmType())
  ImmType.ImmIType:=Mux(IsaI.asUInt=/=0.U,1.U,0.U)
  ImmType.ImmSType:=Mux(IsaS.asUInt=/=0.U,1.U,0.U)
  ImmType.ImmBType:=Mux(IsaB.asUInt=/=0.U,1.U,0.U)
  ImmType.ImmUType:=Mux(IsaU.asUInt=/=0.U,1.U,0.U)
  ImmType.ImmJType:=Mux(IsaU.jal,1.U,0.U) //j指令被包在u里
  io.Imm := MuxLookup(ImmType.asUInt,0.U)(Seq( 
    "b10000".U -> Sext(Inst.immI,32),
    "b01000".U -> Sext(Inst.immS,32),
    "b00100".U -> Sext(Inst.immB,32),
    "b00010".U -> Sext(Inst.immU,32),
    "b00011".U -> Sext(Inst.immJ,32),
  ))
  
  io.d_ebus.data_wen:=(IsaI.addi | IsaR.add  | IsaI.andi| IsaR.and  | IsaU.lui 
         | IsaR.slt  | IsaR.sltu | IsaR.sub | IsaI.ori  | IsaR.or
         | IsaI.xori | IsaR.xor  | IsaI.jalr| IsaU.jal  | IsaU.auipc
         | IsaI.slti | IsaI.sltiu| IsaI.slli| IsaI.srai | IsaI.srli
         | IsaR.slt  | IsaR.sltu | IsaR.sll | IsaR.sra  | IsaR.srl )
  io.d_ebus.result_is_imm:= IsaU.lui
  io.d_ebus.result_is_snpc:=IsaU.jal | IsaI.jalr
  io.d_ebus.src_is_sign:=IsaI.srai | IsaR.sra | IsaR.slt  | IsaB.blt | IsaB.bltu
  io.d_ebus.src1_is_pc :=IsaU.auipc
  io.d_ebus.src2_is_imm:=(IsaI.addi | IsaI.slti | IsaI.sltiu| IsaI.xori| IsaI.ori 
                 | IsaI.andi | IsaI.jalr | IsaU.auipc 
                 | IsaI.lb   | IsaI.lh   | IsaI.lw   | IsaI.lbu | IsaI.lhu )
  io.d_ebus.src2_is_shamt_imm:=IsaI.slli | IsaI.srai | IsaI.srli
  io.d_ebus.src2_is_shamt_src:=IsaR.sll  | IsaR.sra  | IsaR.srl
  io.d_ebus.sram_valid:=IsaI.lb | IsaI.lh | IsaI.lw | IsaI.lbu | IsaI.lhu | IsaS.sb | IsaS.sh | IsaS.sw
  io.d_ebus.sram_wen:=IsaS.sb | IsaS.sh | IsaS.sw
  io.d_ebus.wmask:=Mux(IsaI.lb | IsaI.lbu,1.U,
                   Mux(IsaI.lh | IsaI.lhu,3.U,
                    Mux(IsaI.lw,15.U,0.U)))
  io.d_ebus.is_ebreak:=IsaI.ebreak
  io.d_ebus.src1:=Inst.rs1
  io.d_ebus.src2:=Inst.rs2
  io.d_ebus.rd:=Inst.rd
  io.d_ebus.snpc:=io.f_dbus.snpc

  val is_not_jalr=IsaU.jal|IsaB.beq|IsaB.bne|IsaB.blt|IsaB.bltu|IsaB.bge|IsaB.bgeu

  val alu_op=Wire(Vec(12, Bool()))
  alu_op(0 ):= (IsaI.addi | IsaR.add | IsaI.ebreak | IsaI.jalr | IsaU.auipc
              | io.d_ebus.sram_valid)
  //add加法
  alu_op(1 ):= IsaR.sub
  //sub减法
  alu_op(2 ):= IsaI.andi| IsaR.and
  //and &&
  alu_op(3 ):= IsaI.ori | IsaR.or
  //or  ||
  alu_op(4 ):= IsaI.xori| IsaR.xor
  //xor ^
  alu_op(6 ):=(IsaR.slt | IsaR.sltu | IsaI.slti| IsaI.sltiu)
  //tha <  结果取反就是判断 >=
  alu_op(7 ):= IsaI.slli| IsaR.sll
  //sll << 左移
  alu_op(8 ):= IsaI.srai| IsaR.sra
  //sra >> 无符号右移
  alu_op(9 ):= IsaI.srli| IsaR.srl 
  //srl >> 有符号右移 
  alu_op(10):= 0.U

  val rs1_eq_rs2=Inst.rs1===Inst.rs2
  val rs1_lt_rs2_s=Inst.rs1.asSInt<Inst.rs2.asSInt
  val rs1_lt_rs2_u=Inst.rs1<Inst.rs2
  io.is_jump:= (IsaU.jal 
              | IsaI.jalr
              | IsaB.beq &&  rs1_eq_rs2
              | IsaB.bne && !rs1_eq_rs2
              | IsaB.blt &&  rs1_lt_rs2_s
              | IsaB.bltu&&  rs1_lt_rs2_u
              | IsaB.bge && !rs1_lt_rs2_s
              | IsaB.bgeu&& !rs1_lt_rs2_u
              )

  val singal_dpi=Module(new singal_dpi())
  singal_dpi.io.clock:=clock
  singal_dpi.io.reset:=reset
  singal_dpi.io.pc:=io.pc
  singal_dpi.io.nextpc:=io.nextpc
  singal_dpi.io.inst:=io.inst
  singal_dpi.io.rd:=Inst.rd
  singal_dpi.io.is_jal:=IsaU.jal
  singal_dpi.io.func_flag  :=IsaU.jal | IsaI.jalr
  singal_dpi.io.ebreak_flag:=IsaI.ebreak
  singal_dpi.io.inv_flag   :=Inst_inv
  singal_dpi.io.ret_reg    :=io.result
}

class singal_dpi extends BlackBox with HasBlackBoxPath{
  val io=IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val pc=Input(UInt(32.W))
    val nextpc=Input(UInt(32.W))
    val inst=Input(UInt(32.W))
    val rd=Input(UInt(32.W))
    val is_jal=Input(Bool())
    val func_flag=Input(Bool())
    val ebreak_flag=Input(Bool())
    val inv_flag=Input(Bool()) //inv -> inst not vaild 无效的指令
    val ret_reg=Input(UInt(32.W))
  })
  addPath("playground/src/v_resource/dpi.sv")
}

