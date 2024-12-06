package FuncUnit

import chisel3._
import chisel3.util._
import chisel3.util.experimental.decode._


object Instructions {
  // Loads
  def LB  = BitPat("b?????????????????000?????0000011")
  def LH  = BitPat("b?????????????????001?????0000011")
  def LW  = BitPat("b?????????????????010?????0000011")
  def LBU = BitPat("b?????????????????100?????0000011")
  def LHU = BitPat("b?????????????????101?????0000011")
  // Stores
  def SB = BitPat("b?????????????????000?????0100011")
  def SH = BitPat("b?????????????????001?????0100011")
  def SW = BitPat("b?????????????????010?????0100011")
  // Alus
  def SLL  = BitPat("b0000000??????????001?????0110011")
  def SLLI = BitPat("b0000000??????????001?????0010011")
  def SRL  = BitPat("b0000000??????????101?????0110011")
  def SRLI = BitPat("b0000000??????????101?????0010011")
  def SRA  = BitPat("b0100000??????????101?????0110011")
  def SRAI = BitPat("b0100000??????????101?????0010011")

  def ADD   = BitPat("b0000000??????????000?????0110011")
  def ADDI  = BitPat("b?????????????????000?????0010011")
  def SUB   = BitPat("b0100000??????????000?????0110011")
  def LUI   = BitPat("b?????????????????????????0110111")
  def AUIPC = BitPat("b?????????????????????????0010111")
  def XOR   = BitPat("b0000000??????????100?????0110011")
  def XORI  = BitPat("b?????????????????100?????0010011")
  def OR    = BitPat("b0000000??????????110?????0110011")
  def ORI   = BitPat("b?????????????????110?????0010011")
  def AND   = BitPat("b0000000??????????111?????0110011")
  def ANDI  = BitPat("b?????????????????111?????0010011")

  def SLT   = BitPat("b0000000??????????010?????0110011")
  def SLTI  = BitPat("b?????????????????010?????0010011")
  def SLTU  = BitPat("b0000000??????????011?????0110011")
  def SLTIU = BitPat("b?????????????????011?????0010011")
  // Branches
  def BEQ  = BitPat("b?????????????????000?????1100011")
  def BNE  = BitPat("b?????????????????001?????1100011")
  def BLT  = BitPat("b?????????????????100?????1100011")
  def BGE  = BitPat("b?????????????????101?????1100011")
  def BLTU = BitPat("b?????????????????110?????1100011")
  def BGEU = BitPat("b?????????????????111?????1100011")
  // Jump & Link
  def JAL  = BitPat("b?????????????????????????1101111")
  def JALR = BitPat("b?????????????????000?????1100111")

  // FENCE_I 
  def FENCE_I = BitPat("b00000000000000000001000000001111")

  // Csr
  def CSRRW = BitPat("b?????????????????001?????1110011")
  def CSRRS = BitPat("b?????????????????010?????1110011")

  def ECALL  = BitPat("b00000000000000000000000001110011")
  def EBREAK = BitPat("b00000000000100000000000001110011")
  def MRET   = BitPat("b00110000001000000000000001110011")

}

object Control {
  val Y = "1"
  val N = "0"

  // A_sel
  val A_XXX = "0"
  val A_PC  = "0"
  val A_RS1 = "1"

  // B_sel
  val B_XXX = "00"
  val B_IMM = "00"
  val B_RS2 = "01"
  val B_CSR = "10"

  // imm_sel
  val IMM_X = "000"
  val IMM_I = "001"
  val IMM_S = "010"
  val IMM_U = "011"
  val IMM_J = "100"
  val IMM_B = "101"
  // val IMM_Z = 6.U(3.W)

  // br_type
  val BR_XXX = "0000"
  val BR_LTU = "0001"
  val BR_LT  = "0010"
  val BR_EQ  = "0011"
  val BR_GEU = "0100"
  val BR_GE  = "0101"
  val BR_NE  = "0110"
  val BR_JAL = "0111"
  val BR_JALR= "1000"

