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

module PreIF_s(	// @[<stdin>:3:3]
  input         clock,	// @[<stdin>:4:11]
                reset,	// @[<stdin>:5:11]
                PreIF_to_if_ready,	// @[playground/src/PreIF.scala:7:15]
                PreIF_for_id_Br_J_taken,	// @[playground/src/PreIF.scala:7:15]
  input  [31:0] PreIF_for_id_Br_J_target,	// @[playground/src/PreIF.scala:7:15]
  input         PreIF_for_id_flush,	// @[playground/src/PreIF.scala:7:15]
  input  [31:0] PreIF_for_ex_epc_target,	// @[playground/src/PreIF.scala:7:15]
  input         PreIF_for_ex_epc_taken,	// @[playground/src/PreIF.scala:7:15]
                PreIF_for_ex_Br_B_taken,	// @[playground/src/PreIF.scala:7:15]
  input  [31:0] PreIF_for_ex_Br_B_target,	// @[playground/src/PreIF.scala:7:15]
  input         PreIF_for_ex_flush,	// @[playground/src/PreIF.scala:7:15]
                PreIF_ar_ready,	// @[playground/src/PreIF.scala:7:15]
                PreIF_aw_ready,	// @[playground/src/PreIF.scala:7:15]
                PreIF_w_ready,	// @[playground/src/PreIF.scala:7:15]
                PreIF_b_valid,	// @[playground/src/PreIF.scala:7:15]
  input  [1:0]  PreIF_b_bits_resp,	// @[playground/src/PreIF.scala:7:15]
  output        PreIF_to_if_valid,	// @[playground/src/PreIF.scala:7:15]
  output [31:0] PreIF_to_if_bits_nextpc,	// @[playground/src/PreIF.scala:7:15]
                PreIF_to_if_bits_pc,	// @[playground/src/PreIF.scala:7:15]
  output        PreIF_ar_valid,	// @[playground/src/PreIF.scala:7:15]
  output [31:0] PreIF_ar_bits_addr,	// @[playground/src/PreIF.scala:7:15]
  output [2:0]  PreIF_ar_bits_prot,	// @[playground/src/PreIF.scala:7:15]
  output        PreIF_aw_valid,	// @[playground/src/PreIF.scala:7:15]
  output [31:0] PreIF_aw_bits_addr,	// @[playground/src/PreIF.scala:7:15]
  output [2:0]  PreIF_aw_bits_prot,	// @[playground/src/PreIF.scala:7:15]
  output        PreIF_w_valid,	// @[playground/src/PreIF.scala:7:15]
  output [31:0] PreIF_w_bits_data,	// @[playground/src/PreIF.scala:7:15]
  output [7:0]  PreIF_w_bits_strb,	// @[playground/src/PreIF.scala:7:15]
  output        PreIF_b_ready	// @[playground/src/PreIF.scala:7:15]
);

  wire        fetch_wen = PreIF_to_if_ready;	// @[playground/src/PreIF.scala:23:31]
  wire        resetn = ~reset;	// @[playground/src/PreIF.scala:18:28, :20:12]
  wire        PreIF_flush = PreIF_for_ex_flush | PreIF_for_id_flush;	// @[playground/src/PreIF.scala:26:33, :27:35]
  wire        _PreIF_to_if_valid_output = ~PreIF_flush & resetn;	// @[<stdin>:3:3, playground/src/PreIF.scala:18:28, :26:33, :29:26]
  reg  [31:0] PreIF_pc;	// @[playground/src/PreIF.scala:36:29]
  wire [31:0] PreIF_snpc = PreIF_pc + 32'h4;	// @[playground/src/PreIF.scala:36:29, :37:36, :41:26]
  wire [31:0] PreIF_dnpc =
    PreIF_for_ex_epc_taken
      ? PreIF_for_ex_epc_target
      : PreIF_for_id_Br_J_taken ? PreIF_for_id_Br_J_target : PreIF_for_ex_Br_B_target;	// @[playground/src/PreIF.scala:33:17, :38:36, :42:20]
  wire [31:0] PreIF_nextpc =
    PreIF_for_id_Br_J_taken | PreIF_for_ex_Br_B_taken | PreIF_for_ex_epc_taken
      ? PreIF_dnpc
      : PreIF_snpc;	// @[playground/src/PreIF.scala:37:36, :38:36, :39:36, :43:{21,31}]
  reg         arvalidReg;	// @[playground/src/PreIF.scala:50:25]
  reg  [31:0] araddrReg;	// @[playground/src/PreIF.scala:51:24]
  reg         ReadRequstState;	// @[playground/src/PreIF.scala:54:30]
  wire        is_fire = PreIF_to_if_ready & _PreIF_to_if_valid_output;	// @[<stdin>:3:3, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  wire        _GEN = is_fire & resetn;	// @[playground/src/PreIF.scala:18:28, :93:15, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  `ifndef SYNTHESIS	// @[playground/src/PreIF.scala:94:11]
    always @(posedge clock) begin	// @[playground/src/PreIF.scala:94:11]
      if ((`PRINTF_COND_) & _GEN & ~reset)	// @[playground/src/PreIF.scala:93:15, :94:11]
        $fwrite(32'h80000002, "PreIF: pc=%x, nextpc=%x , is_fire=%d\n", PreIF_pc,
                PreIF_nextpc, is_fire);	// @[playground/src/PreIF.scala:36:29, :39:36, :94:11, src/main/scala/chisel3/util/Decoupled.scala:52:35]
    end // always @(posedge)
  `endif // not def SYNTHESIS
  wire        _GEN_0 = ReadRequstState & PreIF_ar_ready;	// @[playground/src/PreIF.scala:54:30, :61:45, :62:25, :63:22]
  always @(posedge clock) begin	// @[<stdin>:4:11]
    if (reset) begin	// @[<stdin>:4:11]
      PreIF_pc <= 32'h7FFFFFFC;	// @[playground/src/PreIF.scala:36:29]
      arvalidReg <= 1'h0;	// @[playground/src/PreIF.scala:29:26, :50:25]
      araddrReg <= 32'h0;	// @[playground/src/PreIF.scala:51:24]
      ReadRequstState <= 1'h0;	// @[playground/src/PreIF.scala:29:26, :54:30]
    end
    else begin	// @[<stdin>:4:11]
      if (_GEN)	// @[playground/src/PreIF.scala:93:15]
        PreIF_pc <= PreIF_nextpc;	// @[playground/src/PreIF.scala:36:29, :39:36]
      if (ReadRequstState) begin	// @[playground/src/PreIF.scala:54:30]
        arvalidReg <= ~_GEN_0 & arvalidReg;	// @[playground/src/PreIF.scala:50:25, :54:30, :61:45, :62:25, :63:22, :64:17]
        ReadRequstState <= ~_GEN_0 & ReadRequstState;	// @[playground/src/PreIF.scala:54:30, :61:45, :62:25, :63:22]
      end
      else begin	// @[playground/src/PreIF.scala:54:30]
        arvalidReg <= fetch_wen | arvalidReg;	// @[playground/src/PreIF.scala:23:31, :50:25, :56:20, :59:17]
        ReadRequstState <= fetch_wen | ReadRequstState;	// @[playground/src/PreIF.scala:23:31, :54:30, :56:20, :57:22]
      end
      if (~ReadRequstState & fetch_wen)	// @[playground/src/PreIF.scala:23:31, :51:24, :54:30, :55:{23,33}, :56:20, :58:16]
        araddrReg <= PreIF_nextpc;	// @[playground/src/PreIF.scala:39:36, :51:24]
    end
  end // always @(posedge)
  assign PreIF_to_if_valid = _PreIF_to_if_valid_output;	// @[<stdin>:3:3]
  assign PreIF_to_if_bits_nextpc = PreIF_nextpc;	// @[<stdin>:3:3, playground/src/PreIF.scala:39:36]
  assign PreIF_to_if_bits_pc = PreIF_pc;	// @[<stdin>:3:3, playground/src/PreIF.scala:36:29]
  assign PreIF_ar_valid = arvalidReg;	// @[<stdin>:3:3, playground/src/PreIF.scala:50:25]
  assign PreIF_ar_bits_addr = araddrReg;	// @[<stdin>:3:3, playground/src/PreIF.scala:51:24]
  assign PreIF_ar_bits_prot = 3'h0;	// @[<stdin>:3:3, playground/src/PreIF.scala:70:21]
  assign PreIF_aw_valid = 1'h0;	// @[<stdin>:3:3, playground/src/PreIF.scala:29:26]
  assign PreIF_aw_bits_addr = 32'h0;	// @[<stdin>:3:3, playground/src/PreIF.scala:51:24]
  assign PreIF_aw_bits_prot = 3'h0;	// @[<stdin>:3:3, playground/src/PreIF.scala:70:21]
  assign PreIF_w_valid = 1'h0;	// @[<stdin>:3:3, playground/src/PreIF.scala:29:26]
  assign PreIF_w_bits_data = 32'h0;	// @[<stdin>:3:3, playground/src/PreIF.scala:51:24]
  assign PreIF_w_bits_strb = 8'h0;	// @[<stdin>:3:3, playground/src/PreIF.scala:78:20]
  assign PreIF_b_ready = 1'h0;	// @[<stdin>:3:3, playground/src/PreIF.scala:29:26]
endmodule

