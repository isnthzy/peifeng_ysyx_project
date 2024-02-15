// Generated by CIRCT firtool-1.56.0
module EX_stage(	// @[<stdin>:991:3]
  input         clock,	// @[<stdin>:992:11]
                reset,	// @[<stdin>:993:11]
                EX_IO_valid,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_dpic_bundle_id_inv_flag,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_pc_sel,	// @[playground/src/EX_stage.scala:6:12]
  input  [11:0] EX_IO_bits_csr_addr,	// @[playground/src/EX_stage.scala:6:12]
  input  [4:0]  EX_IO_bits_csr_cmd,	// @[playground/src/EX_stage.scala:6:12]
  input  [7:0]  EX_IO_bits_st_type,	// @[playground/src/EX_stage.scala:6:12]
  input  [2:0]  EX_IO_bits_ld_type,	// @[playground/src/EX_stage.scala:6:12]
  input         EX_IO_bits_ebreak_flag,	// @[playground/src/EX_stage.scala:6:12]
  input  [1:0]  EX_IO_bits_wb_sel,	// @[playground/src/EX_stage.scala:6:12]
  input  [3:0]  EX_IO_bits_br_type,	// @[playground/src/EX_stage.scala:6:12]
  input         EX_IO_bits_wen,	// @[playground/src/EX_stage.scala:6:12]
  input  [4:0]  EX_IO_bits_rd,	// @[playground/src/EX_stage.scala:6:12]
  input  [3:0]  EX_IO_bits_alu_op,	// @[playground/src/EX_stage.scala:6:12]
  input  [31:0] EX_IO_bits_src1,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_src2,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_rdata1,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_rdata2,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_nextpc,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_pc,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_inst,	// @[playground/src/EX_stage.scala:6:12]
  input         EX_to_ls_ready,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_IO_ready,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_valid,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_dpic_bundle_id_inv_flag,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_dpic_bundle_ex_func_flag,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_dpic_bundle_ex_is_jal,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_dpic_bundle_ex_is_ret,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_dpic_bundle_ex_is_rd0,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_pc_sel,	// @[playground/src/EX_stage.scala:6:12]
  output [11:0] EX_to_ls_bits_csr_addr,	// @[playground/src/EX_stage.scala:6:12]
  output [4:0]  EX_to_ls_bits_csr_cmd,	// @[playground/src/EX_stage.scala:6:12]
  output [7:0]  EX_to_ls_bits_st_type,	// @[playground/src/EX_stage.scala:6:12]
  output [2:0]  EX_to_ls_bits_ld_type,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_to_ls_bits_ebreak_flag,	// @[playground/src/EX_stage.scala:6:12]
  output [1:0]  EX_to_ls_bits_wb_sel,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_to_ls_bits_wen,	// @[playground/src/EX_stage.scala:6:12]
  output [4:0]  EX_to_ls_bits_rd,	// @[playground/src/EX_stage.scala:6:12]
  output [31:0] EX_to_ls_bits_rdata2,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_result,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_nextpc,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_pc,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_inst,	// @[playground/src/EX_stage.scala:6:12]
  output [4:0]  EX_to_id_addr,	// @[playground/src/EX_stage.scala:6:12]
  output [31:0] EX_to_id_data,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_flush_out,	// @[playground/src/EX_stage.scala:6:12]
                EX_br_bus_is_jump,	// @[playground/src/EX_stage.scala:6:12]
  output [31:0] EX_br_bus_dnpc	// @[playground/src/EX_stage.scala:6:12]
);

  wire [31:0] _Alu_io_result;	// @[playground/src/EX_stage.scala:25:17]
  wire        ex_ready_go = 1'h1;	// @[playground/src/EX_stage.scala:16:33, :17:14]
  reg         ex_valid;	// @[playground/src/EX_stage.scala:15:33]
  wire        _EX_IO_ready_output = ~ex_valid | ex_ready_go & EX_to_ls_ready;	// @[playground/src/EX_stage.scala:15:33, :16:33, :18:{18,28,43}]
  wire        rs1_eq_rs2 = EX_IO_bits_rdata1 == EX_IO_bits_rdata2;	// @[playground/src/EX_stage.scala:31:40]
  wire        rs1_lt_rs2_s = $signed(EX_IO_bits_rdata1) < $signed(EX_IO_bits_rdata2);	// @[playground/src/EX_stage.scala:32:47]
  wire        rs1_lt_rs2_u = EX_IO_bits_rdata1 < EX_IO_bits_rdata2;	// @[playground/src/EX_stage.scala:33:41]
  wire        _EX_to_ls_bits_dpic_bundle_ex_is_jal_T = EX_IO_bits_br_type == 4'h7;	// @[playground/src/EX_stage.scala:35:44]
  wire        _EX_to_ls_bits_dpic_bundle_ex_func_flag_T_1 = EX_IO_bits_br_type == 4'h8;	// @[playground/src/EX_stage.scala:36:44]
  wire        _EX_br_bus_is_jump_output =
    (_EX_to_ls_bits_dpic_bundle_ex_is_jal_T | _EX_to_ls_bits_dpic_bundle_ex_func_flag_T_1
     | EX_IO_bits_br_type == 4'h3 & rs1_eq_rs2 | EX_IO_bits_br_type == 4'h6 & ~rs1_eq_rs2
     | EX_IO_bits_br_type == 4'h2 & rs1_lt_rs2_s | EX_IO_bits_br_type == 4'h1
     & rs1_lt_rs2_u | EX_IO_bits_br_type == 4'h5 & ~rs1_lt_rs2_s
     | EX_IO_bits_br_type == 4'h4 & ~rs1_lt_rs2_u) & ex_valid;	// @[playground/src/EX_stage.scala:15:33, :31:40, :32:47, :33:41, :35:44, :36:44, :37:{45,55}, :38:{45,55,58}, :39:{45,55}, :40:{45,55}, :41:{45,55,58}, :42:{23,45,55,58,73}]
  always @(posedge clock) begin	// @[<stdin>:992:11]
    if (reset)	// @[<stdin>:992:11]
      ex_valid <= 1'h0;	// @[playground/src/EX_stage.scala:15:33]
    else if (_EX_IO_ready_output)	// @[playground/src/EX_stage.scala:18:28]
      ex_valid <= EX_IO_valid;	// @[playground/src/EX_stage.scala:15:33]
  end // always @(posedge)
  Alu Alu (	// @[playground/src/EX_stage.scala:25:17]
    .io_op     (EX_IO_bits_alu_op),
    .io_src1   (EX_IO_bits_src1),
    .io_src2   (EX_IO_bits_src2),
    .io_result (_Alu_io_result)
  );
  assign EX_IO_ready = _EX_IO_ready_output;	// @[<stdin>:991:3, playground/src/EX_stage.scala:18:28]
  assign EX_to_ls_valid = ex_valid & ex_ready_go;	// @[<stdin>:991:3, playground/src/EX_stage.scala:15:33, :16:33, :22:28]
  assign EX_to_ls_bits_dpic_bundle_id_inv_flag = EX_IO_bits_dpic_bundle_id_inv_flag;	// @[<stdin>:991:3]
  assign EX_to_ls_bits_dpic_bundle_ex_func_flag =
    _EX_to_ls_bits_dpic_bundle_ex_is_jal_T | _EX_to_ls_bits_dpic_bundle_ex_func_flag_T_1;	// @[<stdin>:991:3, playground/src/EX_stage.scala:35:44, :36:44, :84:72]
  assign EX_to_ls_bits_dpic_bundle_ex_is_jal = _EX_to_ls_bits_dpic_bundle_ex_is_jal_T;	// @[<stdin>:991:3, playground/src/EX_stage.scala:35:44]
  assign EX_to_ls_bits_dpic_bundle_ex_is_ret = EX_IO_bits_inst == 32'h8067;	// @[<stdin>:991:3, playground/src/EX_stage.scala:86:55]
  assign EX_to_ls_bits_dpic_bundle_ex_is_rd0 = EX_IO_bits_rd == 5'h0;	// @[<stdin>:991:3, playground/src/EX_stage.scala:58:21, :87:53]
  assign EX_to_ls_bits_pc_sel = EX_IO_bits_pc_sel;	// @[<stdin>:991:3]
  assign EX_to_ls_bits_csr_addr = EX_IO_bits_csr_addr;	// @[<stdin>:991:3]
  assign EX_to_ls_bits_csr_cmd = EX_IO_bits_csr_cmd;	// @[<stdin>:991:3]
  assign EX_to_ls_bits_st_type = EX_IO_bits_st_type;	// @[<stdin>:991:3]
  assign EX_to_ls_bits_ld_type = EX_IO_bits_ld_type;	// @[<stdin>:991:3]
  assign EX_to_ls_bits_ebreak_flag = EX_IO_bits_ebreak_flag;	// @[<stdin>:991:3]
  assign EX_to_ls_bits_wb_sel = EX_IO_bits_wb_sel;	// @[<stdin>:991:3]
  assign EX_to_ls_bits_wen = EX_IO_bits_wen;	// @[<stdin>:991:3]
  assign EX_to_ls_bits_rd = EX_IO_bits_rd;	// @[<stdin>:991:3]
  assign EX_to_ls_bits_rdata2 = EX_IO_bits_rdata2;	// @[<stdin>:991:3]
  assign EX_to_ls_bits_result = _Alu_io_result;	// @[<stdin>:991:3, playground/src/EX_stage.scala:25:17]
  assign EX_to_ls_bits_nextpc = EX_IO_bits_nextpc;	// @[<stdin>:991:3]
  assign EX_to_ls_bits_pc = EX_IO_bits_pc;	// @[<stdin>:991:3]
  assign EX_to_ls_bits_inst = EX_IO_bits_inst;	// @[<stdin>:991:3]
  assign EX_to_id_addr = ex_valid & EX_IO_bits_wen ? EX_IO_bits_rd : 5'h0;	// @[<stdin>:991:3, playground/src/EX_stage.scala:15:33, :58:{21,31}]
  assign EX_to_id_data = _Alu_io_result;	// @[<stdin>:991:3, playground/src/EX_stage.scala:25:17]
  assign EX_flush_out = _EX_br_bus_is_jump_output;	// @[<stdin>:991:3, playground/src/EX_stage.scala:42:73]
  assign EX_br_bus_is_jump = _EX_br_bus_is_jump_output;	// @[<stdin>:991:3, playground/src/EX_stage.scala:42:73]
  assign EX_br_bus_dnpc =
    EX_IO_bits_br_type == 4'h8
      ? {_Alu_io_result[31:1], 1'h0}
      : EX_IO_bits_br_type == 4'h7 | EX_IO_bits_br_type == 4'h6
        | EX_IO_bits_br_type == 4'h5 | EX_IO_bits_br_type == 4'h4
        | EX_IO_bits_br_type == 4'h3 | EX_IO_bits_br_type == 4'h2
        | EX_IO_bits_br_type == 4'h1
          ? _Alu_io_result
          : 32'h0;	// @[<stdin>:991:3, playground/src/EX_stage.scala:15:33, :25:17, :35:44, :36:44, :37:45, :38:45, :39:45, :40:45, :41:45, :42:45, :45:52, :54:{18,32}]
endmodule

