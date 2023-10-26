`timescale 1ns / 1ps
module bcd7seg(	// <stdin>:3:3, :40:3
  input  [3:0] seg_in,	// playground/src/SimTop.scala:26:15
  output [6:0] seg_out	// playground/src/SimTop.scala:26:15
);

//   wire [15:0][6:0] _GEN =
//     {7'hE,
//      7'h6,
//      7'h21,
//      7'h46,
//      7'h3,
//      7'h8,
//      7'h10,
//      7'h0,
//      7'h78,
//      7'h2,
//      7'h12,
//      7'h19,
//      7'h30,
//      7'h24,
//      7'h79,
//      7'h40};	// playground/src/SimTop.scala:30:36
//   assign seg_out = _GEN[seg_in];	// <stdin>:3:3, :40:3, playground/src/SimTop.scala:30:36
    assign seg_out=7'h6;
endmodule
