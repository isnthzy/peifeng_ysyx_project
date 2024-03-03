// Generated by CIRCT firtool-1.56.0
module Axi4Lite_Sram_Mem(	// @[<stdin>:1613:3]
  input         clock,	// @[<stdin>:1614:11]
                reset,	// @[<stdin>:1615:11]
                io_ar_valid,	// @[playground/src/Sram.scala:6:12]
  input  [31:0] io_ar_bits_addr,	// @[playground/src/Sram.scala:6:12]
  input  [2:0]  io_ar_bits_prot,	// @[playground/src/Sram.scala:6:12]
  input         io_r_ready,	// @[playground/src/Sram.scala:6:12]
                io_aw_valid,	// @[playground/src/Sram.scala:6:12]
  input  [31:0] io_aw_bits_addr,	// @[playground/src/Sram.scala:6:12]
  input  [2:0]  io_aw_bits_prot,	// @[playground/src/Sram.scala:6:12]
  input         io_w_valid,	// @[playground/src/Sram.scala:6:12]
  input  [31:0] io_w_bits_data,	// @[playground/src/Sram.scala:6:12]
  input  [7:0]  io_w_bits_strb,	// @[playground/src/Sram.scala:6:12]
  input         io_b_ready,	// @[playground/src/Sram.scala:6:12]
  output        io_ar_ready,	// @[playground/src/Sram.scala:6:12]
                io_r_valid,	// @[playground/src/Sram.scala:6:12]
  output [31:0] io_r_bits_data,	// @[playground/src/Sram.scala:6:12]
  output [1:0]  io_r_bits_resp,	// @[playground/src/Sram.scala:6:12]
  output        io_aw_ready,	// @[playground/src/Sram.scala:6:12]
                io_w_ready,	// @[playground/src/Sram.scala:6:12]
                io_b_valid,	// @[playground/src/Sram.scala:6:12]
  output [1:0]  io_b_bits_resp	// @[playground/src/Sram.scala:6:12]
);

  wire _io_ar_ready_output = 1'h1;	// @[<stdin>:1613:3, playground/src/Sram.scala:11:14]
  wire _io_aw_ready_output = 1'h1;	// @[<stdin>:1613:3, playground/src/Sram.scala:11:14]
  wire _io_w_ready_output = 1'h1;	// @[<stdin>:1613:3, playground/src/Sram.scala:11:14]
  wire _dpi_sram_io_req_T = _io_ar_ready_output & io_ar_valid;	// @[<stdin>:1613:3, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  wire _dpi_sram_io_wr_T = _io_aw_ready_output & io_aw_valid;	// @[<stdin>:1613:3, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  wire _dpi_sram_io_wr_T_1 = _io_w_ready_output & io_w_valid;	// @[<stdin>:1613:3, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  reg  readDataValidReg;	// @[playground/src/Sram.scala:17:31]
  reg  writeRespValidReg;	// @[playground/src/Sram.scala:35:32]
  always @(posedge clock) begin	// @[<stdin>:1614:11]
    if (reset) begin	// @[<stdin>:1614:11]
      readDataValidReg <= 1'h0;	// @[playground/src/Sram.scala:17:31]
      writeRespValidReg <= 1'h0;	// @[playground/src/Sram.scala:17:31, :35:32]
    end
    else begin	// @[<stdin>:1614:11]
      readDataValidReg <= _dpi_sram_io_req_T;	// @[playground/src/Sram.scala:17:31, src/main/scala/chisel3/util/Decoupled.scala:52:35]
      writeRespValidReg <= _dpi_sram_io_wr_T & _dpi_sram_io_wr_T_1;	// @[playground/src/Sram.scala:35:32, :36:18, src/main/scala/chisel3/util/Decoupled.scala:52:35]
    end
  end // always @(posedge)
  dpi_sram dpi_sram (	// @[playground/src/Sram.scala:7:22]
    .clock (clock),
    .addr  (io_ar_bits_addr),
    .wdata (io_w_bits_data),
    .wmask (io_w_bits_strb),
    .req   (_dpi_sram_io_req_T | _dpi_sram_io_wr_T & _dpi_sram_io_wr_T_1),	// @[playground/src/Sram.scala:14:{31,46}, src/main/scala/chisel3/util/Decoupled.scala:52:35]
    .wr    (_dpi_sram_io_wr_T & _dpi_sram_io_wr_T_1),	// @[playground/src/Sram.scala:15:31, src/main/scala/chisel3/util/Decoupled.scala:52:35]
    .rdata (io_r_bits_data)
  );
  assign io_ar_ready = _io_ar_ready_output;	// @[<stdin>:1613:3]
  assign io_r_valid = readDataValidReg;	// @[<stdin>:1613:3, playground/src/Sram.scala:17:31]
  assign io_r_bits_resp = 2'h0;	// @[<stdin>:1613:3, playground/src/Sram.scala:25:17]
  assign io_aw_ready = _io_aw_ready_output;	// @[<stdin>:1613:3]
  assign io_w_ready = _io_w_ready_output;	// @[<stdin>:1613:3]
  assign io_b_valid = writeRespValidReg;	// @[<stdin>:1613:3, playground/src/Sram.scala:35:32]
  assign io_b_bits_resp = 2'h0;	// @[<stdin>:1613:3, playground/src/Sram.scala:25:17]
endmodule

