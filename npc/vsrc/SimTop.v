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

module SimTop(	// <stdin>:87:3
  input         clock,	// <stdin>:88:11
                reset,	// <stdin>:89:11
  input  [31:0] io_inst,	// playground/src/SimTop.scala:6:14
  output [31:0] io_pc	// playground/src/SimTop.scala:6:14
);

  reg [31:0] pc;	// playground/src/SimTop.scala:12:17
  always @(posedge clock) begin	// <stdin>:88:11
    if (reset)	// <stdin>:88:11
      pc <= 32'h80000000;	// playground/src/SimTop.scala:12:17
    else	// <stdin>:88:11
      pc <= pc + 32'h4;	// playground/src/SimTop.scala:12:17, :13:9
  end // always @(posedge)
  `ifdef ENABLE_INITIAL_REG_	// <stdin>:87:3
    `ifdef FIRRTL_BEFORE_INITIAL	// <stdin>:87:3
      `FIRRTL_BEFORE_INITIAL	// <stdin>:87:3
    `endif // FIRRTL_BEFORE_INITIAL
    initial begin	// <stdin>:87:3
      automatic logic [31:0] _RANDOM[0:0];	// <stdin>:87:3
      `ifdef INIT_RANDOM_PROLOG_	// <stdin>:87:3
        `INIT_RANDOM_PROLOG_	// <stdin>:87:3
      `endif // INIT_RANDOM_PROLOG_
      `ifdef RANDOMIZE_REG_INIT	// <stdin>:87:3
        _RANDOM[/*Zero width*/ 1'b0] = `RANDOM;	// <stdin>:87:3
        pc = _RANDOM[/*Zero width*/ 1'b0];	// <stdin>:87:3, playground/src/SimTop.scala:12:17
      `endif // RANDOMIZE_REG_INIT
    end // initial
    `ifdef FIRRTL_AFTER_INITIAL	// <stdin>:87:3
      `FIRRTL_AFTER_INITIAL	// <stdin>:87:3
    `endif // FIRRTL_AFTER_INITIAL
  `endif // ENABLE_INITIAL_REG_
  assign io_pc = pc;	// <stdin>:87:3, playground/src/SimTop.scala:12:17
endmodule

