// Generated by CIRCT firtool-1.56.0
module Sram(	// @[<stdin>:1482:3]
  input         clock,	// @[<stdin>:1483:11]
                reset,	// @[<stdin>:1484:11]
                io_in_st_wen,	// @[playground/src/Sram.scala:6:12]
                io_in_ld_wen,	// @[playground/src/Sram.scala:6:12]
  input  [31:0] io_in_addr,	// @[playground/src/Sram.scala:6:12]
  input  [7:0]  io_in_wmask,	// @[playground/src/Sram.scala:6:12]
  input  [31:0] io_in_wdata,	// @[playground/src/Sram.scala:6:12]
  output [31:0] io_out_rdata,	// @[playground/src/Sram.scala:6:12]
  output        io_out_rdata_ok,	// @[playground/src/Sram.scala:6:12]
                io_out_wdata_ok	// @[playground/src/Sram.scala:6:12]
);

  dpi_sram dpi_sram (	// @[playground/src/Sram.scala:12:22]
    .clock    (clock),
    .reset    (reset),
    .ld_wen   (io_in_ld_wen),
    .st_wen   (io_in_st_wen),
    .raddr    (io_in_addr),
    .wmask    (io_in_wmask),
    .waddr    (io_in_addr),
    .wdata    (io_in_wdata),
    .rdata    (io_out_rdata),
    .rdata_ok (io_out_rdata_ok),
    .wdata_ok (io_out_wdata_ok)
  );
endmodule

