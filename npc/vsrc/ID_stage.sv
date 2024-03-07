// Generated by CIRCT firtool-1.56.0
module ID_stage(	// @[<stdin>:1008:3]
  input         clock,	// @[<stdin>:1009:11]
                reset,	// @[<stdin>:1010:11]
                ID_IO_valid,	// @[playground/src/ID_stage.scala:7:12]
  input  [31:0] ID_IO_bits_nextpc,	// @[playground/src/ID_stage.scala:7:12]
                ID_IO_bits_pc,	// @[playground/src/ID_stage.scala:7:12]
                ID_IO_bits_inst,	// @[playground/src/ID_stage.scala:7:12]
  input         ID_to_ex_ready,	// @[playground/src/ID_stage.scala:7:12]
  input  [4:0]  ID_for_ex_fw_addr,	// @[playground/src/ID_stage.scala:7:12]
  input  [31:0] ID_for_ex_fw_data,	// @[playground/src/ID_stage.scala:7:12]
  input         ID_for_ex_csr_ecpt_wen,	// @[playground/src/ID_stage.scala:7:12]
  input  [31:0] ID_for_ex_csr_ecpt_pc_wb,	// @[playground/src/ID_stage.scala:7:12]
  input  [11:0] ID_for_ex_csr_waddr,	// @[playground/src/ID_stage.scala:7:12]
  input         ID_for_ex_csr_wen,	// @[playground/src/ID_stage.scala:7:12]
  input  [31:0] ID_for_ex_csr_wdata,	// @[playground/src/ID_stage.scala:7:12]
  input         ID_for_ex_clog,	// @[playground/src/ID_stage.scala:7:12]
                ID_for_ex_flush,	// @[playground/src/ID_stage.scala:7:12]
  input  [4:0]  ID_for_ls_fw_addr,	// @[playground/src/ID_stage.scala:7:12]
  input  [31:0] ID_for_ls_fw_data,	// @[playground/src/ID_stage.scala:7:12]
  input         ID_for_ls_clog,	// @[playground/src/ID_stage.scala:7:12]
  input  [4:0]  ID_for_wb_rf_waddr,	// @[playground/src/ID_stage.scala:7:12]
  input  [31:0] ID_for_wb_rf_wdata,	// @[playground/src/ID_stage.scala:7:12]
  input         ID_for_wb_rf_wen,	// @[playground/src/ID_stage.scala:7:12]
  output        ID_IO_ready,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_valid,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_bits_dpic_bundle_id_inv_flag,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_bits_pc_sel,	// @[playground/src/ID_stage.scala:7:12]
  output [11:0] ID_to_ex_bits_csr_addr,	// @[playground/src/ID_stage.scala:7:12]
  output [31:0] ID_to_ex_bits_csr_global_mtvec,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_bits_csr_global_mepc,	// @[playground/src/ID_stage.scala:7:12]
  output        ID_to_ex_bits_b_taken,	// @[playground/src/ID_stage.scala:7:12]
  output [3:0]  ID_to_ex_bits_st_type,	// @[playground/src/ID_stage.scala:7:12]
  output [2:0]  ID_to_ex_bits_ld_type,	// @[playground/src/ID_stage.scala:7:12]
  output [4:0]  ID_to_ex_bits_csr_cmd,	// @[playground/src/ID_stage.scala:7:12]
  output [1:0]  ID_to_ex_bits_wb_sel,	// @[playground/src/ID_stage.scala:7:12]
  output [3:0]  ID_to_ex_bits_br_type,	// @[playground/src/ID_stage.scala:7:12]
  output        ID_to_ex_bits_rf_wen,	// @[playground/src/ID_stage.scala:7:12]
  output [4:0]  ID_to_ex_bits_rd,	// @[playground/src/ID_stage.scala:7:12]
  output [3:0]  ID_to_ex_bits_alu_op,	// @[playground/src/ID_stage.scala:7:12]
  output [31:0] ID_to_ex_bits_src1,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_bits_src2,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_bits_rdata1,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_bits_rdata2,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_bits_nextpc,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_bits_pc,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_bits_inst,	// @[playground/src/ID_stage.scala:7:12]
  output        ID_to_if_flush,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_preif_Br_J_taken,	// @[playground/src/ID_stage.scala:7:12]
  output [31:0] ID_to_preif_Br_J_target,	// @[playground/src/ID_stage.scala:7:12]
  output        ID_to_preif_flush	// @[playground/src/ID_stage.scala:7:12]
);

  wire        _B_cond_io_taken;	// @[playground/src/ID_stage.scala:127:20]
  wire        _J_cond_io_taken;	// @[playground/src/ID_stage.scala:126:20]
  wire [31:0] _Csrfile_io_out;	// @[playground/src/ID_stage.scala:94:21]
  wire [31:0] _Regfile_io_rdata1;	// @[playground/src/ID_stage.scala:56:21]
  wire [31:0] _Regfile_io_rdata2;	// @[playground/src/ID_stage.scala:56:21]
  wire        _dc_io_A_sel;	// @[playground/src/ID_stage.scala:33:16]
  wire [1:0]  _dc_io_B_sel;	// @[playground/src/ID_stage.scala:33:16]
  wire [2:0]  _dc_io_imm_sel;	// @[playground/src/ID_stage.scala:33:16]
  wire [3:0]  _dc_io_br_type;	// @[playground/src/ID_stage.scala:33:16]
  wire        _dc_io_wb_en;	// @[playground/src/ID_stage.scala:33:16]
  wire [4:0]  _dc_io_csr_cmd;	// @[playground/src/ID_stage.scala:33:16]
  wire        _dc_io_illegal;	// @[playground/src/ID_stage.scala:33:16]
  wire        id_flush = ID_for_ex_flush;	// @[playground/src/ID_stage.scala:18:30]
  reg  [31:0] casez_tmp;	// @[playground/src/ID_stage.scala:116:38]
  reg         id_valid;	// @[playground/src/ID_stage.scala:23:33]
  wire        id_clog;	// @[playground/src/ID_stage.scala:17:29]
  wire        id_ready_go = ~id_clog;	// @[playground/src/ID_stage.scala:17:29, :23:33, :24:33, :25:19]
  wire        _ID_IO_ready_output = ~id_valid | id_ready_go & ID_to_ex_ready;	// @[playground/src/ID_stage.scala:23:33, :24:33, :26:{18,28,43}]
  wire [4:0]  rs2 = ID_IO_bits_inst[24:20];	// @[playground/src/ID_stage.scala:37:25, :49:25]
  wire [4:0]  rs1 = ID_IO_bits_inst[19:15];	// @[playground/src/ID_stage.scala:36:25, :50:25]
  wire [2:0]  funct3 = ID_IO_bits_inst[14:12];	// @[playground/src/ID_stage.scala:38:28, :51:28]
  wire [4:0]  rd = ID_IO_bits_inst[11:7];	// @[playground/src/ID_stage.scala:39:24, :52:24]
  wire [6:0]  opcode = ID_IO_bits_inst[6:0];	// @[playground/src/ID_stage.scala:40:28, :53:28]
  wire [11:0] csr_addr = ID_IO_bits_inst[31:20];	// @[playground/src/ID_stage.scala:41:30, :54:30]
  wire [4:0]  _Regfile_io_raddr2_T_3 =
    _dc_io_csr_cmd == 5'h4 ? 5'hF : _dc_io_csr_cmd == 5'h5 ? 5'hA : rs2;	// @[playground/src/ID_stage.scala:33:16, :37:25, :58:50]
  wire        rs1_is_forward;	// @[playground/src/ID_stage.scala:68:36]
  wire        rs2_is_forward;	// @[playground/src/ID_stage.scala:69:36]
  assign id_clog =
    (ID_for_ex_clog | ID_for_ls_clog) & (rs1_is_forward | rs2_is_forward) & id_valid;	// @[playground/src/ID_stage.scala:17:29, :23:33, :68:36, :69:36, :70:{27,61,78}]
  wire        _rdata1_T = rs1 == ID_for_ex_fw_addr;	// @[playground/src/ID_stage.scala:36:25, :73:38]
  wire        _rdata1_T_1 = rs1 == ID_for_ls_fw_addr;	// @[playground/src/ID_stage.scala:36:25, :74:37]
  assign rs1_is_forward =
    (|rs1) & (_rdata1_T | _rdata1_T_1 | ID_for_wb_rf_wen & rs1 == ID_for_wb_rf_waddr);	// @[playground/src/ID_stage.scala:36:25, :68:36, :72:{38,46}, :73:38, :74:{37,59}, :75:{37,58}]
  wire        _rdata2_T = _Regfile_io_raddr2_T_3 == ID_for_ex_fw_addr;	// @[playground/src/ID_stage.scala:58:50, :77:38]
  wire        _rdata2_T_1 = _Regfile_io_raddr2_T_3 == ID_for_ls_fw_addr;	// @[playground/src/ID_stage.scala:58:50, :78:37]
  assign rs2_is_forward =
    (|_Regfile_io_raddr2_T_3)
    & (_rdata2_T | _rdata2_T_1 | ID_for_wb_rf_wen
       & _Regfile_io_raddr2_T_3 == ID_for_wb_rf_waddr);	// @[playground/src/ID_stage.scala:58:50, :69:36, :76:{38,46}, :77:38, :78:{37,59}, :79:{37,58}]
  wire [31:0] rdata1 =
    rs1_is_forward
      ? (_rdata1_T
           ? ID_for_ex_fw_data
           : _rdata1_T_1 ? ID_for_ls_fw_data : ID_for_wb_rf_wdata)
      : _Regfile_io_rdata1;	// @[playground/src/ID_stage.scala:56:21, :66:28, :68:36, :73:38, :74:37, :80:14, :81:16, :82:16]
  wire [31:0] rdata2 =
    rs2_is_forward
      ? (_rdata2_T
           ? ID_for_ex_fw_data
           : _rdata2_T_1 ? ID_for_ls_fw_data : ID_for_wb_rf_wdata)
      : _Regfile_io_rdata2;	// @[playground/src/ID_stage.scala:56:21, :67:28, :69:36, :77:38, :78:37, :85:14, :86:16, :87:16]
  wire [31:0] csr_out_data =
    csr_addr == ID_for_ex_csr_waddr ? ID_for_ex_csr_wdata : _Csrfile_io_out;	// @[playground/src/ID_stage.scala:41:30, :93:34, :94:21, :96:{20,29}]
  wire [31:0] src1 = _dc_io_A_sel ? rdata1 : ID_IO_bits_pc;	// @[playground/src/ID_stage.scala:33:16, :66:28, :112:38]
  wire [31:0] imm;	// @[playground/src/ID_stage.scala:35:25]
  always_comb begin	// @[playground/src/ID_stage.scala:116:38]
    casez (_dc_io_B_sel)	// @[playground/src/ID_stage.scala:33:16, :116:38]
      2'b00:
        casez_tmp = imm;	// @[playground/src/ID_stage.scala:35:25, :116:38]
      2'b01:
        casez_tmp = rdata2;	// @[playground/src/ID_stage.scala:67:28, :116:38]
      2'b10:
        casez_tmp = csr_out_data;	// @[playground/src/ID_stage.scala:93:34, :116:38]
      default:
        casez_tmp = 32'h0;	// @[playground/src/ID_stage.scala:116:38, :166:45]
    endcase	// @[playground/src/ID_stage.scala:33:16, :116:38]
  end // always_comb
  wire        _ID_to_preif_flush_T = _J_cond_io_taken & id_valid;	// @[playground/src/ID_stage.scala:23:33, :126:20, :131:42]
  always @(posedge clock) begin	// @[<stdin>:1009:11]
    if (reset)	// @[<stdin>:1009:11]
      id_valid <= 1'h0;	// @[playground/src/ID_stage.scala:23:33]
    else if (_ID_IO_ready_output)	// @[playground/src/ID_stage.scala:26:28]
      id_valid <= ID_IO_valid;	// @[playground/src/ID_stage.scala:23:33]
  end // always @(posedge)
  Decode dc (	// @[playground/src/ID_stage.scala:33:16]
    .io_inst    (ID_IO_bits_inst),
    .io_pc_sel  (ID_to_ex_bits_pc_sel),
    .io_A_sel   (_dc_io_A_sel),
    .io_B_sel   (_dc_io_B_sel),
    .io_imm_sel (_dc_io_imm_sel),
    .io_alu_op  (ID_to_ex_bits_alu_op),
    .io_br_type (_dc_io_br_type),
    .io_st_type (ID_to_ex_bits_st_type),
    .io_ld_type (ID_to_ex_bits_ld_type),
    .io_wb_sel  (ID_to_ex_bits_wb_sel),
    .io_wb_en   (_dc_io_wb_en),
    .io_csr_cmd (_dc_io_csr_cmd),
    .io_illegal (_dc_io_illegal)
  );
  ImmGen ImmGen (	// @[playground/src/ID_stage.scala:34:20]
    .io_inst (ID_IO_bits_inst),
    .io_sel  (_dc_io_imm_sel),	// @[playground/src/ID_stage.scala:33:16]
    .io_out  (imm)
  );
  RegFile Regfile (	// @[playground/src/ID_stage.scala:56:21]
    .clock     (clock),
    .reset     (reset),
    .io_waddr  (ID_for_wb_rf_waddr),
    .io_wdata  (ID_for_wb_rf_wdata),
    .io_raddr1 (rs1),	// @[playground/src/ID_stage.scala:36:25]
    .io_raddr2 (_Regfile_io_raddr2_T_3),	// @[playground/src/ID_stage.scala:58:50]
    .io_wen    (ID_for_wb_rf_wen),
    .io_rdata1 (_Regfile_io_rdata1),
    .io_rdata2 (_Regfile_io_rdata2)
  );
  CsrFile Csrfile (	// @[playground/src/ID_stage.scala:94:21]
    .clock           (clock),
    .reset           (reset),
    .io_csr_cmd      (_dc_io_csr_cmd),	// @[playground/src/ID_stage.scala:33:16]
    .io_csr_raddr    (csr_addr),	// @[playground/src/ID_stage.scala:41:30]
    .io_csr_waddr    (ID_for_ex_csr_waddr),
    .io_csr_wen      (ID_for_ex_csr_wen),
    .io_csr_wdata    (ID_for_ex_csr_wdata),
    .io_mepc_in      (ID_for_ex_csr_ecpt_pc_wb),
    .io_ecpt_wen     (ID_for_ex_csr_ecpt_wen),
    .io_out          (_Csrfile_io_out),
    .io_global_mtvec (ID_to_ex_bits_csr_global_mtvec),
    .io_global_mepc  (ID_to_ex_bits_csr_global_mepc)
  );
  Br_j J_cond (	// @[playground/src/ID_stage.scala:126:20]
    .io_br_type (_dc_io_br_type),	// @[playground/src/ID_stage.scala:33:16]
    .io_src1    (src1),	// @[playground/src/ID_stage.scala:112:38]
    .io_src2    (casez_tmp),	// @[playground/src/ID_stage.scala:116:38]
    .io_taken   (_J_cond_io_taken),
    .io_target  (ID_to_preif_Br_J_target)
  );
  Br_b B_cond (	// @[playground/src/ID_stage.scala:127:20]
    .io_br_type (_dc_io_br_type),	// @[playground/src/ID_stage.scala:33:16]
    .io_rdata1  (rdata1),	// @[playground/src/ID_stage.scala:66:28]
    .io_rdata2  (rdata2),	// @[playground/src/ID_stage.scala:67:28]
    .io_taken   (_B_cond_io_taken)
  );
  assign ID_IO_ready = _ID_IO_ready_output;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:26:28]
  assign ID_to_ex_valid = ~id_flush & id_valid & id_ready_go;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:18:30, :23:33, :24:33, :30:22]
  assign ID_to_ex_bits_dpic_bundle_id_inv_flag =
    _dc_io_illegal & ID_IO_bits_nextpc != 32'h80000000;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:33:16, :165:{56,75}]
  assign ID_to_ex_bits_csr_addr = csr_addr;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:41:30]
  assign ID_to_ex_bits_b_taken = _B_cond_io_taken & id_valid;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:23:33, :127:20, :144:41]
  assign ID_to_ex_bits_csr_cmd = _dc_io_csr_cmd;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:33:16]
  assign ID_to_ex_bits_br_type = _dc_io_br_type;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:33:16]
  assign ID_to_ex_bits_rf_wen = _dc_io_wb_en & id_valid;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:23:33, :33:16, :153:37]
  assign ID_to_ex_bits_rd = rd;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:39:24]
  assign ID_to_ex_bits_src1 = src1;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:112:38]
  assign ID_to_ex_bits_src2 = casez_tmp;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:116:38]
  assign ID_to_ex_bits_rdata1 = rdata1;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:66:28]
  assign ID_to_ex_bits_rdata2 = rdata2;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:67:28]
  assign ID_to_ex_bits_nextpc = ID_IO_bits_nextpc;	// @[<stdin>:1008:3]
  assign ID_to_ex_bits_pc = ID_IO_bits_pc;	// @[<stdin>:1008:3]
  assign ID_to_ex_bits_inst = ID_IO_bits_inst;	// @[<stdin>:1008:3]
  assign ID_to_if_flush = _ID_to_preif_flush_T;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:131:42]
  assign ID_to_preif_Br_J_taken = _ID_to_preif_flush_T;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:131:42]
  assign ID_to_preif_flush = _ID_to_preif_flush_T;	// @[<stdin>:1008:3, playground/src/ID_stage.scala:131:42]
endmodule

