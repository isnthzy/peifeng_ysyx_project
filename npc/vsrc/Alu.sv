// Generated by CIRCT firtool-1.56.0
module Alu(	// @[<stdin>:814:3]
  input  [3:0]  io_op,	// @[playground/src/Alu.scala:5:14]
  input  [31:0] io_src1,	// @[playground/src/Alu.scala:5:14]
                io_src2,	// @[playground/src/Alu.scala:5:14]
  output [31:0] io_result	// @[playground/src/Alu.scala:5:14]
);

  reg  [31:0] casez_tmp;	// @[playground/src/Alu.scala:36:37]
  wire [94:0] _sll_T_1 = {63'h0, io_src1} << io_src2[5:0];	// @[playground/src/Alu.scala:14:{18,28}]
  wire [31:0] _GEN = {27'h0, io_src2[4:0]};	// @[playground/src/Alu.scala:28:{33,43}]
  always_comb begin	// @[playground/src/Alu.scala:36:37]
    casez (io_op)	// @[playground/src/Alu.scala:36:37]
      4'b0000:
        casez_tmp = io_src1 + io_src2;	// @[playground/src/Alu.scala:16:24, :36:37]
      4'b0001:
        casez_tmp = io_src1 - io_src2;	// @[playground/src/Alu.scala:18:24, :36:37]
      4'b0010:
        casez_tmp = io_src1 & io_src2;	// @[playground/src/Alu.scala:20:25, :36:37]
      4'b0011:
        casez_tmp = io_src1 | io_src2;	// @[playground/src/Alu.scala:22:25, :36:37]
      4'b0100:
        casez_tmp = io_src1 ^ io_src2;	// @[playground/src/Alu.scala:24:25, :36:37]
      4'b0101:
        casez_tmp = {31'h0, $signed(io_src1) < $signed(io_src2)};	// @[playground/src/Alu.scala:32:33, :36:37]
      4'b0110:
        casez_tmp = _sll_T_1[31:0];	// @[playground/src/Alu.scala:14:18, :26:20, :36:37]
      4'b0111:
        casez_tmp = {31'h0, io_src1 < io_src2};	// @[playground/src/Alu.scala:34:33, :36:37]
      4'b1000:
        casez_tmp = $signed($signed(io_src1) >>> _GEN);	// @[playground/src/Alu.scala:28:33, :36:37]
      4'b1001:
        casez_tmp = io_src1 >> _GEN;	// @[playground/src/Alu.scala:28:33, :30:33, :36:37]
      4'b1010:
        casez_tmp = 32'h0;	// @[playground/src/Alu.scala:36:37]
      4'b1011:
        casez_tmp = 32'h0;	// @[playground/src/Alu.scala:36:37]
      4'b1100:
        casez_tmp = 32'h0;	// @[playground/src/Alu.scala:36:37]
      4'b1101:
        casez_tmp = 32'h0;	// @[playground/src/Alu.scala:36:37]
      4'b1110:
        casez_tmp = 32'h0;	// @[playground/src/Alu.scala:36:37]
      default:
        casez_tmp = 32'h0;	// @[playground/src/Alu.scala:36:37]
    endcase	// @[playground/src/Alu.scala:36:37]
  end // always_comb
  assign io_result = casez_tmp;	// @[<stdin>:814:3, playground/src/Alu.scala:36:37]
endmodule

