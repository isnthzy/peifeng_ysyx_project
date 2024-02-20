// Generated by CIRCT firtool-1.56.0
module Br_cond(	// @[<stdin>:1034:3]
  input  [3:0]  io_br_type,	// @[playground/src/Br_cond.scala:7:12]
  input  [31:0] io_result,	// @[playground/src/Br_cond.scala:7:12]
                io_rdata1,	// @[playground/src/Br_cond.scala:7:12]
                io_rdata2,	// @[playground/src/Br_cond.scala:7:12]
  output        io_taken,	// @[playground/src/Br_cond.scala:7:12]
  output [31:0] io_target	// @[playground/src/Br_cond.scala:7:12]
);

  wire rs1_eq_rs2 = io_rdata1 == io_rdata2;	// @[playground/src/Br_cond.scala:15:32]
  wire rs1_lt_rs2_s = $signed(io_rdata1) < $signed(io_rdata2);	// @[playground/src/Br_cond.scala:16:39]
  wire rs1_lt_rs2_u = io_rdata1 < io_rdata2;	// @[playground/src/Br_cond.scala:17:33]
  assign io_taken =
    io_br_type == 4'h3 & rs1_eq_rs2 | io_br_type == 4'h6 & ~rs1_eq_rs2
    | io_br_type == 4'h2 & rs1_lt_rs2_s | io_br_type == 4'h1 & rs1_lt_rs2_u
    | io_br_type == 4'h5 & ~rs1_lt_rs2_s | io_br_type == 4'h4 & ~rs1_lt_rs2_u;	// @[<stdin>:1034:3, playground/src/Br_cond.scala:15:32, :16:39, :17:33, :19:{27,37}, :20:{27,37,40}, :21:{27,37}, :22:{27,37}, :23:{27,37,40}, :24:{13,27,37,40}]
  assign io_target =
    io_br_type == 4'h6 | io_br_type == 4'h5 | io_br_type == 4'h4 | io_br_type == 4'h3
    | io_br_type == 4'h2 | io_br_type == 4'h1
      ? io_result
      : 32'h0;	// @[<stdin>:1034:3, playground/src/Br_cond.scala:19:27, :20:27, :21:27, :22:27, :23:27, :24:27, :28:39]
endmodule

