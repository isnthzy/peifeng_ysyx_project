import chisel3._
import chisel3.util._  
import config.Configs._

class SimTop extends Module {
  val io = IO(new Bundle {
    val result=Output(UInt(32.W))
    val wen=Output(Bool())
    val imm=Output(UInt(32.W))
  })

//定义变量 
  val pmem_dpi=Module(new pmem_dpi())
  val jalr_taget=dontTouch(Wire(UInt(32.W)))
  val nextpc=dontTouch(Wire(UInt(32.W)))
  val snpc=dontTouch(Wire(UInt(32.W)))
  val dnpc=dontTouch(Wire(UInt(32.W)))
  val sram_rdata=dontTouch(Wire(UInt(32.W)))
  val Imm=Wire(UInt(32.W))
  val Inst_inv=Reg(Bool())
  val is_jump=dontTouch(Wire(Bool()))
  val Inst=Wire(new Inst())
  val IsaR=dontTouch(Wire(new IsaR()))
  val IsaI=dontTouch(Wire(new IsaI()))
  val IsaS=dontTouch(Wire(new IsaS()))
  val IsaB=dontTouch(Wire(new IsaB()))
  val IsaU=dontTouch(Wire(new IsaU())) //避免取指代码被优化，出现波形找不到现象
  val io_inst=dontTouch(Reg(UInt(32.W)))

// IFU begin
  val REGpc=RegInit(START_ADDR)
  pmem_dpi.io.clock:=clock
  pmem_dpi.io.reset:=reset
  pmem_dpi.io.pc:=REGpc
  pmem_dpi.io.nextpc:=nextpc
  io_inst:=pmem_dpi.io.inst
            
  snpc:=REGpc+4.U
  dnpc:=Mux(j_or_b_jump,REGpc+Imm,
          Mux(IsaI.jalr,jalr_taget,snpc))
  REGpc:=Mux(is_jump,dnpc,snpc)
  
  nextpc:=dnpc

// IDU begin

  Inst.immI  :=io_inst(31,20)
  Inst.immS  :=Cat(io_inst(31,25),io_inst(11,7))
  Inst.immB  :=Cat(io_inst(31),io_inst(7),io_inst(30,25),io_inst(11,8),0.U(1.W))
  Inst.immU  :=Cat(io_inst(31,12),0.U(12.W))
  Inst.immJ  :=Cat(io_inst(31),io_inst(19,12),io_inst(20),io_inst(30,21),0.U(1.W))
  Inst.rs2   :=io_inst(24,20) 
  Inst.rs1   :=io_inst(19,15)
  Inst.funct3:=io_inst(14,12)
  Inst.rd    :=io_inst(11,7)
  Inst.opcode:=io_inst( 6,0)

