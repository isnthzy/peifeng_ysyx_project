// Generated by CIRCT firtool-1.56.0
module Axi4Lite_Sram_If(	// @[<stdin>:2004:3]
  input         clock,	// @[<stdin>:2005:11]
                reset,	// @[<stdin>:2006:11]
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

  wire [7:0] _io_ar_ready_LSFR_io_OutTime;	// @[playground/src/LSFR.scala:24:20]
  wire       _io_aw_ready_output = 1'h1;	// @[<stdin>:2004:3, playground/src/LSFR.scala:28:11]
  wire       _io_w_ready_output = 1'h1;	// @[<stdin>:2004:3, playground/src/LSFR.scala:28:11]
  reg  [7:0] io_ar_ready_delay;	// @[playground/src/LSFR.scala:21:22]
  reg        io_ar_ready_data;	// @[playground/src/LSFR.scala:22:22]
  wire       _io_ar_ready_output = io_ar_ready_data;	// @[<stdin>:2004:3, playground/src/LSFR.scala:22:22]
  wire       _dpi_sram_io_req_T = _io_ar_ready_output & io_ar_valid;	// @[<stdin>:2004:3, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  wire       _dpi_sram_io_wr_T = _io_aw_ready_output & io_aw_valid;	// @[<stdin>:2004:3, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  wire       _dpi_sram_io_wr_T_1 = _io_w_ready_output & io_w_valid;	// @[<stdin>:2004:3, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  reg        readDataValidReg;	// @[playground/src/SramIF.scala:20:31]
  reg        writeRespValidReg;	// @[playground/src/SramIF.scala:36:32]
  wire       _io_ar_ready_T = io_ar_ready_delay == 8'h0;	// @[playground/src/LSFR.scala:21:22, :27:15]
  always @(posedge clock) begin	// @[<stdin>:2005:11]
    if (reset) begin	// @[<stdin>:2005:11]
      io_ar_ready_delay <= 8'h0;	// @[playground/src/LSFR.scala:21:22]
      io_ar_ready_data <= 1'h0;	// @[playground/src/LSFR.scala:22:{22,35}]
      readDataValidReg <= 1'h0;	// @[playground/src/LSFR.scala:22:35, playground/src/SramIF.scala:20:31]
      writeRespValidReg <= 1'h0;	// @[playground/src/LSFR.scala:22:35, playground/src/SramIF.scala:36:32]
    end
    else begin	// @[<stdin>:2005:11]
      if (_io_ar_ready_T)	// @[playground/src/LSFR.scala:27:15]
        io_ar_ready_delay <= _io_ar_ready_LSFR_io_OutTime;	// @[playground/src/LSFR.scala:21:22, :24:20]
      else	// @[playground/src/LSFR.scala:27:15]
        io_ar_ready_delay <= io_ar_ready_delay - 8'h1;	// @[playground/src/LSFR.scala:21:22, :32:19]
      io_ar_ready_data <= _io_ar_ready_T;	// @[playground/src/LSFR.scala:22:22, :27:15]
      readDataValidReg <= _dpi_sram_io_req_T;	// @[playground/src/SramIF.scala:20:31, src/main/scala/chisel3/util/Decoupled.scala:52:35]
      writeRespValidReg <= _dpi_sram_io_wr_T & _dpi_sram_io_wr_T_1;	// @[playground/src/SramIF.scala:36:32, :37:18, src/main/scala/chisel3/util/Decoupled.scala:52:35]
    end
  end // always @(posedge)
  dpi_sram dpi_sram (	// @[playground/src/SramIF.scala:9:22]
    .clock (clock),
    .addr  (io_ar_bits_addr),
    .wdata (io_w_bits_data),
    .wmask (io_w_bits_strb),
    .req   (_dpi_sram_io_req_T),	// @[src/main/scala/chisel3/util/Decoupled.scala:52:35]
    .wr    (_dpi_sram_io_wr_T & _dpi_sram_io_wr_T_1),	// @[playground/src/SramIF.scala:18:31, src/main/scala/chisel3/util/Decoupled.scala:52:35]
    .rdata (io_r_bits_data)
  );
  LSFR io_ar_ready_LSFR (	// @[playground/src/LSFR.scala:24:20]
    .clock      (clock),
    .reset      (reset),
    .io_Seed    (8'h3),	// @[playground/src/LSFR.scala:26:17]
    .io_OutTime (_io_ar_ready_LSFR_io_OutTime)
  );
  assign io_ar_ready = _io_ar_ready_output;	// @[<stdin>:2004:3]
  assign io_r_valid = readDataValidReg;	// @[<stdin>:2004:3, playground/src/SramIF.scala:20:31]
  assign io_r_bits_resp = 2'h0;	// @[<stdin>:2004:3, playground/src/SramIF.scala:28:17]
  assign io_aw_ready = _io_aw_ready_output;	// @[<stdin>:2004:3]
  assign io_w_ready = _io_w_ready_output;	// @[<stdin>:2004:3]
  assign io_b_valid = writeRespValidReg;	// @[<stdin>:2004:3, playground/src/SramIF.scala:36:32]
  assign io_b_bits_resp = 2'h0;	// @[<stdin>:2004:3, playground/src/SramIF.scala:28:17]
endmodule

