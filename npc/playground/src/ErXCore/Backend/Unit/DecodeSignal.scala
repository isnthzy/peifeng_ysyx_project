package ErXCore

import chisel3._
import chisel3.util._
import chisel3.util.experimental.decode._

object DecodeInstructions {
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

object DecodeSignal {
  def isBranch(x: UInt): Bool = x(2).asBool
  def isJump(x: UInt): Bool   = x(3).asBool
  def isLoadInst(x: UInt): Bool  = x(3).asBool 
  def isStoreInst(x: UInt): Bool = x(4).asBool
  def isLoadStore(x: UInt): Bool = x(3,4).asUInt.xorR
  def isJmpBranch(x: UInt): Bool = x(2,3).asUInt.xorR
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
  val BR_LTU = "0101"
  val BR_LT  = "0110"
  val BR_EQ  = "0111"
  val BR_GEU = "0100"
  val BR_GE  = "0101"
  val BR_NE  = "0110"
  val BR_JAL = "1000"
  val BR_JALR= "1001"

  // loadAndStore
  val LS_XXX = "00000"
  val ST_SB  = "10001"
  val ST_SH  = "10010"
  val ST_SW  = "10011"

  val LD_XXX = "01000"
  val LD_LW  = "01001"
  val LD_LH  = "01010"
  val LD_LB  = "01011"
  val LD_LHU = "01100"
  val LD_LBU = "01101"
  //def isLoad、isStore check the last three digits (2.W) ++ __(3.W)
  //def loadEn、StoreEn check the first two positions (2.W)___ ++ (3.W)


  // wb_sel
  val FU_ALU = "00"
  val FU_MEM = "01"
  val FU_BRU = "10"

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
  val ILLEGAL  =  "0111"

