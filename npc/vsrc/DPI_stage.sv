// Generated by CIRCT firtool-1.56.0
module DPI_stage(	// @[<stdin>:1600:3]
  input        clock,	// @[<stdin>:1601:11]
               reset,	// @[<stdin>:1602:11]
               DPI_wb_valid,	// @[playground/src/Dpi_stage.scala:13:13]
  input [31:0] DPI_pc,	// @[playground/src/Dpi_stage.scala:13:13]
               DPI_nextpc,	// @[playground/src/Dpi_stage.scala:13:13]
               DPI_inst,	// @[playground/src/Dpi_stage.scala:13:13]
  input        DPI_inv_flag,	// @[playground/src/Dpi_stage.scala:13:13]
               DPI_func_flag,	// @[playground/src/Dpi_stage.scala:13:13]
               DPI_is_jal,	// @[playground/src/Dpi_stage.scala:13:13]
               DPI_is_ret,	// @[playground/src/Dpi_stage.scala:13:13]
               DPI_is_rd0,	// @[playground/src/Dpi_stage.scala:13:13]
               DPI_is_ebreak,	// @[playground/src/Dpi_stage.scala:13:13]
               DPI_ret_reg_data,	// @[playground/src/Dpi_stage.scala:13:13]
               DPI_csr_commit_wen,	// @[playground/src/Dpi_stage.scala:13:13]
  input [11:0] DPI_csr_commit_waddr,	// @[playground/src/Dpi_stage.scala:13:13]
  input [31:0] DPI_csr_commit_wdata,	// @[playground/src/Dpi_stage.scala:13:13]
  input        DPI_csr_commit_exception_wen,	// @[playground/src/Dpi_stage.scala:13:13]
  input [3:0]  DPI_csr_commit_exception_mcause_in,	// @[playground/src/Dpi_stage.scala:13:13]
  input [31:0] DPI_csr_commit_exception_pc_wb,	// @[playground/src/Dpi_stage.scala:13:13]
  input [2:0]  DPI_ld_type,	// @[playground/src/Dpi_stage.scala:13:13]
  input [3:0]  DPI_st_type,	// @[playground/src/Dpi_stage.scala:13:13]
  input [31:0] DPI_mem_addr,	// @[playground/src/Dpi_stage.scala:13:13]
               DPI_st_data,	// @[playground/src/Dpi_stage.scala:13:13]
               DPI_ld_data	// @[playground/src/Dpi_stage.scala:13:13]
);

  Dpi_GetInfo dpi_getinfo (	// @[playground/src/Dpi_stage.scala:33:25]
    .clock     (clock),
    .reset     (reset),
    .dpi_valid (DPI_wb_valid),
    .pc        (DPI_pc),
    .nextpc    (DPI_nextpc),
    .inst      (DPI_inst)
  );
  dpi_inv dpi_inv (	// @[playground/src/Dpi_stage.scala:41:21]
    .clock     (clock),
    .reset     (reset),
    .dpi_valid (DPI_wb_valid),
    .inv_flag  (DPI_inv_flag),
    .pc        (DPI_pc)
  );
  Dpi_Func dpi_func (	// @[playground/src/Dpi_stage.scala:48:22]
    .clock     (clock),
    .reset     (reset),
    .dpi_valid (DPI_wb_valid),
    .func_flag (DPI_func_flag),
    .is_jal    (DPI_is_jal),
    .pc        (DPI_pc),
    .nextpc    (DPI_nextpc),
    .is_rd0    (DPI_is_rd0),
    .is_ret    (DPI_is_ret)
  );
  Dpi_Ebreak dpi_ebreak (	// @[playground/src/Dpi_stage.scala:59:24]
    .clock        (clock),
    .reset        (reset),
    .dpi_valid    (DPI_wb_valid),
    .is_ebreak    (DPI_is_ebreak),
    .pc           (DPI_pc),
    .ret_reg_data ({31'h0, DPI_ret_reg_data})	// @[playground/src/Dpi_stage.scala:65:29]
  );
  Dpi_CsrCommit dpi_csrcommit (	// @[playground/src/Dpi_stage.scala:67:27]
    .clock         (clock),
    .reset         (reset),
    .dpi_valid     (DPI_wb_valid),
    .csr_wen       (DPI_csr_commit_wen),
    .waddr         ({20'h0, DPI_csr_commit_waddr}),	// @[playground/src/Dpi_stage.scala:72:25]
    .wdata         (DPI_csr_commit_wdata),
    .exception_wen (DPI_csr_commit_exception_wen),
    .mcause_in     ({28'h0, DPI_csr_commit_exception_mcause_in}),	// @[playground/src/Dpi_stage.scala:75:29]
    .pc_wb         (DPI_csr_commit_exception_pc_wb)
  );
  Dpi_Mtrace dpi_mtrace (	// @[playground/src/Dpi_stage.scala:78:24]
    .clock     (clock),
    .reset     (reset),
    .dpi_valid (DPI_wb_valid),
    .pc        (DPI_pc),
    .ld_wen    (|DPI_ld_type),	// @[playground/src/Dpi_stage.scala:83:36]
    .st_wen    (|DPI_st_type),	// @[playground/src/Dpi_stage.scala:84:36]
    .ld_len
      ({29'h0,
        DPI_ld_type == 3'h4
          ? 3'h2
          : DPI_ld_type == 3'h5
              ? 3'h1
              : DPI_ld_type == 3'h1
                  ? 3'h4
                  : {1'h0, DPI_ld_type == 3'h2 ? 2'h2 : {1'h0, DPI_ld_type == 3'h3}}}),	// @[playground/src/Dpi_stage.scala:85:{23,51}]
    .st_len
      ({29'h0,
        DPI_st_type == 4'h3
          ? 3'h4
          : {1'h0, DPI_st_type == 4'h2 ? 2'h2 : {1'h0, DPI_st_type == 4'h1}}}),	// @[playground/src/Dpi_stage.scala:85:{23,51}, :94:{23,51}]
    .mem_addr  (DPI_mem_addr),
    .st_data   (DPI_st_data),
    .ld_data   (DPI_ld_data)
  );
endmodule

