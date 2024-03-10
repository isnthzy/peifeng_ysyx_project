// Generated by CIRCT firtool-1.56.0
module Axi4Lite_Sram_Mem(	// @[<stdin>:1905:3]
  input         clock,	// @[<stdin>:1906:11]
                reset,	// @[<stdin>:1907:11]
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

  wire [7:0] _io_w_ready_LSFR_io_OutTime;	// @[playground/src/LSFR.scala:24:20]
  wire [7:0] _io_aw_ready_LSFR_io_OutTime;	// @[playground/src/LSFR.scala:24:20]
  wire [7:0] _io_ar_ready_LSFR_io_OutTime;	// @[playground/src/LSFR.scala:24:20]
  reg  [7:0] io_ar_ready_delay;	// @[playground/src/LSFR.scala:21:22]
  reg        io_ar_ready_data;	// @[playground/src/LSFR.scala:22:22]
  wire       _io_ar_ready_output = io_ar_ready_data;	// @[<stdin>:1905:3, playground/src/LSFR.scala:22:22]
  wire       _dpi_sram_io_req_T = _io_ar_ready_output & io_ar_valid;	// @[<stdin>:1905:3, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  wire       _io_aw_ready_output;	// @[<stdin>:1905:3]
  wire       _dpi_sram_io_wr_T = _io_aw_ready_output & io_aw_valid;	// @[<stdin>:1905:3, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  wire       _io_w_ready_output;	// @[<stdin>:1905:3]
  wire       _dpi_sram_io_wr_T_1 = _io_w_ready_output & io_w_valid;	// @[<stdin>:1905:3, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  reg        readDataValidReg;	// @[playground/src/Sram.scala:18:31]
  reg  [7:0] io_aw_ready_delay;	// @[playground/src/LSFR.scala:21:22]
  reg        io_aw_ready_data;	// @[playground/src/LSFR.scala:22:22]
  assign _io_aw_ready_output = io_aw_ready_data;	// @[<stdin>:1905:3, playground/src/LSFR.scala:22:22]
  reg  [7:0] io_w_ready_delay;	// @[playground/src/LSFR.scala:21:22]
  reg        io_w_ready_data;	// @[playground/src/LSFR.scala:22:22]
  assign _io_w_ready_output = io_w_ready_data;	// @[<stdin>:1905:3, playground/src/LSFR.scala:22:22]
  reg        writeRespValidReg;	// @[playground/src/Sram.scala:37:32]
  wire       _io_ar_ready_T = io_ar_ready_delay == 8'h0;	// @[playground/src/LSFR.scala:21:22, :27:15]
  wire       _io_aw_ready_T = io_aw_ready_delay == 8'h0;	// @[playground/src/LSFR.scala:21:22, :27:15]
  wire       _io_w_ready_T = io_w_ready_delay == 8'h0;	// @[playground/src/LSFR.scala:21:22, :27:15]
  always @(posedge clock) begin	// @[<stdin>:1906:11]
    if (reset) begin	// @[<stdin>:1906:11]
      io_ar_ready_delay <= 8'h0;	// @[playground/src/LSFR.scala:21:22]
      io_ar_ready_data <= 1'h0;	// @[playground/src/LSFR.scala:22:{22,35}]
      readDataValidReg <= 1'h0;	// @[playground/src/LSFR.scala:22:35, playground/src/Sram.scala:18:31]
      io_aw_ready_delay <= 8'h0;	// @[playground/src/LSFR.scala:21:22]
      io_aw_ready_data <= 1'h0;	// @[playground/src/LSFR.scala:22:{22,35}]
      io_w_ready_delay <= 8'h0;	// @[playground/src/LSFR.scala:21:22]
      io_w_ready_data <= 1'h0;	// @[playground/src/LSFR.scala:22:{22,35}]
      writeRespValidReg <= 1'h0;	// @[playground/src/LSFR.scala:22:35, playground/src/Sram.scala:37:32]
    end
    else begin	// @[<stdin>:1906:11]
      if (_io_ar_ready_T)	// @[playground/src/LSFR.scala:27:15]
        io_ar_ready_delay <= _io_ar_ready_LSFR_io_OutTime;	// @[playground/src/LSFR.scala:21:22, :24:20]
      else	// @[playground/src/LSFR.scala:27:15]
        io_ar_ready_delay <= io_ar_ready_delay - 8'h1;	// @[playground/src/LSFR.scala:21:22, :32:19]
      io_ar_ready_data <= _io_ar_ready_T;	// @[playground/src/LSFR.scala:22:22, :27:15]
      readDataValidReg <= _dpi_sram_io_req_T;	// @[playground/src/Sram.scala:18:31, src/main/scala/chisel3/util/Decoupled.scala:52:35]
      if (_io_aw_ready_T)	// @[playground/src/LSFR.scala:27:15]
        io_aw_ready_delay <= _io_aw_ready_LSFR_io_OutTime;	// @[playground/src/LSFR.scala:21:22, :24:20]
      else	// @[playground/src/LSFR.scala:27:15]
        io_aw_ready_delay <= io_aw_ready_delay - 8'h1;	// @[playground/src/LSFR.scala:21:22, :32:19]
      io_aw_ready_data <= _io_aw_ready_T;	// @[playground/src/LSFR.scala:22:22, :27:15]
      if (_io_w_ready_T)	// @[playground/src/LSFR.scala:27:15]
        io_w_ready_delay <= _io_w_ready_LSFR_io_OutTime;	// @[playground/src/LSFR.scala:21:22, :24:20]
      else	// @[playground/src/LSFR.scala:27:15]
        io_w_ready_delay <= io_w_ready_delay - 8'h1;	// @[playground/src/LSFR.scala:21:22, :32:19]
      io_w_ready_data <= _io_w_ready_T;	// @[playground/src/LSFR.scala:22:22, :27:15]
      writeRespValidReg <= _dpi_sram_io_wr_T & _dpi_sram_io_wr_T_1;	// @[playground/src/Sram.scala:37:32, :38:18, src/main/scala/chisel3/util/Decoupled.scala:52:35]
    end
  end // always @(posedge)
  dpi_sram dpi_sram (	// @[playground/src/Sram.scala:7:22]
    .clock (clock),
    .addr
      (_dpi_sram_io_req_T
         ? io_ar_bits_addr
         : _dpi_sram_io_wr_T & _dpi_sram_io_wr_T_1 ? io_aw_bits_addr : 32'h0),	// @[playground/src/Sram.scala:13:24, :14:{26,39}, src/main/scala/chisel3/util/Decoupled.scala:52:35]
    .wdata (io_w_bits_data),
    .wmask (io_w_bits_strb),
    .req   (_dpi_sram_io_req_T | _dpi_sram_io_wr_T & _dpi_sram_io_wr_T_1),	// @[playground/src/Sram.scala:15:{31,46}, src/main/scala/chisel3/util/Decoupled.scala:52:35]
    .wr    (_dpi_sram_io_wr_T & _dpi_sram_io_wr_T_1),	// @[playground/src/Sram.scala:16:31, src/main/scala/chisel3/util/Decoupled.scala:52:35]
    .rdata (io_r_bits_data)
  );
  LSFR io_ar_ready_LSFR (	// @[playground/src/LSFR.scala:24:20]
    .clock      (clock),
    .reset      (reset),
    .io_Seed    (8'hF),	// @[playground/src/LSFR.scala:26:17]
    .io_OutTime (_io_ar_ready_LSFR_io_OutTime)
  );
  LSFR io_aw_ready_LSFR (	// @[playground/src/LSFR.scala:24:20]
    .clock      (clock),
    .reset      (reset),
    .io_Seed    (8'h1),	// @[playground/src/LSFR.scala:26:17]
    .io_OutTime (_io_aw_ready_LSFR_io_OutTime)
  );
  LSFR io_w_ready_LSFR (	// @[playground/src/LSFR.scala:24:20]
    .clock      (clock),
    .reset      (reset),
    .io_Seed    (8'h14),	// @[playground/src/LSFR.scala:26:17]
    .io_OutTime (_io_w_ready_LSFR_io_OutTime)
  );
  assign io_ar_ready = _io_ar_ready_output;	// @[<stdin>:1905:3]
  assign io_r_valid = readDataValidReg;	// @[<stdin>:1905:3, playground/src/Sram.scala:18:31]
  assign io_r_bits_resp = 2'h0;	// @[<stdin>:1905:3, playground/src/Sram.scala:26:17]
  assign io_aw_ready = _io_aw_ready_output;	// @[<stdin>:1905:3]
  assign io_w_ready = _io_w_ready_output;	// @[<stdin>:1905:3]
  assign io_b_valid = writeRespValidReg;	// @[<stdin>:1905:3, playground/src/Sram.scala:37:32]
  assign io_b_bits_resp = 2'h0;	// @[<stdin>:1905:3, playground/src/Sram.scala:26:17]
endmodule

