// Generated by CIRCT firtool-1.56.0
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
  output        PreIF_to_if_valid,	// @[playground/src/PreIF.scala:7:15]
  output [31:0] PreIF_to_if_bits_nextpc,	// @[playground/src/PreIF.scala:7:15]
                PreIF_to_if_bits_pc,	// @[playground/src/PreIF.scala:7:15]
  output        PreIF_ar_valid,	// @[playground/src/PreIF.scala:7:15]
  output [31:0] PreIF_ar_bits_addr	// @[playground/src/PreIF.scala:7:15]
);

  reg         arvalidReg;	// @[playground/src/PreIF.scala:47:25]
  wire        fetch_wen = PreIF_to_if_ready;	// @[playground/src/PreIF.scala:20:31]
  wire        PreIF_flush = PreIF_for_ex_flush | PreIF_for_id_flush;	// @[playground/src/PreIF.scala:23:33, :24:35]
  wire        PreIF_ready_go = fetch_wen & PreIF_ar_ready & arvalidReg;	// @[playground/src/PreIF.scala:18:36, :20:31, :25:30, :47:25]
  wire        _PreIF_to_if_valid_output = ~PreIF_flush & ~reset & PreIF_ready_go;	// @[playground/src/PreIF.scala:18:36, :23:33, :26:{26,48}]
  reg  [31:0] PreIF_pc;	// @[playground/src/PreIF.scala:33:29]
  wire [31:0] PreIF_snpc = PreIF_pc + 32'h4;	// @[playground/src/PreIF.scala:33:29, :34:36, :38:26]
  wire [31:0] PreIF_dnpc =
    PreIF_for_ex_epc_taken
      ? PreIF_for_ex_epc_target
      : PreIF_for_id_Br_J_taken ? PreIF_for_id_Br_J_target : PreIF_for_ex_Br_B_target;	// @[playground/src/PreIF.scala:30:17, :35:36, :39:20]
  wire [31:0] PreIF_nextpc =
    PreIF_for_id_Br_J_taken | PreIF_for_ex_Br_B_taken | PreIF_for_ex_epc_taken
      ? PreIF_dnpc
      : PreIF_snpc;	// @[playground/src/PreIF.scala:34:36, :35:36, :36:36, :40:{21,31}]
  reg  [31:0] araddrReg;	// @[playground/src/PreIF.scala:48:24]
  reg         ReadRequstState;	// @[playground/src/PreIF.scala:51:30]
  wire        _GEN = ReadRequstState & PreIF_ar_ready;	// @[playground/src/PreIF.scala:51:30, :58:45, :59:25, :60:22]
  always @(posedge clock) begin	// @[<stdin>:4:11]
    if (reset) begin	// @[<stdin>:4:11]
      PreIF_pc <= 32'h80000000;	// @[playground/src/PreIF.scala:33:29]
      arvalidReg <= 1'h0;	// @[playground/src/PreIF.scala:26:26, :47:25]
      araddrReg <= 32'h0;	// @[playground/src/PreIF.scala:48:24]
      ReadRequstState <= 1'h0;	// @[playground/src/PreIF.scala:26:26, :51:30]
    end
    else begin	// @[<stdin>:4:11]
      if (PreIF_to_if_ready & _PreIF_to_if_valid_output)	// @[playground/src/PreIF.scala:26:26, src/main/scala/chisel3/util/Decoupled.scala:52:35]
        PreIF_pc <= PreIF_nextpc;	// @[playground/src/PreIF.scala:33:29, :36:36]
      if (ReadRequstState) begin	// @[playground/src/PreIF.scala:51:30]
        arvalidReg <= ~_GEN & arvalidReg;	// @[playground/src/PreIF.scala:47:25, :51:30, :58:45, :59:25, :60:22, :61:17]
        ReadRequstState <= ~_GEN & ReadRequstState;	// @[playground/src/PreIF.scala:51:30, :58:45, :59:25, :60:22]
      end
      else begin	// @[playground/src/PreIF.scala:51:30]
        arvalidReg <= fetch_wen | arvalidReg;	// @[playground/src/PreIF.scala:20:31, :47:25, :53:20, :56:17]
        ReadRequstState <= fetch_wen | ReadRequstState;	// @[playground/src/PreIF.scala:20:31, :51:30, :53:20, :54:22]
      end
      if (~ReadRequstState & fetch_wen)	// @[playground/src/PreIF.scala:20:31, :48:24, :51:30, :52:{23,33}, :53:20, :55:16]
        araddrReg <= PreIF_nextpc;	// @[playground/src/PreIF.scala:36:36, :48:24]
    end
  end // always @(posedge)
  assign PreIF_to_if_valid = _PreIF_to_if_valid_output;	// @[<stdin>:3:3, playground/src/PreIF.scala:26:26]
  assign PreIF_to_if_bits_nextpc = PreIF_nextpc;	// @[<stdin>:3:3, playground/src/PreIF.scala:36:36]
  assign PreIF_to_if_bits_pc = PreIF_pc;	// @[<stdin>:3:3, playground/src/PreIF.scala:33:29]
  assign PreIF_ar_valid = arvalidReg;	// @[<stdin>:3:3, playground/src/PreIF.scala:47:25]
  assign PreIF_ar_bits_addr = araddrReg;	// @[<stdin>:3:3, playground/src/PreIF.scala:48:24]
endmodule