  import DecodeInstructions._
  // format: off
  val decode_default: String =
  //NOTE: A_sel和B_sel并不代表只用这些，可能会有隐含的被使用，例如cond跳转的imm为隐式计算
  //alu的结果通过aSel和bSel计算（当pc或imm与rs同时出现时，译码会按照rs计算，imm被隐式计算） 
  //                                                                         wb_en     
  //              A_sel   B_sel  imm_sel   alu_op   br_type  ls_type  wb_sel  | csr_cmd   
  //                 |       |     |          |          |      |        |   |  |        
              Seq( A_XXX,  B_XXX, IMM_X, ALU_XXX   , BR_XXX, LS_XXX, FU_ALU, N, CSR_XXXX).reduce(_ + _)
  val decode_table: TruthTable = TruthTable(Map(
    LUI   ->  Seq( A_XXX,  B_IMM, IMM_U, ALU_LUI   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    AUIPC ->  Seq( A_PC,   B_IMM, IMM_U, ALU_ADD   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    JAL   ->  Seq( A_PC,   B_XXX, IMM_J, ALU_PC4   , BR_JAL, LS_XXX, FU_ALU, Y, CSR_XXXX),
    JALR  ->  Seq( A_RS1,  B_XXX, IMM_I, ALU_PC4   , BR_JALR,LS_XXX, FU_ALU, Y, CSR_XXXX),    
    BEQ   ->  Seq( A_RS1,  B_RS2, IMM_B, ALU_EQ    , BR_EQ , LS_XXX, FU_ALU, N, CSR_XXXX),
    BNE   ->  Seq( A_RS1,  B_RS2, IMM_B, ALU_EQ    , BR_NE , LS_XXX, FU_ALU, N, CSR_XXXX),
    BLT   ->  Seq( A_RS1,  B_RS2, IMM_B, ALU_SLT   , BR_LT , LS_XXX, FU_ALU, N, CSR_XXXX),
    BGE   ->  Seq( A_RS1,  B_RS2, IMM_B, ALU_SLT   , BR_GE , LS_XXX, FU_ALU, N, CSR_XXXX),
    BLTU  ->  Seq( A_RS1,  B_RS2, IMM_B, ALU_SLTU  , BR_LTU, LS_XXX, FU_ALU, N, CSR_XXXX),
    BGEU  ->  Seq( A_RS1,  B_RS2, IMM_B, ALU_SLTU  , BR_GEU, LS_XXX, FU_ALU, N, CSR_XXXX),
    LB    ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, LD_LB , FU_MEM, Y, CSR_XXXX),
    LH    ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, LD_LH , FU_MEM, Y, CSR_XXXX),
    LW    ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, LD_LW , FU_MEM, Y, CSR_XXXX),
    LBU   ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, LD_LBU, FU_MEM, Y, CSR_XXXX),
    LHU   ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, LD_LHU, FU_MEM, Y, CSR_XXXX),
    SB    ->  Seq( A_RS1,  B_RS2, IMM_S, ALU_ADD   , BR_XXX, ST_SB , FU_ALU, N, CSR_XXXX),
    SH    ->  Seq( A_RS1,  B_RS2, IMM_S, ALU_ADD   , BR_XXX, ST_SH , FU_ALU, N, CSR_XXXX),
    SW    ->  Seq( A_RS1,  B_RS2, IMM_S, ALU_ADD   , BR_XXX, ST_SW , FU_ALU, N, CSR_XXXX),
    ADDI  ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    SLTI  ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_SLT   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    SLTIU ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_SLTU  , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    XORI  ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_XOR   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    ORI   ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_OR    , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    ANDI  ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_AND   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    SLLI  ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_SLL   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    SRLI  ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_SRL   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    SRAI  ->  Seq( A_RS1,  B_IMM, IMM_I, ALU_SRA   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    ADD   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_ADD   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    SUB   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_SUB   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    SLL   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_SLL   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    SLT   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_SLT   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    SLTU  ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_SLTU  , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    XOR   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_XOR   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    SRL   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_SRL   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    SRA   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_SRA   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    OR    ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_OR    , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    AND   ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_AND   , BR_XXX, LS_XXX, FU_ALU, Y, CSR_XXXX),
    //
    FENCE_I-> Seq( A_RS1,  B_RS2, IMM_X, ALU_ADD   , BR_XXX, LS_XXX, FU_ALU, N, O_FENCEI),
    //
    CSRRW ->  Seq( A_RS1,  B_CSR, IMM_X, ALU_COPY_B, BR_XXX, LS_XXX, FU_ALU, Y, CSR_RW  ),
    CSRRS ->  Seq( A_RS1,  B_CSR, IMM_X, ALU_COPY_B, BR_XXX, LS_XXX, FU_ALU, Y, CSR_RS  ),
    //
    MRET  ->  Seq( A_XXX,  B_XXX, IMM_X, ALU_XXX   , BR_XXX, LS_XXX, FU_ALU, N, CSR_MRET),
    ECALL ->  Seq( A_RS1,  B_RS2, IMM_X, ALU_ADD   , BR_XXX, LS_XXX, FU_ALU, N, CSR_ECAL),
    //
    EBREAK->  Seq( A_RS1,  B_RS2, IMM_X, ALU_ADD   , BR_XXX, LS_XXX, FU_ALU, N, CSR_BREK))
    .map({ case (k, v) => k -> BitPat(s"b${v.reduce(_ + _)}") }), BitPat(s"b$decode_default"));
  // format: on
}

import DecodeSignal._

class DecodeSignals extends Module{
  val io = IO(new Bundle {
    val inst    = Input(UInt(32.W))
    val aSel    = Output(UInt(A_XXX.length.W))
    val bSel    = Output(UInt(B_XXX.length.W))
    val immType = Output(UInt(IMM_X.length.W))
    val aluOp   = Output(UInt(ALU_XXX.length.W))
    val brType  = Output(UInt(BR_XXX.length.W))
    val lsType  = Output(UInt(LS_XXX.length.W))
    val fuSel   = Output(UInt(FU_ALU.length.W))
    val rfWen   = Output(UInt(Y.length.W))
    val csrOp   = Output(UInt(CSR_XXXX.length.W))
  })

  val decode_result=decoder(QMCMinimizer,io.inst,DecodeSignal.decode_table)

  val entries = Seq( 
    io.aSel,
    io.bSel,
    io.immType,
    io.aluOp,
    io.brType,
    io.lsType,
    io.fuSel,
    io.rfWen,
    io.csrOp
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