  IsaU.lui   :=(io_inst===BitPat("b??????? ????? ????? ??? ????? 01101 11"))
  IsaU.auipc :=(io_inst===BitPat("b??????? ????? ????? ??? ????? 00101 11"))
  IsaU.jal   :=(io_inst===BitPat("b??????? ????? ????? ??? ????? 11011 11"))
  IsaI.jalr  :=(io_inst===BitPat("b??????? ????? ????? 000 ????? 11001 11"))
  IsaB.beq   :=(io_inst===BitPat("b??????? ????? ????? 000 ????? 11000 11"))
  IsaB.bne   :=(io_inst===BitPat("b??????? ????? ????? 001 ????? 11000 11"))
  IsaB.blt   :=(io_inst===BitPat("b??????? ????? ????? 100 ????? 11000 11"))
  IsaB.bge   :=(io_inst===BitPat("b??????? ????? ????? 101 ????? 11000 11"))
  IsaB.bltu  :=(io_inst===BitPat("b??????? ????? ????? 110 ????? 11000 11"))
  IsaB.bgeu  :=(io_inst===BitPat("b??????? ????? ????? 111 ????? 11000 11"))
  IsaI.lb    :=(io_inst===BitPat("b??????? ????? ????? 000 ????? 00000 11"))
  IsaI.lh    :=(io_inst===BitPat("b??????? ????? ????? 001 ????? 00000 11"))
  IsaI.lw    :=(io_inst===BitPat("b??????? ????? ????? 010 ????? 00000 11"))
  IsaI.lbu   :=(io_inst===BitPat("b??????? ????? ????? 100 ????? 00000 11"))
  IsaI.lhu   :=(io_inst===BitPat("b??????? ????? ????? 101 ????? 00000 11"))
  IsaS.sb    :=(io_inst===BitPat("b??????? ????? ????? 000 ????? 01000 11"))
  IsaS.sh    :=(io_inst===BitPat("b??????? ????? ????? 001 ????? 01000 11"))
  IsaS.sw    :=(io_inst===BitPat("b??????? ????? ????? 010 ????? 01000 11"))
  IsaI.addi  :=(io_inst===BitPat("b??????? ????? ????? 000 ????? 00100 11"))
  IsaI.slti  :=(io_inst===BitPat("b??????? ????? ????? 010 ????? 00100 11"))
  IsaI.sltiu :=(io_inst===BitPat("b??????? ????? ????? 011 ????? 00100 11"))
  IsaI.xori  :=(io_inst===BitPat("b??????? ????? ????? 100 ????? 00100 11"))
  IsaI.ori   :=(io_inst===BitPat("b??????? ????? ????? 110 ????? 00100 11"))
  IsaI.andi  :=(io_inst===BitPat("b??????? ????? ????? 111 ????? 00100 11"))
  IsaI.slli  :=(io_inst===BitPat("b0000000 ????? ????? 001 ????? 00100 11"))
  IsaI.srli  :=(io_inst===BitPat("b0000000 ????? ????? 101 ????? 00100 11"))
  IsaI.srai  :=(io_inst===BitPat("b0100000 ????? ????? 101 ????? 00100 11"))
  IsaR.add   :=(io_inst===BitPat("b0000000 ????? ????? 000 ????? 01100 11"))
  IsaR.sub   :=(io_inst===BitPat("b0100000 ????? ????? 000 ????? 01100 11"))
  IsaR.sll   :=(io_inst===BitPat("b0000000 ????? ????? 001 ????? 01100 11"))
  IsaR.slt   :=(io_inst===BitPat("b0000000 ????? ????? 010 ????? 01100 11"))
  IsaR.sltu  :=(io_inst===BitPat("b0000000 ????? ????? 011 ????? 01100 11"))
  IsaR.xor   :=(io_inst===BitPat("b0000000 ????? ????? 100 ????? 01100 11"))
  IsaR.srl   :=(io_inst===BitPat("b0000000 ????? ????? 101 ????? 01100 11"))
  IsaR.sra   :=(io_inst===BitPat("b0100000 ????? ????? 101 ????? 01100 11"))
  IsaR.or    :=(io_inst===BitPat("b0000000 ????? ????? 110 ????? 01100 11"))
  IsaR.and   :=(io_inst===BitPat("b0000000 ????? ????? 111 ????? 01100 11"))

  when(reset===false.B){
    Inst_inv :=io_inst.asUInt =/=0.U &IsaB.asUInt===0.U & IsaI.asUInt===0.U & IsaR.asUInt===0.U & IsaS.asUInt===0.U & IsaU.asUInt===0.U//inv ->inst not valid
  }
  
  IsaI.ebreak:=(io_inst===BitPat("b0000000 00001 00000 000 00000 11100 11"))
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
  val wen=(IsaI.addi | IsaR.add  | IsaI.andi| IsaR.and  | IsaU.lui 
         | IsaR.slt  | IsaR.sltu | IsaR.sub | IsaI.ori  | IsaR.or
         | IsaI.xori | IsaR.xor  | IsaI.jalr| IsaU.jal  | IsaU.auipc
         | IsaI.slti | IsaI.sltiu| IsaI.slli| IsaI.srai | IsaI.srli
         | IsaR.slt  | IsaR.sltu | IsaR.sll | IsaR.sra  | IsaR.srl )
  val is_b_jump =ImmType.ImmBType
  val result_is_imm= IsaU.lui
  val result_is_snpc=IsaU.jal | IsaI.jalr
  val j_or_b_jump=IsaU.jal|IsaB.beq|IsaB.bne|IsaB.blt|IsaB.bltu|IsaB.bge|IsaB.bgeu
  val src_is_sign=IsaI.srai | IsaR.sra | IsaR.slt  | IsaB.blt | IsaB.bltu
  val src1_is_pc =IsaU.auipc
  val src2_is_imm=(IsaI.addi | IsaI.slti | IsaI.sltiu| IsaI.xori| IsaI.ori 
                 | IsaI.andi | IsaI.jalr | IsaU.auipc 
                 | IsaI.lb   | IsaI.lh   | IsaI.lw   | IsaI.lbu | IsaI.lhu )
  val src2_is_shamt_imm=IsaI.slli | IsaI.srai | IsaI.srli
  val src2_is_shamt_src=IsaR.sll  | IsaR.sra  | IsaR.srl
  val sram_valid=IsaI.lb | IsaI.lh | IsaI.lw | IsaI.lbu | IsaI.lhu | IsaS.sb | IsaS.sh | IsaS.sw
  val sram_wen=IsaS.sb | IsaS.sh | IsaS.sw
  val wmask=Mux(IsaI.lb | IsaI.lbu,1.U,
              Mux(IsaI.lh | IsaI.lhu,3.U,
                Mux(IsaI.lw,15.U,0.U)))
  io.wen:=wen