  // st_type
  val ST_XXX = "00"
  val ST_SB  = "01"
  val ST_SH  = "10"
  val ST_SW  = "11"

  // ld_type
  val LD_XXX = "000"
  val LD_LW  = "001"
  val LD_LH  = "010"
  val LD_LB  = "011"
  val LD_LHU = "100"
  val LD_LBU = "101"

  // wb_sel
  val WB_ALU = "00"
  val WB_MEM = "01"


  val ALU_XXX    = "0000"
  val ALU_ADD    = "0001"
  val ALU_SUB    = "0010"
  val ALU_AND    = "0011"
  val ALU_OR     = "0100"
  val ALU_XOR    = "0101"
  val ALU_SLT    = "0110"
  val ALU_SLL    = "0111"
  val ALU_SLTU   = "1000"
  val ALU_SRL    = "1001"
  val ALU_SRA    = "1010"
  val ALU_LUI    = "1011"
  val ALU_EQ     = "1100"
  val ALU_COPY_B = "1101"
  val ALU_PC4    = "1110"

  
  val CSR_XXXX =  "0000"
  val CSR_RW   =  "0001"
  val CSR_RS   =  "0010"
  val CSR_ECAL =  "0011"
  val CSR_MRET =  "0100"
  val CSR_BREK =  "0101"
  val O_FENCEI =  "0110"

  import Instructions._
  // format: off
  val decode_default: String =
  //NOTE: A_sel和B_sel并不代表只用这些，可能会有隐含的被使用，例如cond跳转的imm为隐式计算
  //alu的结果通过aSel和bSel计算（当pc或imm与rs同时出现时，译码会按照rs计算，imm被隐式计算） 
  //                                                                               wb_en     illegal?
  //              A_sel   B_sel  imm_sel   alu_op   br_type  st_type ld_type wb_sel  | csr_cmd    |
  //                 |       |     |          |          |      |       |        |   |  |         |
              Seq( A_XXX,  B_XXX, IMM_X, ALU_XXX   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, N, CSR_XXXX, Y).reduce(_ + _)
  val decode_table: TruthTable = TruthTable(Map(
    LUI   ->  Seq( A_XXX,  B_IMM, IMM_U, ALU_LUI   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    AUIPC ->  Seq( A_PC,   B_IMM, IMM_U, ALU_ADD   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    JAL   ->  Seq( A_PC,   B_XXX, IMM_J, ALU_PC4   , BR_JAL, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    JALR  ->  Seq( A_RS1,  B_XXX, IMM_I, ALU_PC4   , BR_JALR,ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),    
    BEQ   ->  Seq( A_RS1,  B_RS2, IMM_B, ALU_EQ    , BR_EQ , ST_XXX, LD_XXX, WB_ALU, N, CSR_XXXX, N),
    BNE   ->  Seq( A_RS1,  B_RS2, IMM_B, ALU_EQ    , BR_NE , ST_XXX, LD_XXX, WB_ALU, N, CSR_XXXX, N),
    BLT   ->  Seq( A_RS1,  B_RS2, IMM_B, ALU_SLT   , BR_LT , ST_XXX, LD_XXX, WB_ALU, N, CSR_XXXX, N),
    BGE   ->  Seq( A_RS1,  B_RS2, IMM_B, ALU_SLT   , BR_GE , ST_XXX, LD_XXX, WB_ALU, N, CSR_XXXX, N),
    BLTU  ->  Seq( A_RS1,  B_RS2, IMM_B, ALU_SLTU  , BR_LTU, ST_XXX, LD_XXX, WB_ALU, N, CSR_XXXX, N),
    BGEU  ->  Seq( A_RS1,  B_RS2, IMM_B, ALU_SLTU  , BR_GEU, ST_XXX, LD_XXX, WB_ALU, N, CSR_XXXX, N),
    LB    ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, ST_XXX, LD_LB , WB_MEM, Y, CSR_XXXX, N),
    LH    ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, ST_XXX, LD_LH , WB_MEM, Y, CSR_XXXX, N),
    LW    ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, ST_XXX, LD_LW , WB_MEM, Y, CSR_XXXX, N),
    LBU   ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, ST_XXX, LD_LBU, WB_MEM, Y, CSR_XXXX, N),
    LHU   ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, ST_XXX, LD_LHU, WB_MEM, Y, CSR_XXXX, N),
    SB    ->  Seq( A_RS1,  B_RS2, IMM_S, ALU_ADD   , BR_XXX, ST_SB , LD_XXX, WB_ALU, N, CSR_XXXX, N),
    SH    ->  Seq( A_RS1,  B_RS2, IMM_S, ALU_ADD   , BR_XXX, ST_SH , LD_XXX, WB_ALU, N, CSR_XXXX, N),
    SW    ->  Seq( A_RS1,  B_RS2, IMM_S, ALU_ADD   , BR_XXX, ST_SW , LD_XXX, WB_ALU, N, CSR_XXXX, N),
    ADDI  ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    SLTI  ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_SLT   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    SLTIU ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_SLTU  , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    XORI  ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_XOR   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    ORI   ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_OR    , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    ANDI  ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_AND   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    SLLI  ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_SLL   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    SRLI  ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_SRL   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    SRAI  ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_SRA   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    ADD   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_ADD   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    SUB   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_SUB   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    SLL   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_SLL   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    SLT   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_SLT   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    SLTU  ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_SLTU  , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    XOR   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_XOR   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    SRL   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_SRL   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    SRA   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_SRA   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    OR    ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_OR    , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    AND   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_AND   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_XXXX, N),
    //
    FENCE_I-> Seq( A_RS1,  B_RS2, IMM_X, ALU_ADD   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, N, O_FENCEI, N),
    //
    CSRRW ->  Seq( A_RS1,  B_CSR, IMM_X, ALU_COPY_B, BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_RW  , N),
    CSRRS ->  Seq( A_RS1,  B_CSR, IMM_X, ALU_COPY_B, BR_XXX, ST_XXX, LD_XXX, WB_ALU, Y, CSR_RS  , N),
    //
    MRET  ->  Seq( A_XXX,  B_XXX, IMM_X, ALU_XXX   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, N, CSR_MRET, N),
    ECALL ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_ADD   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, N, CSR_ECAL, N),
    //
    EBREAK->  Seq( A_RS1,  B_RS2, IMM_X, ALU_ADD   , BR_XXX, ST_XXX, LD_XXX, WB_ALU, N, CSR_BREK, N))
    .map({ case (k, v) => k -> BitPat(s"b${v.reduce(_ + _)}") }), BitPat(s"b$decode_default"));
  // format: on
}

