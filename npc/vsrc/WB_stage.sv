// Generated by CIRCT firtool-1.56.0
module WB_stage(	// @[<stdin>:1707:3]
  input         clock,	// @[<stdin>:1708:11]
                reset,	// @[<stdin>:1709:11]
                WB_IO_valid,	// @[playground/src/WB_stage.scala:7:12]
                WB_IO_bits_csr_commit_wen,	// @[playground/src/WB_stage.scala:7:12]
  input  [11:0] WB_IO_bits_csr_commit_waddr,	// @[playground/src/WB_stage.scala:7:12]
  input  [31:0] WB_IO_bits_csr_commit_wdata,	// @[playground/src/WB_stage.scala:7:12]
  input         WB_IO_bits_csr_commit_exception_wen,	// @[playground/src/WB_stage.scala:7:12]
  input  [3:0]  WB_IO_bits_csr_commit_exception_mcause_in,	// @[playground/src/WB_stage.scala:7:12]
  input  [31:0] WB_IO_bits_csr_commit_exception_pc_wb,	// @[playground/src/WB_stage.scala:7:12]
  input         WB_IO_bits_dpic_bundle_id_inv_flag,	// @[playground/src/WB_stage.scala:7:12]
                WB_IO_bits_dpic_bundle_ex_func_flag,	// @[playground/src/WB_stage.scala:7:12]
                WB_IO_bits_dpic_bundle_ex_is_jal,	// @[playground/src/WB_stage.scala:7:12]
                WB_IO_bits_dpic_bundle_ex_is_ret,	// @[playground/src/WB_stage.scala:7:12]
                WB_IO_bits_dpic_bundle_ex_is_rd0,	// @[playground/src/WB_stage.scala:7:12]
  input  [2:0]  WB_IO_bits_dpic_bundle_ex_ld_type,	// @[playground/src/WB_stage.scala:7:12]
  input  [3:0]  WB_IO_bits_dpic_bundle_ex_st_type,	// @[playground/src/WB_stage.scala:7:12]
  input  [31:0] WB_IO_bits_dpic_bundle_ex_mem_addr,	// @[playground/src/WB_stage.scala:7:12]
                WB_IO_bits_dpic_bundle_ex_st_data,	// @[playground/src/WB_stage.scala:7:12]
                WB_IO_bits_dpic_bundle_ls_ld_data,	// @[playground/src/WB_stage.scala:7:12]
  input  [4:0]  WB_IO_bits_csr_cmd,	// @[playground/src/WB_stage.scala:7:12]
  input         WB_IO_bits_rf_wen,	// @[playground/src/WB_stage.scala:7:12]
  input  [4:0]  WB_IO_bits_rd,	// @[playground/src/WB_stage.scala:7:12]
  input  [31:0] WB_IO_bits_result,	// @[playground/src/WB_stage.scala:7:12]
                WB_IO_bits_nextpc,	// @[playground/src/WB_stage.scala:7:12]
                WB_IO_bits_pc,	// @[playground/src/WB_stage.scala:7:12]
                WB_IO_bits_inst,	// @[playground/src/WB_stage.scala:7:12]
  output        WB_IO_ready,	// @[playground/src/WB_stage.scala:7:12]
  output [4:0]  WB_to_id_rf_waddr,	// @[playground/src/WB_stage.scala:7:12]
  output [31:0] WB_to_id_rf_wdata,	// @[playground/src/WB_stage.scala:7:12]
  output        WB_to_id_rf_wen,	// @[playground/src/WB_stage.scala:7:12]
  output [4:0]  WB_debug_waddr,	// @[playground/src/WB_stage.scala:7:12]
  output [31:0] WB_debug_wdata,	// @[playground/src/WB_stage.scala:7:12]
  output        WB_debug_wen	// @[playground/src/WB_stage.scala:7:12]
);

  wire wb_ready_go = 1'h1;	// @[playground/src/WB_stage.scala:18:33, :19:14]
  reg  wb_valid;	// @[playground/src/WB_stage.scala:17:33]
  wire _WB_IO_ready_output = ~wb_valid | wb_ready_go;	// @[playground/src/WB_stage.scala:17:33, :18:33, :20:{18,28}]
  wire _WB_to_id_rf_wen_output = WB_IO_bits_rf_wen & wb_valid;	// @[playground/src/WB_stage.scala:17:33, :28:39]
  always @(posedge clock) begin	// @[<stdin>:1708:11]
    if (reset)	// @[<stdin>:1708:11]
      wb_valid <= 1'h0;	// @[playground/src/WB_stage.scala:17:33]
    else if (_WB_IO_ready_output)	// @[playground/src/WB_stage.scala:20:28]
      wb_valid <= WB_IO_valid;	// @[playground/src/WB_stage.scala:17:33]
  end // always @(posedge)
  DPI_stage DPI_stage (	// @[playground/src/WB_stage.scala:35:23]
    .clock                              (clock),
    .reset                              (reset),
    .DPI_wb_valid                       (wb_valid),	// @[playground/src/WB_stage.scala:17:33]
    .DPI_pc                             (WB_IO_bits_pc),
    .DPI_nextpc                         (WB_IO_bits_nextpc),
    .DPI_inst                           (WB_IO_bits_inst),
    .DPI_inv_flag                       (WB_IO_bits_dpic_bundle_id_inv_flag),
    .DPI_func_flag                      (WB_IO_bits_dpic_bundle_ex_func_flag),
    .DPI_is_jal                         (WB_IO_bits_dpic_bundle_ex_is_jal),
    .DPI_is_ret                         (WB_IO_bits_dpic_bundle_ex_is_ret),
    .DPI_is_rd0                         (WB_IO_bits_dpic_bundle_ex_is_rd0),
    .DPI_is_ebreak                      (WB_IO_bits_csr_cmd == 5'h5),	// @[playground/src/WB_stage.scala:45:47]
    .DPI_ret_reg_data                   (WB_IO_bits_result[0]),	// @[playground/src/WB_stage.scala:46:29]
    .DPI_csr_commit_wen                 (WB_IO_bits_csr_commit_wen),
    .DPI_csr_commit_waddr               (WB_IO_bits_csr_commit_waddr),
    .DPI_csr_commit_wdata               (WB_IO_bits_csr_commit_wdata),
    .DPI_csr_commit_exception_wen       (WB_IO_bits_csr_commit_exception_wen),
    .DPI_csr_commit_exception_mcause_in (WB_IO_bits_csr_commit_exception_mcause_in),
    .DPI_csr_commit_exception_pc_wb     (WB_IO_bits_csr_commit_exception_pc_wb),
    .DPI_ld_type                        (WB_IO_bits_dpic_bundle_ex_ld_type),
    .DPI_st_type                        (WB_IO_bits_dpic_bundle_ex_st_type),
    .DPI_mem_addr                       (WB_IO_bits_dpic_bundle_ex_mem_addr),
    .DPI_st_data                        (WB_IO_bits_dpic_bundle_ex_st_data),
    .DPI_ld_data                        (WB_IO_bits_dpic_bundle_ls_ld_data)
  );
  assign WB_IO_ready = _WB_IO_ready_output;	// @[<stdin>:1707:3, playground/src/WB_stage.scala:20:28]
  assign WB_to_id_rf_waddr = WB_IO_bits_rd;	// @[<stdin>:1707:3]
  assign WB_to_id_rf_wdata = WB_IO_bits_result;	// @[<stdin>:1707:3]
  assign WB_to_id_rf_wen = _WB_to_id_rf_wen_output;	// @[<stdin>:1707:3, playground/src/WB_stage.scala:28:39]
  assign WB_debug_waddr = WB_IO_bits_rd;	// @[<stdin>:1707:3]
  assign WB_debug_wdata = WB_IO_bits_result;	// @[<stdin>:1707:3]
  assign WB_debug_wen = _WB_to_id_rf_wen_output;	// @[<stdin>:1707:3, playground/src/WB_stage.scala:28:39]
endmodule

