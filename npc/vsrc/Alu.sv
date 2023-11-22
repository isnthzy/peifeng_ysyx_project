// Generated by CIRCT firtool-1.56.0
module Alu(	// @[<stdin>:57:3]
  input  [11:0] io_op,	// @[playground/src/Alu.scala:5:14]
  input  [31:0] io_src1,	// @[playground/src/Alu.scala:5:14]
                io_src2,	// @[playground/src/Alu.scala:5:14]
  input         io_sign,	// @[playground/src/Alu.scala:5:14]
  output [31:0] io_result	// @[playground/src/Alu.scala:5:14]
);

  wire [94:0] _sll_T_1 = {63'h0, io_src1} << io_src2[5:0];	// @[playground/src/Alu.scala:19:{18,28}]
  wire [31:0] _GEN = {26'h0, io_src2[5:0]};	// @[playground/src/Alu.scala:19:28, :39:32]
  assign io_result =
    io_op == 12'h400
      ? io_src1 >> _GEN
      : io_op == 12'h200
          ? $signed($signed(io_src1) >>> _GEN)
          : io_op == 12'h100
              ? _sll_T_1[31:0]
              : io_op == 12'h80
                  ? {31'h0,
                     io_sign ? $signed(io_src1) < $signed(io_src2) : io_src1 < io_src2}
                  : io_op == 12'h40
                      ? {31'h0, io_src1 == io_src2}
                      : io_op == 12'h20
                          ? io_src1 ^ io_src2
                          : io_op == 12'h10
                              ? io_src1 | io_src2
                              : io_op == 12'h8
                                  ? io_src1 & io_src2
                                  : io_op == 12'h4
                                      ? ~io_src1
                                      : io_op == 12'h2
                                          ? io_src1 - io_src2
                                          : io_op == 12'h1
                                              ? (io_sign
                                                   ? io_src1 + io_src2
                                                   : io_src1 + io_src2)
                                              : 32'h0;	// @[<stdin>:57:3, playground/src/Alu.scala:12:32, :13:25, :15:29, :16:36, :19:18, :21:20, :23:24, :25:17, :27:25, :29:25, :31:25, :33:28, :35:20, :37:20, :39:32, :41:25, :43:37]
endmodule

