import chisel3._
import chisel3.util._

object Alus {
  val ALU_ADD    = 0.U(4.W)
  val ALU_SUB    = 1.U(4.W)
  val ALU_AND    = 2.U(4.W)
  val ALU_OR     = 3.U(4.W)
  val ALU_XOR    = 4.U(4.W)
  val ALU_SLT    = 5.U(4.W)
  val ALU_SLL    = 6.U(4.W)
  val ALU_SLTU   = 7.U(4.W)
  val ALU_SRL    = 8.U(4.W)
  val ALU_SRA    = 9.U(4.W)
  val ALU_COPY_B = 10.U(4.W)
  val ALU_LUI    = 11.U(4.W)
  val ALU_XXX    = 12.U(4.W)
}

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

  // Csr
  def CSRRW = BitPat("b?????????????????001?????1110011")
  def CSRRS = BitPat("b?????????????????010?????1110011")

  def ECALL  = BitPat("b00000000000000000000000001110011")
  def EBREAK = BitPat("b00000000000100000000000001110011")
  def MRET   = BitPat("b00110000001000000000000001110011")

}

object Control {
  val Y = true.B
  val N = false.B

  // pc_sel
  val PC_XXX = 0.U(1.W)
  val PC_EPC = 1.U(1.W)

  // A_sel
  val A_XXX = 0.U(1.W)
  val A_PC  = 0.U(1.W)
  val A_RS1 = 1.U(1.W)

  // B_sel
  val B_XXX = 0.U(2.W)
  val B_IMM = 0.U(2.W)
  val B_RS2 = 1.U(2.W)
  val B_CSR = 2.U(2.W)

  // imm_sel
  val IMM_X = 0.U(3.W)
  val IMM_I = 1.U(3.W)
  val IMM_S = 2.U(3.W)
  val IMM_U = 3.U(3.W)
  val IMM_J = 4.U(3.W)
  val IMM_B = 5.U(3.W)
  // val IMM_Z = 6.U(3.W)

  // br_type
  val BR_XXX = 0.U(4.W)
  val BR_LTU = 1.U(4.W)
  val BR_LT  = 2.U(4.W)
  val BR_EQ  = 3.U(4.W)
  val BR_GEU = 4.U(4.W)
  val BR_GE  = 5.U(4.W)
  val BR_NE  = 6.U(4.W)
  val BR_JAL = 7.U(4.W)
  val BR_JR  = 8.U(4.W)

  // st_type
  val ST_XXX = 0.U(3.W)
  val ST_SB  = 1.U(3.W)
  val ST_SH  = 2.U(3.W)
  val ST_SW  = 3.U(3.W)

  // ld_type
  val LD_XXX = 0.U(3.W)
  val LD_LW  = 1.U(3.W)
  val LD_LH  = 2.U(3.W)
  val LD_LB  = 3.U(3.W)
  val LD_LHU = 4.U(3.W)
  val LD_LBU = 5.U(3.W)

  // wb_sel
  val WB_ALU = 0.U(2.W)
  val WB_MEM = 1.U(2.W)
  val WB_PC4 = 2.U(2.W)
  val WB_CSR = 3.U(2.W)

