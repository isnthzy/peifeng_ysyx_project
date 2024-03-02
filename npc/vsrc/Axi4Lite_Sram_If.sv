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

module Axi4Lite_Sram_If(	// @[<stdin>:1667:3]
  input         clock,	// @[<stdin>:1668:11]
                reset,	// @[<stdin>:1669:11]
                io_ar_valid,	// @[playground/src/SramIF.scala:8:12]
  input  [31:0] io_ar_bits_addr,	// @[playground/src/SramIF.scala:8:12]
  input  [2:0]  io_ar_bits_prot,	// @[playground/src/SramIF.scala:8:12]
  input         io_r_ready,	// @[playground/src/SramIF.scala:8:12]
                io_aw_valid,	// @[playground/src/SramIF.scala:8:12]
  input  [31:0] io_aw_bits_addr,	// @[playground/src/SramIF.scala:8:12]
  input  [2:0]  io_aw_bits_prot,	// @[playground/src/SramIF.scala:8:12]
  input         io_w_valid,	// @[playground/src/SramIF.scala:8:12]
  input  [31:0] io_w_bits_data,	// @[playground/src/SramIF.scala:8:12]
  input  [7:0]  io_w_bits_strb,	// @[playground/src/SramIF.scala:8:12]
  input         io_b_ready,	// @[playground/src/SramIF.scala:8:12]
  output        io_ar_ready,	// @[playground/src/SramIF.scala:8:12]
                io_r_valid,	// @[playground/src/SramIF.scala:8:12]
  output [31:0] io_r_bits_data,	// @[playground/src/SramIF.scala:8:12]
  output [1:0]  io_r_bits_resp,	// @[playground/src/SramIF.scala:8:12]
  output        io_aw_ready,	// @[playground/src/SramIF.scala:8:12]
                io_w_ready,	// @[playground/src/SramIF.scala:8:12]
                io_b_valid,	// @[playground/src/SramIF.scala:8:12]
  output [1:0]  io_b_bits_resp	// @[playground/src/SramIF.scala:8:12]
);

  wire _io_ar_ready_output = 1'h1;	// @[<stdin>:1667:3, playground/src/SramIF.scala:13:14]
  wire _io_aw_ready_output = 1'h1;	// @[<stdin>:1667:3, playground/src/SramIF.scala:13:14]
  wire _io_w_ready_output = 1'h1;	// @[<stdin>:1667:3, playground/src/SramIF.scala:13:14]
  wire _dpi_sram_io_req_T = _io_ar_ready_output & io_ar_valid;	// @[<stdin>:1667:3, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  wire _dpi_sram_io_wr_T = _io_aw_ready_output & io_aw_valid;	// @[<stdin>:1667:3, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  wire _dpi_sram_io_wr_T_1 = _io_w_ready_output & io_w_valid;	// @[<stdin>:1667:3, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  reg  readDataValidReg;	// @[playground/src/SramIF.scala:19:31]
  reg  writeRespValidReg;	// @[playground/src/SramIF.scala:35:32]
  always @(posedge clock) begin	// @[<stdin>:1668:11]
    if (reset) begin	// @[<stdin>:1668:11]
      readDataValidReg <= 1'h0;	// @[playground/src/SramIF.scala:19:31]
      writeRespValidReg <= 1'h0;	// @[playground/src/SramIF.scala:19:31, :35:32]
    end
    else begin	// @[<stdin>:1668:11]
      readDataValidReg <= _dpi_sram_io_req_T;	// @[playground/src/SramIF.scala:19:31, src/main/scala/chisel3/util/Decoupled.scala:52:35]
      writeRespValidReg <= _dpi_sram_io_wr_T & _dpi_sram_io_wr_T_1;	// @[playground/src/SramIF.scala:35:32, :36:18, src/main/scala/chisel3/util/Decoupled.scala:52:35]
    end
  end // always @(posedge)
  dpi_sram dpi_sram (	// @[playground/src/SramIF.scala:9:22]
    .clock (clock),
    .addr  (io_ar_bits_addr),
    .wdata (io_w_bits_data),
    .wmask (io_w_bits_strb),
    .req   (_dpi_sram_io_req_T),	// @[src/main/scala/chisel3/util/Decoupled.scala:52:35]
    .wr    (_dpi_sram_io_wr_T & _dpi_sram_io_wr_T_1),	// @[playground/src/SramIF.scala:17:31, src/main/scala/chisel3/util/Decoupled.scala:52:35]
    .rdata (io_r_bits_data)
  );
  assign io_ar_ready = _io_ar_ready_output;	// @[<stdin>:1667:3]
  assign io_r_valid = readDataValidReg;	// @[<stdin>:1667:3, playground/src/SramIF.scala:19:31]
  assign io_r_bits_resp = 2'h0;	// @[<stdin>:1667:3, playground/src/SramIF.scala:27:17]
  assign io_aw_ready = _io_aw_ready_output;	// @[<stdin>:1667:3]
  assign io_w_ready = _io_w_ready_output;	// @[<stdin>:1667:3]
  assign io_b_valid = writeRespValidReg;	// @[<stdin>:1667:3, playground/src/SramIF.scala:35:32]
  assign io_b_bits_resp = 2'h0;	// @[<stdin>:1667:3, playground/src/SramIF.scala:27:17]
endmodule

