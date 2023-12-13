// Generated by CIRCT firtool-1.56.0
// Standard header to adapt well known macros to our needs.

// Users can define 'PRINTF_COND' to add an extra gate to prints.
`ifndef PRINTF_COND_
  `ifdef PRINTF_COND
    `define PRINTF_COND_ (`PRINTF_COND)
  `else  // PRINTF_COND
    `define PRINTF_COND_ 1
  `endif // PRINTF_COND
`endif // not def PRINTF_COND_

module ID_stage(	// @[<stdin>:40:3]
  input         clock,	// @[<stdin>:41:11]
                reset,	// @[<stdin>:42:11]
  input  [31:0] io_pc,	// @[playground/src/ID_stage.scala:6:14]
                io_nextpc,	// @[playground/src/ID_stage.scala:6:14]
                io_inst,	// @[playground/src/ID_stage.scala:6:14]
                io_result,	// @[playground/src/ID_stage.scala:6:14]
                io_f_dbus_snpc,	// @[playground/src/ID_stage.scala:6:14]
  output [31:0] io_Imm,	// @[playground/src/ID_stage.scala:6:14]
  output        io_is_not_jalr,	// @[playground/src/ID_stage.scala:6:14]
                io_is_jump,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_is_ebreak,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_data_wen,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_result_is_imm,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_result_is_snpc,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_src_is_sign,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_src1_is_pc,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_src2_is_imm,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_src2_is_shamt_imm,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_src2_is_shamt_src,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_sram_valid,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_sram_wen,	// @[playground/src/ID_stage.scala:6:14]
  output [3:0]  io_d_ebus_wmask,	// @[playground/src/ID_stage.scala:6:14]
  output [31:0] io_d_ebus_snpc,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_imm,	// @[playground/src/ID_stage.scala:6:14]
  output [4:0]  io_d_ebus_src1,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_src2,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_rd,	// @[playground/src/ID_stage.scala:6:14]
  output        io_d_ebus_alu_op_0,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_alu_op_1,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_alu_op_2,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_alu_op_3,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_alu_op_4,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_alu_op_5,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_alu_op_6,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_alu_op_7,	// @[playground/src/ID_stage.scala:6:14]
                io_d_ebus_alu_op_8	// @[playground/src/ID_stage.scala:6:14]
);

  wire        IsaI_ebreak = 1'h0;	// @[playground/src/ID_stage.scala:21:32, :29:21]
  wire        IsaU_lui = io_inst[6:0] == 7'h37;	// @[playground/src/ID_stage.scala:24:32, :38:26]
  wire        IsaU_auipc = io_inst[6:0] == 7'h17;	// @[playground/src/ID_stage.scala:24:32, :38:26, :39:26]
  wire        IsaU_jal = io_inst[6:0] == 7'h6F;	// @[playground/src/ID_stage.scala:24:32, :38:26, :40:26]
  wire [9:0]  _GEN = {io_inst[14:12], io_inst[6:0]};	// @[playground/src/ID_stage.scala:38:26, :41:26]
  wire        IsaI_jalr = _GEN == 10'h67;	// @[playground/src/ID_stage.scala:21:32, :41:26]
  wire        IsaB_beq = _GEN == 10'h63;	// @[playground/src/ID_stage.scala:23:32, :41:26, :42:26]
  wire        IsaB_bne = _GEN == 10'hE3;	// @[playground/src/ID_stage.scala:23:32, :41:26, :43:26]
  wire        IsaB_blt = _GEN == 10'h263;	// @[playground/src/ID_stage.scala:23:32, :41:26, :44:26]
  wire        IsaB_bge = _GEN == 10'h2E3;	// @[playground/src/ID_stage.scala:23:32, :41:26, :45:26]
  wire        IsaB_bltu = _GEN == 10'h363;	// @[playground/src/ID_stage.scala:23:32, :41:26, :46:26]
  wire        IsaB_bgeu = _GEN == 10'h3E3;	// @[playground/src/ID_stage.scala:23:32, :41:26, :47:26]
  wire        IsaI_lb = _GEN == 10'h3;	// @[playground/src/ID_stage.scala:21:32, :41:26, :48:26]
  wire        IsaI_lh = _GEN == 10'h83;	// @[playground/src/ID_stage.scala:21:32, :41:26, :49:26]
  wire        IsaI_lw = _GEN == 10'h103;	// @[playground/src/ID_stage.scala:21:32, :41:26, :50:26]
  wire        IsaI_lbu = _GEN == 10'h203;	// @[playground/src/ID_stage.scala:21:32, :41:26, :51:26]
  wire        IsaI_lhu = _GEN == 10'h283;	// @[playground/src/ID_stage.scala:21:32, :41:26, :52:26]
  wire        IsaS_sb = _GEN == 10'h23;	// @[playground/src/ID_stage.scala:22:32, :41:26, :53:26]
  wire        IsaS_sh = _GEN == 10'hA3;	// @[playground/src/ID_stage.scala:22:32, :41:26, :54:26]
  wire        IsaS_sw = _GEN == 10'h123;	// @[playground/src/ID_stage.scala:22:32, :41:26, :55:26]
  wire        IsaI_addi = _GEN == 10'h13;	// @[playground/src/ID_stage.scala:21:32, :41:26, :56:26]
  wire        IsaI_slti = _GEN == 10'h113;	// @[playground/src/ID_stage.scala:21:32, :41:26, :57:26]
  wire        IsaI_sltiu = _GEN == 10'h193;	// @[playground/src/ID_stage.scala:21:32, :41:26, :58:26]
  wire        IsaI_xori = _GEN == 10'h213;	// @[playground/src/ID_stage.scala:21:32, :41:26, :59:26]
  wire        IsaI_ori = _GEN == 10'h313;	// @[playground/src/ID_stage.scala:21:32, :41:26, :60:26]
  wire        IsaI_andi = _GEN == 10'h393;	// @[playground/src/ID_stage.scala:21:32, :41:26, :61:26]
  wire [16:0] _GEN_0 = {io_inst[31:25], io_inst[14:12], io_inst[6:0]};	// @[playground/src/ID_stage.scala:38:26, :41:26, :62:26]
  wire        IsaI_slli = _GEN_0 == 17'h93;	// @[playground/src/ID_stage.scala:21:32, :62:26]
  wire        IsaI_srli = _GEN_0 == 17'h293;	// @[playground/src/ID_stage.scala:21:32, :62:26, :63:26]
  wire        IsaI_srai = _GEN_0 == 17'h8293;	// @[playground/src/ID_stage.scala:21:32, :62:26, :64:26]
  wire        IsaR_add = _GEN_0 == 17'h33;	// @[playground/src/ID_stage.scala:20:32, :62:26, :65:26]
  wire        IsaR_sub = _GEN_0 == 17'h8033;	// @[playground/src/ID_stage.scala:20:32, :62:26, :66:26]
  wire        IsaR_sll = _GEN_0 == 17'hB3;	// @[playground/src/ID_stage.scala:20:32, :62:26, :67:26]
  wire        IsaR_slt = _GEN_0 == 17'h133;	// @[playground/src/ID_stage.scala:20:32, :62:26, :68:26]
  wire        IsaR_sltu = _GEN_0 == 17'h1B3;	// @[playground/src/ID_stage.scala:20:32, :62:26, :69:26]
  wire        IsaR_xor = _GEN_0 == 17'h233;	// @[playground/src/ID_stage.scala:20:32, :62:26, :70:26]
  wire        IsaR_srl = _GEN_0 == 17'h2B3;	// @[playground/src/ID_stage.scala:20:32, :62:26, :71:26]
  wire        IsaR_sra = _GEN_0 == 17'h82B3;	// @[playground/src/ID_stage.scala:20:32, :62:26, :72:26]
  wire        IsaR_or = _GEN_0 == 17'h333;	// @[playground/src/ID_stage.scala:20:32, :62:26, :73:26]
  wire        IsaR_and = _GEN_0 == 17'h3B3;	// @[playground/src/ID_stage.scala:20:32, :62:26, :74:26]
  wire [4:0]  _io_Imm_T =
    {|{IsaI_jalr,
       IsaI_lb,
       IsaI_lh,
       IsaI_lw,
       IsaI_lbu,
       IsaI_lhu,
       IsaI_addi,
       IsaI_slti,
       IsaI_sltiu,
       IsaI_xori,
       IsaI_ori,
       IsaI_andi,
       IsaI_slli,
       IsaI_srli,
       IsaI_srai,
       IsaI_ebreak},
     |{IsaS_sb, IsaS_sh, IsaS_sw},
     |{IsaB_beq, IsaB_bne, IsaB_blt, IsaB_bge, IsaB_bltu, IsaB_bgeu},
     |{IsaU_lui, IsaU_auipc, IsaU_jal},
     IsaU_jal};	// @[playground/src/ID_stage.scala:21:32, :22:32, :23:32, :24:32, :84:{32,39}, :85:{32,39}, :86:{32,39}, :87:{32,39}, :89:31]
  wire [31:0] _io_Imm_output =
    _io_Imm_T == 5'h3
      ? {{12{io_inst[19]}}, io_inst[19:12], io_inst[20], io_inst[30:21], 1'h0}
      : _io_Imm_T == 5'h2
          ? {io_inst[31:12], 12'h0}
          : _io_Imm_T == 5'h4
              ? {{21{io_inst[7]}}, io_inst[30:25], io_inst[11:8], 1'h0}
              : _io_Imm_T == 5'h8
                  ? {{20{io_inst[31]}}, io_inst[31:25], io_inst[11:7]}
                  : _io_Imm_T == 5'h10 ? {{20{io_inst[31]}}, io_inst[31:20]} : 32'h0;	// @[playground/src/Bundle.scala:101:{10,15,37}, playground/src/ID_stage.scala:27:25, :28:{29,46}, :29:{21,42,54,71}, :30:{21,29}, :31:{42,59,72}, :89:{31,43}]
  wire        _io_d_ebus_alu_op_0_T = IsaI_addi | IsaR_add;	// @[playground/src/ID_stage.scala:20:32, :21:32, :99:36]
  wire        _singal_dpi_io_func_flag_T = IsaU_jal | IsaI_jalr;	// @[playground/src/ID_stage.scala:21:32, :24:32, :105:40]
  wire        _io_d_ebus_alu_op_7_T = IsaI_srai | IsaR_sra;	// @[playground/src/ID_stage.scala:20:32, :21:32, :106:41]
  wire        _io_d_ebus_sram_valid_output =
    IsaI_lb | IsaI_lh | IsaI_lw | IsaI_lbu | IsaI_lhu | IsaS_sb | IsaS_sh | IsaS_sw;	// @[playground/src/ID_stage.scala:21:32, :22:32, :113:104]
  wire        rs1_eq_rs2 = io_inst[19:15] == io_inst[24:20];	// @[playground/src/ID_stage.scala:32:25, :33:25, :150:31]
  wire        rs1_lt_rs2_s = $signed(io_inst[19:15]) < $signed(io_inst[24:20]);	// @[playground/src/ID_stage.scala:32:25, :33:25, :151:38]
  wire        rs1_lt_rs2_u = io_inst[19:15] < io_inst[24:20];	// @[playground/src/ID_stage.scala:32:25, :33:25, :152:31]
  singal_dpi singal_dpi (	// @[playground/src/ID_stage.scala:162:26]
    .clock       (clock),
    .reset       (reset),
    .pc          (io_pc),
    .nextpc      (io_nextpc),
    .inst        (io_inst),
    .rd          ({27'h0, io_inst[11:7]}),	// @[playground/src/ID_stage.scala:28:46, :39:26, :168:29]
    .is_jal      (IsaU_jal),	// @[playground/src/ID_stage.scala:24:32]
    .func_flag   (_singal_dpi_io_func_flag_T),	// @[playground/src/ID_stage.scala:105:40]
    .ebreak_flag (IsaI_ebreak),	// @[playground/src/ID_stage.scala:21:32]
    .inv_flag
      ((|io_inst) & {IsaB_beq, IsaB_bne, IsaB_blt, IsaB_bge, IsaB_bltu, IsaB_bgeu} == 6'h0
       & {IsaI_jalr,
          IsaI_lb,
          IsaI_lh,
          IsaI_lw,
          IsaI_lbu,
          IsaI_lhu,
          IsaI_addi,
          IsaI_slti,
          IsaI_sltiu,
          IsaI_xori,
          IsaI_ori,
          IsaI_andi,
          IsaI_slli,
          IsaI_srli,
          IsaI_srai,
          IsaI_ebreak} == 16'h0
       & {IsaR_add,
          IsaR_sub,
          IsaR_sll,
          IsaR_slt,
          IsaR_sltu,
          IsaR_xor,
          IsaR_srl,
          IsaR_sra,
          IsaR_or,
          IsaR_and} == 10'h0 & {IsaS_sb, IsaS_sh, IsaS_sw} == 3'h0
       & {IsaU_lui, IsaU_auipc, IsaU_jal} == 3'h0),	// @[playground/src/ID_stage.scala:20:32, :21:32, :22:32, :23:32, :24:32, :77:{31,46,53,68,75,90,97,112,119,127,134,141}]
    .ret_reg     (io_result)
  );
  assign io_Imm = _io_Imm_output;	// @[<stdin>:40:3, playground/src/ID_stage.scala:89:43]
  assign io_is_not_jalr =
    IsaU_jal | IsaB_beq | IsaB_bne | IsaB_blt | IsaB_bltu | IsaB_bge | IsaB_bgeu;	// @[<stdin>:40:3, playground/src/ID_stage.scala:23:32, :24:32, :124:86]
  assign io_is_jump =
    _singal_dpi_io_func_flag_T | IsaB_beq & rs1_eq_rs2 | IsaB_bne & ~rs1_eq_rs2 | IsaB_blt
    & rs1_lt_rs2_s | IsaB_bltu & rs1_lt_rs2_u | IsaB_bge & ~rs1_lt_rs2_s | IsaB_bgeu
    & ~rs1_lt_rs2_u;	// @[<stdin>:40:3, playground/src/ID_stage.scala:23:32, :105:40, :150:31, :151:38, :152:31, :155:26, :156:{26,29}, :157:26, :158:27, :159:{26,29}, :160:{15,27,30}]
  assign io_d_ebus_is_ebreak = IsaI_ebreak;	// @[<stdin>:40:3, playground/src/ID_stage.scala:21:32]
  assign io_d_ebus_data_wen =
    _io_d_ebus_alu_op_0_T | IsaI_andi | IsaR_and | IsaU_lui | IsaR_slt | IsaR_sltu
    | IsaR_sub | IsaI_ori | IsaR_or | IsaI_xori | IsaR_xor | IsaI_jalr | IsaU_jal
    | IsaU_auipc | IsaI_slti | IsaI_sltiu | IsaI_slli | IsaI_srai | IsaI_srli | IsaR_sll
    | IsaR_sra | IsaR_srl;	// @[<stdin>:40:3, playground/src/ID_stage.scala:20:32, :21:32, :24:32, :99:36, :103:50]
  assign io_d_ebus_result_is_imm = IsaU_lui;	// @[<stdin>:40:3, playground/src/ID_stage.scala:24:32]
  assign io_d_ebus_result_is_snpc = _singal_dpi_io_func_flag_T;	// @[<stdin>:40:3, playground/src/ID_stage.scala:105:40]
  assign io_d_ebus_src_is_sign = _io_d_ebus_alu_op_7_T | IsaR_slt | IsaB_blt | IsaB_bltu;	// @[<stdin>:40:3, playground/src/ID_stage.scala:20:32, :23:32, :106:{41,74}]
  assign io_d_ebus_src1_is_pc = IsaU_auipc;	// @[<stdin>:40:3, playground/src/ID_stage.scala:24:32]
  assign io_d_ebus_src2_is_imm =
    IsaI_addi | IsaI_slti | IsaI_sltiu | IsaI_xori | IsaI_ori | IsaI_andi | IsaI_jalr
    | IsaU_auipc | IsaI_lb | IsaI_lh | IsaI_lw | IsaI_lbu | IsaI_lhu;	// @[<stdin>:40:3, playground/src/ID_stage.scala:21:32, :24:32, :110:46]
  assign io_d_ebus_src2_is_shamt_imm = IsaI_slli | IsaI_srai | IsaI_srli;	// @[<stdin>:40:3, playground/src/ID_stage.scala:21:32, :111:56]
  assign io_d_ebus_src2_is_shamt_src = IsaR_sll | IsaR_sra | IsaR_srl;	// @[<stdin>:40:3, playground/src/ID_stage.scala:20:32, :112:54]
  assign io_d_ebus_sram_valid = _io_d_ebus_sram_valid_output;	// @[<stdin>:40:3, playground/src/ID_stage.scala:113:104]
  assign io_d_ebus_sram_wen = IsaS_sb | IsaS_sh | IsaS_sw;	// @[<stdin>:40:3, playground/src/ID_stage.scala:22:32, :114:52]
  assign io_d_ebus_wmask =
    IsaI_lb | IsaI_lbu ? 4'h1 : IsaI_lh | IsaI_lhu ? 4'h3 : {4{IsaI_lw}};	// @[<stdin>:40:3, playground/src/ID_stage.scala:21:32, :115:{37,46}, :116:{38,47}, :117:40]
  assign io_d_ebus_snpc = io_f_dbus_snpc;	// @[<stdin>:40:3]
  assign io_d_ebus_imm = _io_Imm_output;	// @[<stdin>:40:3, playground/src/ID_stage.scala:89:43]
  assign io_d_ebus_src1 = io_inst[19:15];	// @[<stdin>:40:3, playground/src/ID_stage.scala:33:25]
  assign io_d_ebus_src2 = io_inst[24:20];	// @[<stdin>:40:3, playground/src/ID_stage.scala:32:25]
  assign io_d_ebus_rd = io_inst[11:7];	// @[<stdin>:40:3, playground/src/ID_stage.scala:28:46]
  assign io_d_ebus_alu_op_0 =
    _io_d_ebus_alu_op_0_T | IsaI_ebreak | IsaI_jalr | IsaU_auipc
    | _io_d_ebus_sram_valid_output;	// @[<stdin>:40:3, playground/src/ID_stage.scala:21:32, :24:32, :99:36, :113:104, :127:5]
  assign io_d_ebus_alu_op_1 = IsaR_sub;	// @[<stdin>:40:3, playground/src/ID_stage.scala:20:32]
  assign io_d_ebus_alu_op_2 = IsaI_andi | IsaR_and;	// @[<stdin>:40:3, playground/src/ID_stage.scala:20:32, :21:32, :131:36]
  assign io_d_ebus_alu_op_3 = IsaI_ori | IsaR_or;	// @[<stdin>:40:3, playground/src/ID_stage.scala:20:32, :21:32, :133:35]
  assign io_d_ebus_alu_op_4 = IsaI_xori | IsaR_xor;	// @[<stdin>:40:3, playground/src/ID_stage.scala:20:32, :21:32, :135:36]
  assign io_d_ebus_alu_op_5 = IsaR_slt | IsaR_sltu | IsaI_slti | IsaI_sltiu;	// @[<stdin>:40:3, playground/src/ID_stage.scala:20:32, :21:32, :137:60]
  assign io_d_ebus_alu_op_6 = IsaI_slli | IsaR_sll;	// @[<stdin>:40:3, playground/src/ID_stage.scala:20:32, :21:32, :139:36]
  assign io_d_ebus_alu_op_7 = _io_d_ebus_alu_op_7_T;	// @[<stdin>:40:3, playground/src/ID_stage.scala:106:41]
  assign io_d_ebus_alu_op_8 = IsaI_srli | IsaR_srl;	// @[<stdin>:40:3, playground/src/ID_stage.scala:20:32, :21:32, :143:36]
endmodule