  val alu_op=Wire(Vec(12, Bool()))
  alu_op(0 ):= (IsaI.addi | IsaR.add | IsaI.ebreak | IsaI.jalr | IsaU.auipc
              | sram_valid)
  //add加法
  alu_op(1 ):= IsaR.sub
  //sub减法
  alu_op(2 ):= IsaI.andi| IsaR.and
  //and &&
  alu_op(3 ):= IsaI.ori | IsaR.or
  //or  ||
  alu_op(4 ):= IsaI.xori| IsaR.xor
  //xor ^
  alu_op(5 ):= IsaB.beq | IsaB.bne 
  //eq === 结果取反就是判断!=
  alu_op(6 ):=(IsaB.blt | IsaB.bltu | IsaB.bge | IsaB.bgeu | IsaR.slt | IsaR.sltu 
             | IsaI.slti| IsaI.sltiu)
  //tha <  结果取反就是判断 >=
  alu_op(7 ):= IsaI.slli| IsaR.sll
  //sll << 左移
  alu_op(8 ):= IsaI.srai| IsaR.sra
  //sra >> 无符号右移
  alu_op(9):= IsaI.srli| IsaR.srl 
  //srl >> 有符号右移 
  alu_op(10):= 0.U
  
// EXU begin
  val RegFile=Module(new RegFile())
  RegFile.io.raddr1:=Mux(IsaI.ebreak,10.U,Inst.rs1)
  RegFile.io.raddr2:=Mux(IsaI.ebreak, 0.U,Inst.rs2)
  RegFile.io.waddr:=Inst.rd
  RegFile.io.wen:=wen
  val rf_rdata1=RegFile.io.rdata1
  val rf_rdata2=RegFile.io.rdata2

  val alu =Module(new Alu())
  val src1=Mux(src1_is_pc ,REGpc,rf_rdata1)
  val src2=Mux(src2_is_imm,Imm,
            Mux(src2_is_shamt_imm,Inst.immI(5,0), //立即数(5,0)的位移量
              Mux(src2_is_shamt_src,rf_rdata2(5,0),rf_rdata2))) //src2(5,0)的位移量

  alu.io.op  :=alu_op.asUInt
  alu.io.src1:=src1
  alu.io.src2:=src2
  alu.io.sign:=src_is_sign 

  is_jump := (IsaU.jal 
            | IsaI.jalr
            | IsaB.beq &&  alu.io.result(0)
            | IsaB.bne && ~alu.io.result(0)
            | IsaB.blt &&  alu.io.result(0)
            | IsaB.bltu&&  alu.io.result(0)
            | IsaB.bge && ~alu.io.result(0)
            | IsaB.bgeu&& ~alu.io.result(0)
            )
              


  io.result:=Mux(result_is_imm,Imm,
              Mux(result_is_snpc,snpc,alu.io.result)) //要往rd中写入dnpc
  val jalr_tmp=alu.io.result+Imm
  jalr_taget:=Cat(jalr_tmp(31,1),0.U(1.W))
  RegFile.io.wdata:=io.result

  pmem_dpi.io.sram_valid:=sram_valid
  pmem_dpi.io.sram_wen:=sram_wen
  pmem_dpi.io.raddr:=alu.io.result
  sram_rdata:=pmem_dpi.io.rdata
  pmem_dpi.io.waddr:=alu.io.result
  pmem_dpi.io.wdata:=src2
  pmem_dpi.io.wmask:=wmask
  
  // val sram_rdata_resul
  val singal_dpi=Module(new singal_dpi())
  singal_dpi.io.clock:=clock
  singal_dpi.io.reset:=reset
  singal_dpi.io.pc:=REGpc
  singal_dpi.io.nextpc:=nextpc
  singal_dpi.io.inst:=io_inst
  singal_dpi.io.rd:=Inst.rd
  singal_dpi.io.is_jal:=IsaU.jal
  singal_dpi.io.func_flag  :=IsaU.jal | IsaI.jalr
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

class pmem_dpi extends BlackBox with HasBlackBoxPath{
  val io=IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val pc=Input(UInt(32.W))
    val nextpc=Input(UInt(32.W))
    val sram_valid=Input(Bool())
    val sram_wen=Input(Bool())
    val raddr=Input(UInt(32.W))
    val rdata=Output(UInt(32.W))
    val waddr=Input(UInt(32.W))
    val wdata=Input(UInt(32.W))
    val wmask=Input(UInt(5.W))
    val inst=Output(UInt(32.W))
  })
  addPath("playground/src/v_resource/pmem.sv")
}
