// Generated by CIRCT firtool-1.56.0
module Alu(	// @[<stdin>:494:3]
  input  [11:0] io_op,	// @[playground/src/Alu.scala:5:14]
  input  [31:0] io_src1,	// @[playground/src/Alu.scala:5:14]
                io_src2,	// @[playground/src/Alu.scala:5:14]
  input         io_sign,	// @[playground/src/Alu.scala:5:14]
  output [31:0] io_result	// @[playground/src/Alu.scala:5:14]
);

  wire [94:0] _sll_T_1 = {63'h0, io_src1} << io_src2[5:0];	// @[playground/src/Alu.scala:18:{18,28}]
  wire [31:0] _GEN = {26'h0, io_src2[5:0]};	// @[playground/src/Alu.scala:18:28, :38:32]
  assign io_result =
    io_op == 12'h200
      ? io_src1 >> _GEN
      : io_op == 12'h100
          ? $signed($signed(io_src1) >>> _GEN)
          : io_op == 12'h80
              ? _sll_T_1[31:0]
              : io_op == 12'h40
                  ? {31'h0,
                     io_sign ? $signed(io_src1) < $signed(io_src2) : io_src1 < io_src2}
                  : io_op == 12'h20
                      ? {31'h0, io_src1 == io_src2}
                      : io_op == 12'h10
                          ? io_src1 ^ io_src2
                          : io_op == 12'h8
                              ? io_src1 | io_src2
                              : io_op == 12'h4
                                  ? io_src1 & io_src2
                                  : io_op == 12'h2
                                      ? io_src1 - io_src2
                                      : io_op == 12'h1 ? io_src1 + io_src2 : 32'h0;	// @[<stdin>:494:3, playground/src/Alu.scala:14:29, :15:36, :18:18, :20:24, :22:24, :26:25, :28:25, :30:25, :32:28, :34:20, :36:20, :38:32, :40:25, :42:37]
endmodule

