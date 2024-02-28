// Generated by CIRCT firtool-1.56.0
module IF_stage(	// @[<stdin>:3:3]
  input         clock,	// @[<stdin>:4:11]
                reset,	// @[<stdin>:5:11]
                IF_to_id_ready,	// @[playground/src/IF_stage.scala:6:12]
                IF_for_id_Br_J_taken,	// @[playground/src/IF_stage.scala:6:12]
  input  [31:0] IF_for_id_Br_J_target,	// @[playground/src/IF_stage.scala:6:12]
  input         IF_for_id_flush,	// @[playground/src/IF_stage.scala:6:12]
  input  [31:0] IF_for_ex_epc_target,	// @[playground/src/IF_stage.scala:6:12]
  input         IF_for_ex_epc_taken,	// @[playground/src/IF_stage.scala:6:12]
                IF_for_ex_Br_B_taken,	// @[playground/src/IF_stage.scala:6:12]
  input  [31:0] IF_for_ex_Br_B_target,	// @[playground/src/IF_stage.scala:6:12]
  input         IF_for_ex_flush,	// @[playground/src/IF_stage.scala:6:12]
                IF_ar_ready,	// @[playground/src/IF_stage.scala:6:12]
                IF_r_valid,	// @[playground/src/IF_stage.scala:6:12]
  input  [31:0] IF_r_bits_data,	// @[playground/src/IF_stage.scala:6:12]
  input  [1:0]  IF_r_bits_resp,	// @[playground/src/IF_stage.scala:6:12]
  input         IF_aw_ready,	// @[playground/src/IF_stage.scala:6:12]
                IF_w_ready,	// @[playground/src/IF_stage.scala:6:12]
                IF_b_valid,	// @[playground/src/IF_stage.scala:6:12]
  input  [1:0]  IF_b_bits_resp,	// @[playground/src/IF_stage.scala:6:12]
  output        IF_to_id_valid,	// @[playground/src/IF_stage.scala:6:12]
  output [31:0] IF_to_id_bits_nextpc,	// @[playground/src/IF_stage.scala:6:12]
                IF_to_id_bits_pc,	// @[playground/src/IF_stage.scala:6:12]
                IF_to_id_bits_inst,	// @[playground/src/IF_stage.scala:6:12]
  output        IF_ar_valid,	// @[playground/src/IF_stage.scala:6:12]
  output [31:0] IF_ar_bits_addr,	// @[playground/src/IF_stage.scala:6:12]
  output [2:0]  IF_ar_bits_prot,	// @[playground/src/IF_stage.scala:6:12]
  output        IF_r_ready,	// @[playground/src/IF_stage.scala:6:12]
                IF_aw_valid,	// @[playground/src/IF_stage.scala:6:12]
  output [31:0] IF_aw_bits_addr,	// @[playground/src/IF_stage.scala:6:12]
  output [2:0]  IF_aw_bits_prot,	// @[playground/src/IF_stage.scala:6:12]
  output        IF_w_valid,	// @[playground/src/IF_stage.scala:6:12]
  output [31:0] IF_w_bits_data,	// @[playground/src/IF_stage.scala:6:12]
  output [7:0]  IF_w_bits_strb,	// @[playground/src/IF_stage.scala:6:12]
  output        IF_b_ready	// @[playground/src/IF_stage.scala:6:12]
);

  wire        if_ready_go = IF_to_id_ready;	// @[playground/src/IF_stage.scala:27:33]
  wire        if_flush = IF_for_ex_flush | IF_for_id_flush;	// @[playground/src/IF_stage.scala:23:30, :24:29]
  reg         if_valid;	// @[playground/src/IF_stage.scala:26:33]
  wire        _IF_r_ready_output = if_valid;	// @[<stdin>:3:3, playground/src/IF_stage.scala:26:33]
  reg  [31:0] if_pc;	// @[playground/src/IF_stage.scala:40:26]
  wire [31:0] if_snpc = if_pc + 32'h4;	// @[playground/src/IF_stage.scala:40:26, :41:33, :47:20]
  wire [31:0] if_dnpc =
    IF_for_ex_epc_taken
      ? IF_for_ex_epc_target
      : IF_for_id_Br_J_taken ? IF_for_id_Br_J_target : IF_for_ex_Br_B_target;	// @[playground/src/IF_stage.scala:37:17, :42:33, :48:17]
  wire [31:0] if_nextpc =
    IF_for_id_Br_J_taken | IF_for_ex_Br_B_taken | IF_for_ex_epc_taken ? if_dnpc : if_snpc;	// @[playground/src/IF_stage.scala:41:33, :42:33, :43:33, :49:{18,28}]
  reg         DoAddrReadReg;	// @[playground/src/IF_stage.scala:51:28]
  wire [31:0] if_inst = _IF_r_ready_output & IF_r_valid ? IF_r_bits_data : 32'h0;	// @[<stdin>:3:3, playground/src/IF_stage.scala:45:36, :57:18, :58:12, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  always @(posedge clock) begin	// @[<stdin>:4:11]
    if (reset) begin	// @[<stdin>:4:11]
      if_valid <= 1'h0;	// @[playground/src/IF_stage.scala:26:33]
      if_pc <= 32'h7FFFFFFC;	// @[playground/src/IF_stage.scala:40:26]
      DoAddrReadReg <= 1'h0;	// @[playground/src/IF_stage.scala:26:33, :51:28]
    end
    else begin	// @[<stdin>:4:11]
      if_valid <= if_ready_go | if_valid;	// @[playground/src/IF_stage.scala:26:33, :27:33, :29:20, :30:13]
      if (if_ready_go)	// @[playground/src/IF_stage.scala:27:33]
        if_pc <= if_nextpc;	// @[playground/src/IF_stage.scala:40:26, :43:33]
      DoAddrReadReg <= if_ready_go;	// @[playground/src/IF_stage.scala:27:33, :51:28]
    end
  end // always @(posedge)
  assign IF_to_id_valid = ~if_flush & if_valid & if_ready_go;	// @[<stdin>:3:3, playground/src/IF_stage.scala:23:30, :26:33, :27:33, :32:22]
  assign IF_to_id_bits_nextpc = if_nextpc;	// @[<stdin>:3:3, playground/src/IF_stage.scala:43:33]
  assign IF_to_id_bits_pc = if_pc;	// @[<stdin>:3:3, playground/src/IF_stage.scala:40:26]
  assign IF_to_id_bits_inst = if_inst;	// @[<stdin>:3:3, playground/src/IF_stage.scala:45:36]
  assign IF_ar_valid = DoAddrReadReg;	// @[<stdin>:3:3, playground/src/IF_stage.scala:51:28]
  assign IF_ar_bits_addr = if_nextpc;	// @[<stdin>:3:3, playground/src/IF_stage.scala:43:33]
  assign IF_ar_bits_prot = 3'h0;	// @[<stdin>:3:3, playground/src/IF_stage.scala:55:18]
  assign IF_r_ready = _IF_r_ready_output;	// @[<stdin>:3:3]
  assign IF_aw_valid = 1'h0;	// @[<stdin>:3:3, playground/src/IF_stage.scala:26:33]
  assign IF_aw_bits_addr = 32'h0;	// @[<stdin>:3:3, playground/src/IF_stage.scala:45:36]
  assign IF_aw_bits_prot = 3'h0;	// @[<stdin>:3:3, playground/src/IF_stage.scala:55:18]
  assign IF_w_valid = 1'h0;	// @[<stdin>:3:3, playground/src/IF_stage.scala:26:33]
  assign IF_w_bits_data = 32'h0;	// @[<stdin>:3:3, playground/src/IF_stage.scala:45:36]
  assign IF_w_bits_strb = 8'h0;	// @[<stdin>:3:3, playground/src/IF_stage.scala:64:17]
  assign IF_b_ready = 1'h0;	// @[<stdin>:3:3, playground/src/IF_stage.scala:26:33]
endmodule

