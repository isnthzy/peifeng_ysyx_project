// Generated by CIRCT firtool-1.56.0
module SimTop(	// <stdin>:3:3
  input        clock,	// <stdin>:4:11
               reset,	// <stdin>:5:11
  input  [2:0] io_op,	// playground/src/SimTop.scala:5:14
  input  [3:0] io_a,	// playground/src/SimTop.scala:5:14
               io_b,	// playground/src/SimTop.scala:5:14
  output [3:0] io_out,	// playground/src/SimTop.scala:5:14
  output       io_of	// playground/src/SimTop.scala:5:14
);

  wire [2:0]      _sum_T = io_a[2:0] + io_b[2:0];	// playground/src/SimTop.scala:5:14, :12:18
  wire            _io_of_T_2 = io_op == 3'h1;	// playground/src/SimTop.scala:26:34
  wire [2:0]      _io_out_T_3 = _io_of_T_2 ? io_a[2:0] - io_b[2:0] : _sum_T;	// playground/src/SimTop.scala:5:14, :12:18, :16:18, :26:34
  wire [3:0]      _GEN = {_io_out_T_3[2], _io_out_T_3};	// playground/src/SimTop.scala:26:34
  wire [7:0][3:0] _GEN_0 =
    {{{3'h0, io_a == io_b}},
     {{3'h0, $signed(io_a) < $signed(io_b)}},
     {io_a ^ io_b},
     {io_a | io_b},
     {io_a & io_b},
     {~io_a},
     {_GEN},
     {_GEN}};	// playground/src/SimTop.scala:20:16, :21:21, :22:20, :23:21, :24:25, :25:24, :26:34, :32:33
  assign io_out = _GEN_0[io_op];	// <stdin>:3:3, playground/src/SimTop.scala:26:34
  assign io_of =
    _io_of_T_2
      ? io_a[3] != io_b[3] & io_a[3] < io_b[3]
      : io_op == 3'h0 & io_a[3] == io_b[3] & _sum_T[2] != io_a[3];	// <stdin>:3:3, playground/src/SimTop.scala:12:18, :13:39, :14:{27,31,39,54,58}, :18:{31,44,56}, :26:34, :32:33
endmodule