  import Instructions._
  import Alus._
  // format: off
  val default =
  //                                                               kill                        wb_en  illegal?
  //             pc_sel    A_sel   B_sel  imm_sel   alu_op   br_type  |  st_type ld_type wb_sel  | csr_cmd |
  //               |         |       |     |          |          |    |     |       |       |    |  |      |
             List(PC_XXX  , A_XXX,  B_XXX, IMM_X, ALU_XXX   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, Y)
  val map = Array(
    LUI   -> List(PC_XXX  , A_XXX,  B_IMM, IMM_U, ALU_LUI   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    AUIPC -> List(PC_XXX  , A_PC,   B_IMM, IMM_U, ALU_ADD   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    JAL   -> List(PC_XXX  , A_PC,   B_IMM, IMM_J, ALU_XXX   , BR_JAL, Y, ST_XXX, LD_XXX, WB_PC4, Y, CSR.N, N),
    JALR  -> List(PC_XXX  , A_RS1,  B_IMM, IMM_I, ALU_XXX   , BR_JR , Y, ST_XXX, LD_XXX, WB_PC4, Y, CSR.N, N),    
    BEQ   -> List(PC_XXX  , A_PC,   B_IMM, IMM_B, ALU_ADD   , BR_EQ , N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, N),
    BNE   -> List(PC_XXX  , A_PC,   B_IMM, IMM_B, ALU_ADD   , BR_NE , N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, N),
    BLT   -> List(PC_XXX  , A_PC,   B_IMM, IMM_B, ALU_ADD   , BR_LT , N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, N),
    BGE   -> List(PC_XXX  , A_PC,   B_IMM, IMM_B, ALU_ADD   , BR_GE , N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, N),
    BLTU  -> List(PC_XXX  , A_PC,   B_IMM, IMM_B, ALU_ADD   , BR_LTU, N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, N),
    BGEU  -> List(PC_XXX  , A_PC,   B_IMM, IMM_B, ALU_ADD   , BR_GEU, N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, N),
    LB    -> List(PC_XXX  , A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, Y, ST_XXX, LD_LB , WB_MEM, Y, CSR.N, N),
    LH    -> List(PC_XXX  , A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, Y, ST_XXX, LD_LH , WB_MEM, Y, CSR.N, N),
    LW    -> List(PC_XXX  , A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, Y, ST_XXX, LD_LW , WB_MEM, Y, CSR.N, N),
    LBU   -> List(PC_XXX  , A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, Y, ST_XXX, LD_LBU, WB_MEM, Y, CSR.N, N),
    LHU   -> List(PC_XXX  , A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, Y, ST_XXX, LD_LHU, WB_MEM, Y, CSR.N, N),
    SB    -> List(PC_XXX  , A_RS1,  B_IMM, IMM_S, ALU_ADD   , BR_XXX, N, ST_SB , LD_XXX, WB_ALU, N, CSR.N, N),
    SH    -> List(PC_XXX  , A_RS1,  B_IMM, IMM_S, ALU_ADD   , BR_XXX, N, ST_SH , LD_XXX, WB_ALU, N, CSR.N, N),
    SW    -> List(PC_XXX  , A_RS1,  B_IMM, IMM_S, ALU_ADD   , BR_XXX, N, ST_SW , LD_XXX, WB_ALU, N, CSR.N, N),
    ADDI  -> List(PC_XXX  , A_RS1,  B_IMM, IMM_I, ALU_ADD   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SLTI  -> List(PC_XXX  , A_RS1,  B_IMM, IMM_I, ALU_SLT   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SLTIU -> List(PC_XXX  , A_RS1,  B_IMM, IMM_I, ALU_SLTU  , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    XORI  -> List(PC_XXX  , A_RS1,  B_IMM, IMM_I, ALU_XOR   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    ORI   -> List(PC_XXX  , A_RS1,  B_IMM, IMM_I, ALU_OR    , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    ANDI  -> List(PC_XXX  , A_RS1,  B_IMM, IMM_I, ALU_AND   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SLLI  -> List(PC_XXX  , A_RS1,  B_IMM, IMM_I, ALU_SLL   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SRLI  -> List(PC_XXX  , A_RS1,  B_IMM, IMM_I, ALU_SRL   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SRAI  -> List(PC_XXX  , A_RS1,  B_IMM, IMM_I, ALU_SRA   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    ADD   -> List(PC_XXX  , A_RS1,  B_RS2, IMM_X, ALU_ADD   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SUB   -> List(PC_XXX  , A_RS1,  B_RS2, IMM_X, ALU_SUB   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SLL   -> List(PC_XXX  , A_RS1,  B_RS2, IMM_X, ALU_SLL   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SLT   -> List(PC_XXX  , A_RS1,  B_RS2, IMM_X, ALU_SLT   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SLTU  -> List(PC_XXX  , A_RS1,  B_RS2, IMM_X, ALU_SLTU  , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    XOR   -> List(PC_XXX  , A_RS1,  B_RS2, IMM_X, ALU_XOR   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SRL   -> List(PC_XXX  , A_RS1,  B_RS2, IMM_X, ALU_SRL   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SRA   -> List(PC_XXX  , A_RS1,  B_RS2, IMM_X, ALU_SRA   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    OR    -> List(PC_XXX  , A_RS1,  B_RS2, IMM_X, ALU_OR    , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    AND   -> List(PC_XXX  , A_RS1,  B_RS2, IMM_X, ALU_AND   , BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    //
    CSRRW -> List(PC_XXX  , A_XXX,  B_CSR, IMM_X, ALU_COPY_B, BR_XXX, Y, ST_XXX, LD_XXX, WB_CSR, Y, CSR.W    , N),
    CSRRS -> List(PC_XXX  , A_XXX,  B_CSR, IMM_X, ALU_COPY_B, BR_XXX, Y, ST_XXX, LD_XXX, WB_CSR, Y, CSR.S    , N),
    //
    MRET  -> List(PC_EPC  , A_XXX,  B_XXX, IMM_X, ALU_XXX   , BR_XXX, Y, ST_XXX, LD_XXX, WB_CSR, N, CSR.MRET , N),
    ECALL -> List(PC_EPC  , A_RS1,  B_RS2, IMM_X, ALU_ADD   , BR_XXX, N, ST_XXX, LD_XXX, WB_CSR, N, CSR.ECALL, N),
    //
    EBREAK-> List(PC_XXX  , A_RS1,  B_RS2, IMM_X, ALU_ADD   , BR_XXX, N, ST_XXX, LD_XXX, WB_CSR, N, CSR.BREAK, N))
  // format: on
}

class DecodeSignals extends Bundle {
  val inst     = Input(UInt(32.W))
  val pc_sel   = Output(UInt(1.W))
  val inst_inv = Output(Bool())
  val A_sel    = Output(UInt(1.W))
  val B_sel    = Output(UInt(2.W))
  val imm_sel  = Output(UInt(3.W))
  val alu_op   = Output(UInt(4.W))
  val br_type  = Output(UInt(4.W))
  val st_type  = Output(UInt(3.W))
  val ld_type  = Output(UInt(3.W))
  val wb_sel   = Output(UInt(2.W))
  val wb_en    = Output(Bool())
  val csr_cmd  = Output(UInt(5.W))
  val illegal  = Output(Bool())
}

class Decode extends Module {
  val io          = IO(new DecodeSignals)
  val ctrlSignals = ListLookup(io.inst, Control.default, Control.map)

  io.pc_sel    := ctrlSignals(0)
  //选择pc或者csr提供的epc
  io.inst_inv  := ctrlSignals(6).asBool
  //当该条指令是非法指令时，置高信号
  io.A_sel   := ctrlSignals(1)
  io.B_sel   := ctrlSignals(2)
  io.imm_sel := ctrlSignals(3)
  io.alu_op  := ctrlSignals(4)
  io.br_type := ctrlSignals(5)
  io.st_type := ctrlSignals(7)

  io.ld_type := ctrlSignals(8)
  io.wb_sel  := ctrlSignals(9)
  io.wb_en   := ctrlSignals(10).asBool
  io.csr_cmd := ctrlSignals(11)
  io.illegal := ctrlSignals(12)
}
