// Generated by CIRCT firtool-1.56.0
module Alu(	// @[<stdin>:57:3]
  input  [11:0] io_op,	// @[playground/src/Alu.scala:5:14]
  input  [31:0] io_src1,	// @[playground/src/Alu.scala:5:14]
                io_src2,	// @[playground/src/Alu.scala:5:14]
  output [31:0] io_result	// @[playground/src/Alu.scala:5:14]
);

  assign io_result =
    io_op == 12'h80
      ? {31'h0, io_src1 == io_src2}
      : io_op == 12'h40
          ? {31'h0, io_src1 < io_src2}
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
                              : io_op == 12'h1 ? io_src1 + io_src2 : 32'h0;	// @[<stdin>:57:3, playground/src/Alu.scala:11:23, :12:23, :13:15, :14:23, :15:23, :16:23, :17:28, :18:27, :19:37]
endmodule
