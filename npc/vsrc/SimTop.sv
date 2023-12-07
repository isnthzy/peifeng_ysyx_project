// Generated by CIRCT firtool-1.56.0
module SimTop(	// @[<stdin>:130:3]
  input         clock,	// @[<stdin>:131:11]
                reset,	// @[<stdin>:132:11]
  input  [31:0] io_inst,	// @[playground/src/SimTop.scala:6:14]
  output [31:0] io_pc,	// @[playground/src/SimTop.scala:6:14]
                io_nextpc,	// @[playground/src/SimTop.scala:6:14]
                io_result,	// @[playground/src/SimTop.scala:6:14]
  output        io_wen,	// @[playground/src/SimTop.scala:6:14]
  output [31:0] io_imm	// @[playground/src/SimTop.scala:6:14]
);

  wire [31:0] Imm;	// @[playground/src/SimTop.scala:102:39]
  wire [31:0] _alu_io_result;	// @[playground/src/SimTop.scala:160:18]
  wire [31:0] _RegFile_io_rdata1;	// @[playground/src/SimTop.scala:152:21]
  wire [31:0] _RegFile_io_rdata2;	// @[playground/src/SimTop.scala:152:21]
  reg  [31:0] REGpc;	// @[playground/src/SimTop.scala:31:20]
  wire [31:0] snpc = REGpc + 32'h4;	// @[playground/src/SimTop.scala:18:26, :31:20, :32:14]
  wire [31:0] jalr_taget;	// @[playground/src/SimTop.scala:16:32]
  wire        IsaI_jalr;	// @[playground/src/SimTop.scala:25:26]
  wire        IsaU_jal;	// @[playground/src/SimTop.scala:28:26]
  wire [31:0] dnpc = IsaU_jal ? REGpc + Imm : IsaI_jalr ? jalr_taget : snpc;	// @[playground/src/SimTop.scala:16:32, :18:26, :19:26, :25:26, :28:26, :31:20, :33:{12,27}, :34:14, :102:39]
  wire        is_jump;	// @[playground/src/SimTop.scala:22:29]
  wire [31:0] nextpc = is_jump ? dnpc : snpc;	// @[playground/src/SimTop.scala:17:28, :18:26, :19:26, :22:29, :35:14]
  wire        IsaU_lui = io_inst[6:0] == 7'h37;	// @[playground/src/SimTop.scala:28:26, :54:24]
  wire        IsaU_auipc = io_inst[6:0] == 7'h17;	// @[playground/src/SimTop.scala:28:26, :54:24, :55:24]
  assign IsaU_jal = io_inst[6:0] == 7'h6F;	// @[playground/src/SimTop.scala:28:26, :54:24, :56:24]
  wire [9:0]  _GEN = {io_inst[14:12], io_inst[6:0]};	// @[playground/src/SimTop.scala:54:24, :57:24]
  assign IsaI_jalr = _GEN == 10'h67;	// @[playground/src/SimTop.scala:25:26, :57:24]
  wire        IsaB_beq = _GEN == 10'h63;	// @[playground/src/SimTop.scala:27:26, :57:24, :58:24]
  wire        IsaB_bne = _GEN == 10'hE3;	// @[playground/src/SimTop.scala:27:26, :57:24, :59:24]
  wire        IsaB_blt = _GEN == 10'h263;	// @[playground/src/SimTop.scala:27:26, :57:24, :60:24]
  wire        IsaB_bge = _GEN == 10'h2E3;	// @[playground/src/SimTop.scala:27:26, :57:24, :61:24]
  wire        IsaB_bltu = _GEN == 10'h363;	// @[playground/src/SimTop.scala:27:26, :57:24, :62:24]
  wire        IsaB_bgeu = _GEN == 10'h3E3;	// @[playground/src/SimTop.scala:27:26, :57:24, :63:24]
  wire        IsaI_lb = _GEN == 10'h3;	// @[playground/src/SimTop.scala:25:26, :57:24, :64:24]
  wire        IsaI_lh = _GEN == 10'h83;	// @[playground/src/SimTop.scala:25:26, :57:24, :65:24]
  wire        IsaI_lw = _GEN == 10'h103;	// @[playground/src/SimTop.scala:25:26, :57:24, :66:24]
  wire        IsaI_lbu = _GEN == 10'h203;	// @[playground/src/SimTop.scala:25:26, :57:24, :67:24]
  wire        IsaI_lhu = _GEN == 10'h283;	// @[playground/src/SimTop.scala:25:26, :57:24, :68:24]
  wire        IsaS_sb = _GEN == 10'h23;	// @[playground/src/SimTop.scala:26:26, :57:24, :69:24]
  wire        IsaS_sh = _GEN == 10'hA3;	// @[playground/src/SimTop.scala:26:26, :57:24, :70:24]
  wire        IsaS_sw = _GEN == 10'h123;	// @[playground/src/SimTop.scala:26:26, :57:24, :71:24]
  wire        IsaI_addi = _GEN == 10'h13;	// @[playground/src/SimTop.scala:25:26, :57:24, :72:24]
  wire        IsaI_slti = _GEN == 10'h113;	// @[playground/src/SimTop.scala:25:26, :57:24, :73:24]
  wire        IsaI_sltiu = _GEN == 10'h193;	// @[playground/src/SimTop.scala:25:26, :57:24, :74:24]
  wire        IsaI_xori = _GEN == 10'h213;	// @[playground/src/SimTop.scala:25:26, :57:24, :75:24]
  wire        IsaI_ori = _GEN == 10'h313;	// @[playground/src/SimTop.scala:25:26, :57:24, :76:24]
  wire        IsaI_andi = _GEN == 10'h393;	// @[playground/src/SimTop.scala:25:26, :57:24, :77:24]
  wire [16:0] _GEN_0 = {io_inst[31:25], io_inst[14:12], io_inst[6:0]};	// @[playground/src/SimTop.scala:54:24, :57:24, :78:24]
  wire        IsaI_slli = _GEN_0 == 17'h93;	// @[playground/src/SimTop.scala:25:26, :78:24]
  wire        IsaI_srli = _GEN_0 == 17'h293;	// @[playground/src/SimTop.scala:25:26, :78:24, :79:24]
  wire        IsaI_srai = _GEN_0 == 17'h8293;	// @[playground/src/SimTop.scala:25:26, :78:24, :80:24]
  wire        IsaR_add = _GEN_0 == 17'h33;	// @[playground/src/SimTop.scala:24:26, :78:24, :81:24]
  wire        IsaR_sub = _GEN_0 == 17'h8033;	// @[playground/src/SimTop.scala:24:26, :78:24, :82:24]
  wire        IsaR_sll = _GEN_0 == 17'hB3;	// @[playground/src/SimTop.scala:24:26, :78:24, :83:24]
  wire        IsaR_slt = _GEN_0 == 17'h133;	// @[playground/src/SimTop.scala:24:26, :78:24, :84:24]
  wire        IsaR_sltu = _GEN_0 == 17'h1B3;	// @[playground/src/SimTop.scala:24:26, :78:24, :85:24]
  wire        IsaR_xor = _GEN_0 == 17'h233;	// @[playground/src/SimTop.scala:24:26, :78:24, :86:24]
  wire        IsaR_srl = _GEN_0 == 17'h2B3;	// @[playground/src/SimTop.scala:24:26, :78:24, :87:24]
  wire        IsaR_sra = _GEN_0 == 17'h82B3;	// @[playground/src/SimTop.scala:24:26, :78:24, :88:24]
  wire        IsaR_or = _GEN_0 == 17'h333;	// @[playground/src/SimTop.scala:24:26, :78:24, :89:24]
  wire        IsaR_and = _GEN_0 == 17'h3B3;	// @[playground/src/SimTop.scala:24:26, :78:24, :90:24]
  wire        IsaI_ebreak;	// @[playground/src/SimTop.scala:25:26]
  assign IsaI_ebreak = io_inst == 32'h100073;	// @[playground/src/SimTop.scala:25:26, :93:24]
  wire [4:0]  _Imm_T =
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
     IsaU_jal};	// @[playground/src/SimTop.scala:25:26, :26:26, :27:26, :28:26, :97:{30,36}, :98:{30,36}, :99:{30,36}, :100:{30,36}, :102:28]
  assign Imm =
    _Imm_T == 5'h3
      ? {{12{io_inst[19]}}, io_inst[19:12], io_inst[20], io_inst[30:21], 1'h0}
      : _Imm_T == 5'h2
          ? {io_inst[31:12], 12'h0}
          : _Imm_T == 5'h4
              ? {{21{io_inst[7]}}, io_inst[30:25], io_inst[11:8], 1'h0}
              : _Imm_T == 5'h8
                  ? {{20{io_inst[31]}}, io_inst[31:25], io_inst[11:7]}
                  : _Imm_T == 5'h10 ? {{20{io_inst[31]}}, io_inst[31:20]} : 32'h0;	// @[playground/src/Bundle.scala:76:{10,15,37}, playground/src/SimTop.scala:35:15, :43:23, :44:{27,42}, :45:{39,50,65}, :46:{19,27}, :47:{39,54,66}, :102:{28,39}]
  wire        wen =
    IsaI_addi | IsaR_add | IsaI_andi | IsaR_and | IsaU_lui | IsaR_slt | IsaR_sltu
    | IsaR_sub | IsaI_ori | IsaR_or | IsaI_xori | IsaR_xor | IsaI_jalr | IsaU_jal
    | IsaU_auipc | IsaI_slti | IsaI_sltiu | IsaI_slli | IsaI_srai | IsaI_srli | IsaR_sll
    | IsaR_sra | IsaR_srl;	// @[playground/src/SimTop.scala:24:26, :25:26, :28:26, :114:57]
  wire        result_is_snpc = IsaU_jal | IsaI_jalr;	// @[playground/src/SimTop.scala:25:26, :28:26, :117:31]
  wire        alu_op_9 = IsaI_srai | IsaR_sra;	// @[playground/src/SimTop.scala:24:26, :25:26, :118:29]
  assign is_jump =
    result_is_snpc | IsaB_beq & _alu_io_result[0] | IsaB_bne & ~(_alu_io_result[0])
    | IsaB_blt & _alu_io_result[0] | IsaB_bltu & _alu_io_result[0] | IsaB_bge
    & ~(_alu_io_result[0]) | IsaB_bgeu & ~(_alu_io_result[0]);	// @[playground/src/SimTop.scala:22:29, :27:26, :117:31, :160:18, :173:{24,41}, :174:{24,27}, :175:24, :176:24, :177:{24,27}, :178:{13,24,27}]
  wire [31:0] _io_result_output = IsaU_lui ? Imm : result_is_snpc ? snpc : _alu_io_result;	// @[playground/src/SimTop.scala:18:26, :28:26, :102:39, :117:31, :160:18, :183:17, :184:18]
  wire [31:0] _jalr_tmp_T = _alu_io_result + Imm;	// @[playground/src/SimTop.scala:102:39, :160:18, :185:29]
  assign jalr_taget = {_jalr_tmp_T[31:1], 1'h0};	// @[playground/src/SimTop.scala:16:32, :35:15, :185:29, :186:{18,27}]
  always @(posedge clock) begin	// @[<stdin>:131:11]
    if (reset)	// @[<stdin>:131:11]
      REGpc <= 32'h80000000;	// @[playground/src/SimTop.scala:31:20]
    else	// @[<stdin>:131:11]
      REGpc <= nextpc;	// @[playground/src/SimTop.scala:17:28, :31:20]
  end // always @(posedge)
  RegFile RegFile (	// @[playground/src/SimTop.scala:152:21]
    .clock     (clock),
    .reset     (reset),
    .io_waddr  (io_inst[11:7]),	// @[playground/src/SimTop.scala:44:42]
    .io_wdata  (_io_result_output),	// @[playground/src/SimTop.scala:183:17]
    .io_raddr1 (IsaI_ebreak ? 5'hA : io_inst[19:15]),	// @[playground/src/SimTop.scala:25:26, :49:23, :153:25]
    .io_raddr2 (IsaI_ebreak ? 5'h0 : io_inst[24:20]),	// @[playground/src/SimTop.scala:25:26, :48:23, :154:25]
    .io_wen    (wen),	// @[playground/src/SimTop.scala:114:57]
    .io_rdata1 (_RegFile_io_rdata1),
    .io_rdata2 (_RegFile_io_rdata2)
  );
  Alu alu (	// @[playground/src/SimTop.scala:160:18]
    .io_op
      ({1'h0,
        IsaI_srli | IsaR_srl,
        alu_op_9,
        IsaI_slli | IsaR_sll,
        IsaB_blt | IsaB_bltu | IsaB_bge | IsaB_bgeu | IsaR_slt | IsaR_sltu | IsaI_slti
          | IsaI_sltiu,
        IsaB_beq | IsaB_bne,
        IsaI_xori | IsaR_xor,
        IsaI_ori | IsaR_or,
        IsaI_andi | IsaR_and,
        1'h0,
        IsaI_addi | IsaR_sub,
        IsaR_add | IsaI_ebreak | IsaI_jalr}),	// @[playground/src/SimTop.scala:24:26, :25:26, :27:26, :35:15, :118:29, :127:39, :129:26, :133:25, :135:25, :137:25, :139:25, :142:25, :144:25, :148:25, :166:23]
    .io_src1   (IsaU_auipc ? REGpc : _RegFile_io_rdata1),	// @[playground/src/SimTop.scala:28:26, :31:20, :152:21, :161:15]
    .io_src2
      (IsaI_addi | IsaI_slti | IsaI_sltiu | IsaI_xori | IsaI_ori | IsaI_andi | IsaI_jalr
         ? Imm
         : IsaI_slli | IsaI_srai | IsaI_srli
             ? {26'h0, io_inst[25:20]}
             : IsaR_sll | IsaR_sra | IsaR_srl
                 ? {26'h0, _RegFile_io_rdata2[5:0]}
                 : _RegFile_io_rdata2),	// @[playground/src/SimTop.scala:24:26, :25:26, :54:24, :102:39, :120:86, :121:47, :122:47, :152:21, :162:15, :163:{16,44}, :164:{18,46}]
    .io_sign   (alu_op_9 | IsaR_slt | IsaB_blt | IsaB_bltu),	// @[playground/src/SimTop.scala:24:26, :27:26, :118:{29,63}]
    .io_result (_alu_io_result)
  );
  singal_dpi singal_dpi (	// @[playground/src/SimTop.scala:189:24]
    .clock       (clock),
    .reset       (reset),
    .pc          (REGpc),	// @[playground/src/SimTop.scala:31:20]
    .nextpc      (nextpc),	// @[playground/src/SimTop.scala:17:28]
    .inst        (io_inst),
    .rd          ({27'h0, io_inst[11:7]}),	// @[playground/src/SimTop.scala:44:42, :55:24, :195:19]
    .is_jal      (IsaU_jal),	// @[playground/src/SimTop.scala:28:26]
    .func_flag   (result_is_snpc),	// @[playground/src/SimTop.scala:117:31]
    .ebreak_flag (IsaI_ebreak),	// @[playground/src/SimTop.scala:25:26]
    .inv_flag
      ({IsaB_beq, IsaB_bne, IsaB_blt, IsaB_bge, IsaB_bltu, IsaB_bgeu} == 6'h0
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
       & {IsaU_lui, IsaU_auipc, IsaU_jal} == 3'h0),	// @[playground/src/SimTop.scala:24:26, :25:26, :26:26, :27:26, :28:26, :92:{22,28,42,48,62,68,82,88,95,102,108}]
    .ret_reg     (_alu_io_result)	// @[playground/src/SimTop.scala:160:18]
  );
  assign io_pc = REGpc;	// @[<stdin>:130:3, playground/src/SimTop.scala:31:20]
  assign io_nextpc = nextpc;	// @[<stdin>:130:3, playground/src/SimTop.scala:17:28]
  assign io_result = _io_result_output;	// @[<stdin>:130:3, playground/src/SimTop.scala:183:17]
  assign io_wen = wen;	// @[<stdin>:130:3, playground/src/SimTop.scala:114:57]
  assign io_imm = Imm;	// @[<stdin>:130:3, playground/src/SimTop.scala:102:39]
endmodule

