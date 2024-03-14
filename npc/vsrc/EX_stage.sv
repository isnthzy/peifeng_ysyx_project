// Generated by CIRCT firtool-1.56.0
module EX_stage(	// @[<stdin>:1268:3]
  input         clock,	// @[<stdin>:1269:11]
                reset,	// @[<stdin>:1270:11]
                EX_IO_valid,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_dpic_bundle_id_inv_flag,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_pc_sel,	// @[playground/src/EX_stage.scala:7:12]
  input  [11:0] EX_IO_bits_csr_addr,	// @[playground/src/EX_stage.scala:7:12]
  input  [31:0] EX_IO_bits_csr_global_mtvec,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_csr_global_mepc,	// @[playground/src/EX_stage.scala:7:12]
  input         EX_IO_bits_b_taken,	// @[playground/src/EX_stage.scala:7:12]
  input  [2:0]  EX_IO_bits_st_type,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_ld_type,	// @[playground/src/EX_stage.scala:7:12]
  input  [4:0]  EX_IO_bits_csr_cmd,	// @[playground/src/EX_stage.scala:7:12]
  input  [1:0]  EX_IO_bits_wb_sel,	// @[playground/src/EX_stage.scala:7:12]
  input  [3:0]  EX_IO_bits_br_type,	// @[playground/src/EX_stage.scala:7:12]
  input         EX_IO_bits_rf_wen,	// @[playground/src/EX_stage.scala:7:12]
  input  [4:0]  EX_IO_bits_rd,	// @[playground/src/EX_stage.scala:7:12]
  input  [3:0]  EX_IO_bits_alu_op,	// @[playground/src/EX_stage.scala:7:12]
  input  [31:0] EX_IO_bits_src1,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_src2,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_rdata1,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_rdata2,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_nextpc,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_pc,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_inst,	// @[playground/src/EX_stage.scala:7:12]
  input         EX_to_ls_ready,	// @[playground/src/EX_stage.scala:7:12]
                EX_wdata_ok,	// @[playground/src/EX_stage.scala:7:12]
                EX_raddr_ok,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_IO_ready,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_valid,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_csr_commit_wen,	// @[playground/src/EX_stage.scala:7:12]
  output [11:0] EX_to_ls_bits_csr_commit_waddr,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_ls_bits_csr_commit_wdata,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_ls_bits_csr_commit_exception_wen,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_ls_bits_csr_commit_exception_pc_wb,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_ls_bits_dpic_bundle_id_inv_flag,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_dpic_bundle_ex_func_flag,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_dpic_bundle_ex_is_jal,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_dpic_bundle_ex_is_ret,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_dpic_bundle_ex_is_rd0,	// @[playground/src/EX_stage.scala:7:12]
  output [2:0]  EX_to_ls_bits_dpic_bundle_ex_ld_type,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_dpic_bundle_ex_st_type,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_ls_bits_dpic_bundle_ex_mem_addr,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_dpic_bundle_ex_st_data,	// @[playground/src/EX_stage.scala:7:12]
  output [1:0]  EX_to_ls_bits_addr_low2bit,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_ls_bits_ld_wen,	// @[playground/src/EX_stage.scala:7:12]
  output [2:0]  EX_to_ls_bits_ld_type,	// @[playground/src/EX_stage.scala:7:12]
  output [4:0]  EX_to_ls_bits_csr_cmd,	// @[playground/src/EX_stage.scala:7:12]
  output [1:0]  EX_to_ls_bits_wb_sel,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_ls_bits_rf_wen,	// @[playground/src/EX_stage.scala:7:12]
  output [4:0]  EX_to_ls_bits_rd,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_ls_bits_result,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_nextpc,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_pc,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_inst,	// @[playground/src/EX_stage.scala:7:12]
  output [4:0]  EX_to_id_fw_addr,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_id_fw_data,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_id_csr_ecpt_wen,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_id_csr_ecpt_pc_wb,	// @[playground/src/EX_stage.scala:7:12]
  output [11:0] EX_to_id_csr_waddr,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_id_csr_wen,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_id_csr_wdata,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_id_clog,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_id_flush,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_if_flush,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_preif_epc_taken,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_preif_epc_target,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_preif_Br_B_stall,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_preif_Br_B_taken,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_preif_Br_B_target,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_preif_flush,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_mem_addr,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_write_en,	// @[playground/src/EX_stage.scala:7:12]
  output [3:0]  EX_wstrb,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_wdata,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_read_en	// @[playground/src/EX_stage.scala:7:12]
);

  wire        _Csr_alu_io_wen;	// @[playground/src/EX_stage.scala:113:21]
  wire [31:0] _Csr_alu_io_out;	// @[playground/src/EX_stage.scala:113:21]
  wire [31:0] _Alu_io_result;	// @[playground/src/EX_stage.scala:42:17]
  wire        ld_wen = |EX_IO_bits_ld_type;	// @[playground/src/EX_stage.scala:26:28, :29:30]
  wire        st_wen = |EX_IO_bits_st_type;	// @[playground/src/EX_stage.scala:27:28, :28:30]
  reg         ex_valid;	// @[playground/src/EX_stage.scala:32:33]
  wire        ex_clog;	// @[playground/src/EX_stage.scala:30:29]
  wire        ex_ready_go = ~ex_clog;	// @[playground/src/EX_stage.scala:28:30, :30:29, :33:33, :34:19]
  wire        _EX_IO_ready_output = ~ex_valid | ex_ready_go & EX_to_ls_ready;	// @[playground/src/EX_stage.scala:32:33, :33:33, :35:{17,27,42}]
  wire        _EX_to_preif_Br_B_taken_output = EX_IO_bits_b_taken & ex_valid;	// @[playground/src/EX_stage.scala:32:33, :48:46]
  wire        _EX_to_preif_epc_target_T = EX_IO_bits_csr_cmd == 5'h3;	// @[playground/src/EX_stage.scala:65:32]
  wire        _EX_to_preif_epc_target_T_1 = EX_IO_bits_csr_cmd == 5'h4;	// @[playground/src/EX_stage.scala:66:32]
  wire        to_flush =
    (_EX_to_preif_Br_B_taken_output | _EX_to_preif_epc_target_T
     | _EX_to_preif_epc_target_T_1) & ex_valid;	// @[playground/src/EX_stage.scala:32:33, :48:46, :65:32, :66:{10,32,46}]
  wire        _store_half_sel_T_2 = _Alu_io_result[1:0] == 2'h2;	// @[playground/src/EX_stage.scala:42:17, :80:33, :81:49]
  assign ex_clog = (~EX_wdata_ok & st_wen | ~EX_raddr_ok & ld_wen) & ex_valid;	// @[playground/src/EX_stage.scala:26:28, :27:28, :30:29, :32:33, :107:{15,28}, :108:{12,15,28,37}]
  wire        _EX_to_id_csr_wen_output = _Csr_alu_io_wen & ex_valid;	// @[playground/src/EX_stage.scala:32:33, :113:21, :118:35]
  wire        _EX_to_id_csr_ecpt_wen_output = _EX_to_preif_epc_target_T_1 & ex_valid;	// @[playground/src/EX_stage.scala:32:33, :66:32, :120:58]
  wire        _EX_to_ls_bits_dpic_bundle_ex_is_jal_T = EX_IO_bits_br_type == 4'h7;	// @[playground/src/EX_stage.scala:148:62]
  always @(posedge clock) begin	// @[<stdin>:1269:11]
    if (reset)	// @[<stdin>:1269:11]
      ex_valid <= 1'h0;	// @[playground/src/EX_stage.scala:28:30, :32:33]
    else if (_EX_IO_ready_output)	// @[playground/src/EX_stage.scala:35:27]
      ex_valid <= EX_IO_valid;	// @[playground/src/EX_stage.scala:32:33]
  end // always @(posedge)
  Alu Alu (	// @[playground/src/EX_stage.scala:42:17]
    .io_op     (EX_IO_bits_alu_op),
    .io_src1   (EX_IO_bits_src1),
    .io_src2   (EX_IO_bits_src2),
    .io_result (_Alu_io_result)
  );
  Csr_alu Csr_alu (	// @[playground/src/EX_stage.scala:113:21]
    .io_csr_cmd   (EX_IO_bits_csr_cmd),
    .io_in_csr    (_Alu_io_result),	// @[playground/src/EX_stage.scala:42:17]
    .io_in_rdata1 (EX_IO_bits_rdata1),
    .io_wen       (_Csr_alu_io_wen),
    .io_out       (_Csr_alu_io_out)
  );
  assign EX_IO_ready = _EX_IO_ready_output;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:35:27]
  assign EX_to_ls_valid = ex_valid & ex_ready_go;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:32:33, :33:33, :39:28]
  assign EX_to_ls_bits_csr_commit_wen = _EX_to_id_csr_wen_output;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:118:35]
  assign EX_to_ls_bits_csr_commit_waddr = EX_IO_bits_csr_addr;	// @[<stdin>:1268:3]
  assign EX_to_ls_bits_csr_commit_wdata = _Csr_alu_io_out;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:113:21]
  assign EX_to_ls_bits_csr_commit_exception_wen = _EX_to_id_csr_ecpt_wen_output;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:120:58]
  assign EX_to_ls_bits_csr_commit_exception_pc_wb = EX_IO_bits_pc;	// @[<stdin>:1268:3]
  assign EX_to_ls_bits_dpic_bundle_id_inv_flag = EX_IO_bits_dpic_bundle_id_inv_flag;	// @[<stdin>:1268:3]
  assign EX_to_ls_bits_dpic_bundle_ex_func_flag =
    _EX_to_ls_bits_dpic_bundle_ex_is_jal_T | EX_IO_bits_br_type == 4'h8;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:81:49, :148:{62,72,92}]
  assign EX_to_ls_bits_dpic_bundle_ex_is_jal = _EX_to_ls_bits_dpic_bundle_ex_is_jal_T;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:148:62]
  assign EX_to_ls_bits_dpic_bundle_ex_is_ret = EX_IO_bits_inst == 32'h8067;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:150:55]
  assign EX_to_ls_bits_dpic_bundle_ex_is_rd0 = EX_IO_bits_rd == 5'h0;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:76:24, :151:53]
  assign EX_to_ls_bits_dpic_bundle_ex_ld_type = EX_IO_bits_ld_type;	// @[<stdin>:1268:3]
  assign EX_to_ls_bits_dpic_bundle_ex_st_type = EX_IO_bits_st_type;	// @[<stdin>:1268:3]
  assign EX_to_ls_bits_dpic_bundle_ex_mem_addr = st_wen | ld_wen ? _Alu_io_result : 32'h0;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:26:28, :27:28, :42:17, :155:{45,53}, :158:45]
  assign EX_to_ls_bits_dpic_bundle_ex_st_data = st_wen ? EX_IO_bits_rdata2 : 32'h0;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:27:28, :156:44, :158:45]
  assign EX_to_ls_bits_addr_low2bit = _Alu_io_result[1:0];	// @[<stdin>:1268:3, playground/src/EX_stage.scala:42:17, :80:33]
  assign EX_to_ls_bits_ld_wen = ld_wen;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:26:28]
  assign EX_to_ls_bits_ld_type = EX_IO_bits_ld_type;	// @[<stdin>:1268:3]
  assign EX_to_ls_bits_csr_cmd = EX_IO_bits_csr_cmd;	// @[<stdin>:1268:3]
  assign EX_to_ls_bits_wb_sel = EX_IO_bits_wb_sel;	// @[<stdin>:1268:3]
  assign EX_to_ls_bits_rf_wen = EX_IO_bits_rf_wen;	// @[<stdin>:1268:3]
  assign EX_to_ls_bits_rd = EX_IO_bits_rd;	// @[<stdin>:1268:3]
  assign EX_to_ls_bits_result = _Alu_io_result;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:42:17]
  assign EX_to_ls_bits_nextpc = EX_IO_bits_nextpc;	// @[<stdin>:1268:3]
  assign EX_to_ls_bits_pc = EX_IO_bits_pc;	// @[<stdin>:1268:3]
  assign EX_to_ls_bits_inst = EX_IO_bits_inst;	// @[<stdin>:1268:3]
  assign EX_to_id_fw_addr = ex_valid & EX_IO_bits_rf_wen ? EX_IO_bits_rd : 5'h0;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:32:33, :76:{24,34}]
  assign EX_to_id_fw_data = _Alu_io_result;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:42:17]
  assign EX_to_id_csr_ecpt_wen = _EX_to_id_csr_ecpt_wen_output;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:120:58]
  assign EX_to_id_csr_ecpt_pc_wb = EX_IO_bits_pc;	// @[<stdin>:1268:3]
  assign EX_to_id_csr_waddr = EX_IO_bits_csr_addr;	// @[<stdin>:1268:3]
  assign EX_to_id_csr_wen = _EX_to_id_csr_wen_output;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:118:35]
  assign EX_to_id_csr_wdata = _Csr_alu_io_out;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:113:21]
  assign EX_to_id_clog = (|EX_IO_bits_ld_type) & ex_valid;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:29:30, :32:33, :72:44]
  assign EX_to_id_flush = to_flush;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:66:46]
  assign EX_to_if_flush = to_flush;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:66:46]
  assign EX_to_preif_epc_taken = EX_IO_bits_pc_sel & ex_valid;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:32:33, :124:54]
  assign EX_to_preif_epc_target =
    _EX_to_preif_epc_target_T
      ? EX_IO_bits_csr_global_mepc
      : _EX_to_preif_epc_target_T_1 ? EX_IO_bits_csr_global_mtvec : 32'h0;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:65:32, :66:32, :125:30, :126:28, :158:45]
  assign EX_to_preif_Br_B_stall = _EX_to_preif_Br_B_taken_output & ex_ready_go;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:33:33, :48:{46,57}]
  assign EX_to_preif_Br_B_taken = _EX_to_preif_Br_B_taken_output;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:48:46]
  assign EX_to_preif_Br_B_target =
    EX_IO_bits_br_type == 4'h6 | EX_IO_bits_br_type == 4'h5 | EX_IO_bits_br_type == 4'h4
    | EX_IO_bits_br_type == 4'h3 | EX_IO_bits_br_type == 4'h2 | EX_IO_bits_br_type == 4'h1
      ? _Alu_io_result
      : 32'h0;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:42:17, :50:61, :158:45]
  assign EX_to_preif_flush = to_flush;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:66:46]
  assign EX_mem_addr = _Alu_io_result & 32'hFFFFFFFC;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:42:17, :93:{30,32}]
  assign EX_write_en = st_wen;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:27:28]
  assign EX_wstrb =
    EX_IO_bits_st_type == 3'h3
      ? 4'hF
      : EX_IO_bits_st_type == 3'h2
          ? ((&(_Alu_io_result[1:0])) | _store_half_sel_T_2 ? 4'hC : 4'h3)
          : EX_IO_bits_st_type == 3'h1
              ? ((&(_Alu_io_result[1:0]))
                   ? 4'h8
                   : {1'h0,
                      _store_half_sel_T_2
                        ? 3'h4
                        : {1'h0, _Alu_io_result[1:0] == 2'h1 ? 2'h2 : 2'h1}})
              : 4'h0;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:28:30, :42:17, :50:61, :80:33, :81:49, :87:49, :94:46]
  assign EX_wdata = EX_IO_bits_rdata2;	// @[<stdin>:1268:3]
  assign EX_read_en = ld_wen;	// @[<stdin>:1268:3, playground/src/EX_stage.scala:26:28]
endmodule

