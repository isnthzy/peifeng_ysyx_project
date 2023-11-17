// Generated by CIRCT firtool-1.56.0
// Standard header to adapt well known macros to our needs.
`ifndef RANDOMIZE
  `ifdef RANDOMIZE_REG_INIT
    `define RANDOMIZE
  `endif // RANDOMIZE_REG_INIT
`endif // not def RANDOMIZE

// RANDOM may be set to an expression that produces a 32-bit random unsigned value.
`ifndef RANDOM
  `define RANDOM $random
`endif // not def RANDOM

// Users can define INIT_RANDOM as general code that gets injected into the
// initializer block for modules with registers.
`ifndef INIT_RANDOM
  `define INIT_RANDOM
`endif // not def INIT_RANDOM

// If using random initialization, you can also define RANDOMIZE_DELAY to
// customize the delay used, otherwise 0.002 is used.
`ifndef RANDOMIZE_DELAY
  `define RANDOMIZE_DELAY 0.002
`endif // not def RANDOMIZE_DELAY

// Define INIT_RANDOM_PROLOG_ for use in our modules below.
`ifndef INIT_RANDOM_PROLOG_
  `ifdef RANDOMIZE
    `ifdef VERILATOR
      `define INIT_RANDOM_PROLOG_ `INIT_RANDOM
    `else  // VERILATOR
      `define INIT_RANDOM_PROLOG_ `INIT_RANDOM #`RANDOMIZE_DELAY begin end
    `endif // VERILATOR
  `else  // RANDOMIZE
    `define INIT_RANDOM_PROLOG_
  `endif // RANDOMIZE
`endif // not def INIT_RANDOM_PROLOG_

// Include register initializers in init blocks unless synthesis is set
`ifndef SYNTHESIS
  `ifndef ENABLE_INITIAL_REG_
    `define ENABLE_INITIAL_REG_
  `endif // not def ENABLE_INITIAL_REG_
`endif // not def SYNTHESIS

// Include rmemory initializers in init blocks unless synthesis is set
`ifndef SYNTHESIS
  `ifndef ENABLE_INITIAL_MEM_
    `define ENABLE_INITIAL_MEM_
  `endif // not def ENABLE_INITIAL_MEM_
`endif // not def SYNTHESIS

module Reg(	// <stdin>:3:3
  input  clock,	// <stdin>:4:11
         reset,	// <stdin>:5:11
         reg_din,	// playground/src/SimTop.scala:16:13
         reg_wen,	// playground/src/SimTop.scala:16:13
  output reg_dout	// playground/src/SimTop.scala:16:13
);

  reg reg_dout_0;	// playground/src/SimTop.scala:21:23
  always @(posedge clock) begin	// <stdin>:4:11
    if (reset)	// <stdin>:4:11
      reg_dout_0 <= 1'h1;	// <stdin>:3:3, playground/src/SimTop.scala:21:23
    else if (reg_wen)	// playground/src/SimTop.scala:16:13
      reg_dout_0 <= reg_din;	// playground/src/SimTop.scala:21:23
  end // always @(posedge)
  `ifdef ENABLE_INITIAL_REG_	// <stdin>:3:3
    `ifdef FIRRTL_BEFORE_INITIAL	// <stdin>:3:3
      `FIRRTL_BEFORE_INITIAL	// <stdin>:3:3
    `endif // FIRRTL_BEFORE_INITIAL
    initial begin	// <stdin>:3:3
      automatic logic [31:0] _RANDOM[0:0];	// <stdin>:3:3
      `ifdef INIT_RANDOM_PROLOG_	// <stdin>:3:3
        `INIT_RANDOM_PROLOG_	// <stdin>:3:3
      `endif // INIT_RANDOM_PROLOG_
      `ifdef RANDOMIZE_REG_INIT	// <stdin>:3:3
        _RANDOM[/*Zero width*/ 1'b0] = `RANDOM;	// <stdin>:3:3
        reg_dout_0 = _RANDOM[/*Zero width*/ 1'b0][0];	// <stdin>:3:3, playground/src/SimTop.scala:21:23
      `endif // RANDOMIZE_REG_INIT
    end // initial
    `ifdef FIRRTL_AFTER_INITIAL	// <stdin>:3:3
      `FIRRTL_AFTER_INITIAL	// <stdin>:3:3
    `endif // FIRRTL_AFTER_INITIAL
  `endif // ENABLE_INITIAL_REG_
  assign reg_dout = reg_dout_0;	// <stdin>:3:3, playground/src/SimTop.scala:21:23
endmodule

module SimTop(	// <stdin>:14:3
  input        clock,	// <stdin>:15:11
               reset,	// <stdin>:16:11
  input  [3:0] io_in,	// playground/src/SimTop.scala:5:14
  output [3:0] io_out	// playground/src/SimTop.scala:5:14
);

  wire _i1_reg_dout;	// playground/src/SimTop.scala:9:16
  Reg i1 (	// playground/src/SimTop.scala:9:16
    .clock    (clock),
    .reset    (reset),
    .reg_din  (io_in[0]),	// playground/src/SimTop.scala:10:14
    .reg_wen  (_i1_reg_dout),	// playground/src/SimTop.scala:9:16
    .reg_dout (_i1_reg_dout)
  );
  assign io_out = {3'h0, _i1_reg_dout};	// <stdin>:14:3, playground/src/SimTop.scala:9:16, :11:10
endmodule