import Control._
class DecodeSignals extends Bundle{
  val inst    =Input(UInt(32.W))
  val aSel    =Output(UInt(A_XXX.length.W))
  val bSel    =Output(UInt(B_XXX.length.W))
  val immType =Output(UInt(IMM_X.length.W))
  val aluOp   =Output(UInt(ALU_XXX.length.W))
  val brType  =Output(UInt(BR_XXX.length.W))
  val stType  =Output(UInt(ST_XXX.length.W))
  val ldType  =Output(UInt(LD_XXX.length.W))
  val wbSel   =Output(UInt(WB_ALU.length.W))
  val rfWen   =Output(UInt(Y.length.W))
  val csrOp   =Output(UInt(CSR_XXXX.length.W))
  val illigal =Output(UInt(Y.length.W))
}

class Decode extends Module{
  val io = IO(new DecodeSignals)


  val decode_result=decoder(QMCMinimizer,io.inst,Control.decode_table)

  val entries = Seq( 
    io.aSel,
    io.bSel,
    io.immType,
    io.aluOp,
    io.brType,
    io.stType,
    io.ldType,
    io.wbSel,
    io.rfWen,
    io.csrOp,
    io.illigal
  ) //NOTE:chisel魔法，自动连线,自动匹配位宽
  var i = 0
  for (entry <- entries.reverse) {
    val entry_width = entry.getWidth
    if (entry_width == 1) {
      entry := decode_result(i)
    } else {
      entry := decode_result(i + entry_width - 1, i)
    }
    i += entry_width
  }
}

