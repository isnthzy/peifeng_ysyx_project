// Generated by CIRCT firtool-1.56.0
module Br_j(	// @[<stdin>:952:3]
  input  [3:0]  io_br_type,	// @[playground/src/Br_cond.scala:60:12]
  input  [31:0] io_src1,	// @[playground/src/Br_cond.scala:60:12]
                io_src2,	// @[playground/src/Br_cond.scala:60:12]
  output        io_taken,	// @[playground/src/Br_cond.scala:60:12]
  output [31:0] io_target	// @[playground/src/Br_cond.scala:60:12]
);

  wire [31:0] _result_T = io_src1 + io_src2;	// @[playground/src/Br_cond.scala:71:21]
  assign io_taken = io_br_type == 4'h7 | io_br_type == 4'h8;	// @[<stdin>:952:3, playground/src/Br_cond.scala:68:27, :69:{13,26}]
  assign io_target =
    io_br_type == 4'h8 ? {_result_T[31:1], 1'h0} : io_br_type == 4'h7 ? _result_T : 32'h0;	// @[<stdin>:952:3, playground/src/Br_cond.scala:68:27, :69:26, :71:21, :73:39, :76:{18,25}]
endmodule

