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

module SimTop(	// @[<stdin>:655:3]
  input         clock,	// @[<stdin>:656:11]
                reset,	// @[<stdin>:657:11]
  output [31:0] io_result	// @[playground/src/SimTop.scala:6:14]
);

  wire [31:0] sram_rdata;	// @[playground/src/SimTop.scala:18:35]
  wire [31:0] nextpc;	// @[playground/src/SimTop.scala:13:35]
  wire [31:0] pc;	// @[playground/src/SimTop.scala:12:35]
  wire [31:0] _EXE_stage_io_result;	// @[playground/src/SimTop.scala:41:25]
  wire        _EXE_stage_io_sram_valid;	// @[playground/src/SimTop.scala:41:25]
  wire        _EXE_stage_io_sram_wen;	// @[playground/src/SimTop.scala:41:25]
  wire [31:0] _EXE_stage_io_sram_wdata;	// @[playground/src/SimTop.scala:41:25]
  wire [3:0]  _EXE_stage_io_sram_wmask;	// @[playground/src/SimTop.scala:41:25]
  wire        _ID_stage_io_d_ebus_is_ebreak;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_data_wen;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_result_is_imm;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_result_is_snpc;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_src_is_sign;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_src1_is_pc;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_src2_is_imm;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_src2_is_shamt_imm;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_src2_is_shamt_src;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_sram_valid;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_sram_wen;	// @[playground/src/SimTop.scala:29:24]
  wire [3:0]  _ID_stage_io_d_ebus_wmask;	// @[playground/src/SimTop.scala:29:24]
  wire [31:0] _ID_stage_io_d_ebus_snpc;	// @[playground/src/SimTop.scala:29:24]
  wire [31:0] _ID_stage_io_d_ebus_imm;	// @[playground/src/SimTop.scala:29:24]
  wire [4:0]  _ID_stage_io_d_ebus_src1;	// @[playground/src/SimTop.scala:29:24]
  wire [4:0]  _ID_stage_io_d_ebus_src2;	// @[playground/src/SimTop.scala:29:24]
  wire [4:0]  _ID_stage_io_d_ebus_rd;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_alu_op_0;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_alu_op_1;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_alu_op_2;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_alu_op_3;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_alu_op_4;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_alu_op_5;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_alu_op_6;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_alu_op_7;	// @[playground/src/SimTop.scala:29:24]
  wire        _ID_stage_io_d_ebus_alu_op_8;	// @[playground/src/SimTop.scala:29:24]
  wire [31:0] _IF_stage_io_f_dbus_snpc;	// @[playground/src/SimTop.scala:21:24]
  wire [31:0] Imm;	// @[playground/src/SimTop.scala:11:35]
  wire        is_not_jalr;	// @[playground/src/SimTop.scala:14:35]
  wire        is_jump;	// @[playground/src/SimTop.scala:15:35]
  wire [31:0] jalr_taget;	// @[playground/src/SimTop.scala:16:35]
  IF_stage IF_stage (	// @[playground/src/SimTop.scala:21:24]
    .clock          (clock),
    .reset          (reset),
    .io_jalr_taget  (jalr_taget),	// @[playground/src/SimTop.scala:16:35]
    .io_is_not_jalr (is_not_jalr),	// @[playground/src/SimTop.scala:14:35]
    .io_is_jump     (is_jump),	// @[playground/src/SimTop.scala:15:35]
    .io_Imm         (Imm),	// @[playground/src/SimTop.scala:11:35]
    .io_pc          (pc),
    .io_nextpc      (nextpc),
    .io_f_dbus_snpc (_IF_stage_io_f_dbus_snpc)
  );
  wire [31:0] inst;	// @[playground/src/SimTop.scala:19:35]
  ID_stage ID_stage (	// @[playground/src/SimTop.scala:29:24]
    .clock                       (clock),
    .reset                       (reset),
    .io_pc                       (pc),	// @[playground/src/SimTop.scala:12:35]
    .io_nextpc                   (nextpc),	// @[playground/src/SimTop.scala:13:35]
    .io_inst                     (inst),	// @[playground/src/SimTop.scala:19:35]
    .io_result                   (_EXE_stage_io_result),	// @[playground/src/SimTop.scala:41:25]
    .io_f_dbus_snpc              (_IF_stage_io_f_dbus_snpc),	// @[playground/src/SimTop.scala:21:24]
    .io_Imm                      (Imm),
    .io_is_not_jalr              (is_not_jalr),
    .io_is_jump                  (is_jump),
    .io_d_ebus_is_ebreak         (_ID_stage_io_d_ebus_is_ebreak),
    .io_d_ebus_data_wen          (_ID_stage_io_d_ebus_data_wen),
    .io_d_ebus_result_is_imm     (_ID_stage_io_d_ebus_result_is_imm),
    .io_d_ebus_result_is_snpc    (_ID_stage_io_d_ebus_result_is_snpc),
    .io_d_ebus_src_is_sign       (_ID_stage_io_d_ebus_src_is_sign),
    .io_d_ebus_src1_is_pc        (_ID_stage_io_d_ebus_src1_is_pc),
    .io_d_ebus_src2_is_imm       (_ID_stage_io_d_ebus_src2_is_imm),
    .io_d_ebus_src2_is_shamt_imm (_ID_stage_io_d_ebus_src2_is_shamt_imm),
    .io_d_ebus_src2_is_shamt_src (_ID_stage_io_d_ebus_src2_is_shamt_src),
    .io_d_ebus_sram_valid        (_ID_stage_io_d_ebus_sram_valid),
    .io_d_ebus_sram_wen          (_ID_stage_io_d_ebus_sram_wen),
    .io_d_ebus_wmask             (_ID_stage_io_d_ebus_wmask),
    .io_d_ebus_snpc              (_ID_stage_io_d_ebus_snpc),
    .io_d_ebus_imm               (_ID_stage_io_d_ebus_imm),
    .io_d_ebus_src1              (_ID_stage_io_d_ebus_src1),
    .io_d_ebus_src2              (_ID_stage_io_d_ebus_src2),
    .io_d_ebus_rd                (_ID_stage_io_d_ebus_rd),
    .io_d_ebus_alu_op_0          (_ID_stage_io_d_ebus_alu_op_0),
    .io_d_ebus_alu_op_1          (_ID_stage_io_d_ebus_alu_op_1),
    .io_d_ebus_alu_op_2          (_ID_stage_io_d_ebus_alu_op_2),
    .io_d_ebus_alu_op_3          (_ID_stage_io_d_ebus_alu_op_3),
    .io_d_ebus_alu_op_4          (_ID_stage_io_d_ebus_alu_op_4),
    .io_d_ebus_alu_op_5          (_ID_stage_io_d_ebus_alu_op_5),
    .io_d_ebus_alu_op_6          (_ID_stage_io_d_ebus_alu_op_6),
    .io_d_ebus_alu_op_7          (_ID_stage_io_d_ebus_alu_op_7),
    .io_d_ebus_alu_op_8          (_ID_stage_io_d_ebus_alu_op_8)
  );
  EXE_stage EXE_stage (	// @[playground/src/SimTop.scala:41:25]
    .clock                       (clock),
    .reset                       (reset),
    .io_d_ebus_is_ebreak         (_ID_stage_io_d_ebus_is_ebreak),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_data_wen          (_ID_stage_io_d_ebus_data_wen),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_result_is_imm     (_ID_stage_io_d_ebus_result_is_imm),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_result_is_snpc    (_ID_stage_io_d_ebus_result_is_snpc),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_src_is_sign       (_ID_stage_io_d_ebus_src_is_sign),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_src1_is_pc        (_ID_stage_io_d_ebus_src1_is_pc),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_src2_is_imm       (_ID_stage_io_d_ebus_src2_is_imm),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_src2_is_shamt_imm (_ID_stage_io_d_ebus_src2_is_shamt_imm),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_src2_is_shamt_src (_ID_stage_io_d_ebus_src2_is_shamt_src),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_sram_valid        (_ID_stage_io_d_ebus_sram_valid),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_sram_wen          (_ID_stage_io_d_ebus_sram_wen),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_wmask             (_ID_stage_io_d_ebus_wmask),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_snpc              (_ID_stage_io_d_ebus_snpc),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_imm               (_ID_stage_io_d_ebus_imm),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_src1              (_ID_stage_io_d_ebus_src1),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_src2              (_ID_stage_io_d_ebus_src2),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_rd                (_ID_stage_io_d_ebus_rd),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_alu_op_0          (_ID_stage_io_d_ebus_alu_op_0),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_alu_op_1          (_ID_stage_io_d_ebus_alu_op_1),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_alu_op_2          (_ID_stage_io_d_ebus_alu_op_2),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_alu_op_3          (_ID_stage_io_d_ebus_alu_op_3),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_alu_op_4          (_ID_stage_io_d_ebus_alu_op_4),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_alu_op_5          (_ID_stage_io_d_ebus_alu_op_5),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_alu_op_6          (_ID_stage_io_d_ebus_alu_op_6),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_alu_op_7          (_ID_stage_io_d_ebus_alu_op_7),	// @[playground/src/SimTop.scala:29:24]
    .io_d_ebus_alu_op_8          (_ID_stage_io_d_ebus_alu_op_8),	// @[playground/src/SimTop.scala:29:24]
    .io_pc                       (pc),	// @[playground/src/SimTop.scala:12:35]
    .io_result                   (_EXE_stage_io_result),
    .io_jalr_taget               (jalr_taget),
    .io_sram_valid               (_EXE_stage_io_sram_valid),
    .io_sram_wen                 (_EXE_stage_io_sram_wen),
    .io_sram_wdata               (_EXE_stage_io_sram_wdata),
    .io_sram_wmask               (_EXE_stage_io_sram_wmask)
  );
  pmem_dpi pmem_dpi (	// @[playground/src/SimTop.scala:50:22]
    .clock      (clock),
    .reset      (reset),
    .pc         (pc),	// @[playground/src/SimTop.scala:12:35]
    .nextpc     (nextpc),	// @[playground/src/SimTop.scala:13:35]
    .sram_valid (_EXE_stage_io_sram_valid),	// @[playground/src/SimTop.scala:41:25]
    .sram_wen   (_EXE_stage_io_sram_wen),	// @[playground/src/SimTop.scala:41:25]
    .raddr      (_EXE_stage_io_result),	// @[playground/src/SimTop.scala:41:25]
    .waddr      (_EXE_stage_io_result),	// @[playground/src/SimTop.scala:41:25]
    .wdata      (_EXE_stage_io_sram_wdata),	// @[playground/src/SimTop.scala:41:25]
    .wmask      (_EXE_stage_io_sram_wmask),	// @[playground/src/SimTop.scala:41:25]
    .inst       (inst),
    .rdata      (sram_rdata)
  );
  assign io_result = _EXE_stage_io_result;	// @[<stdin>:655:3, playground/src/SimTop.scala:41:25]
endmodule

