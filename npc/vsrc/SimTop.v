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

module bcd7seg(	// <stdin>:3:3, :30:3
  input  [3:0] seg_in,	// playground/src/SimTop.scala:45:15
  output [6:0] seg_out	// playground/src/SimTop.scala:45:15
);

  assign seg_out =
    seg_in == 4'h9
      ? 7'h10
      : seg_in == 4'h8
          ? 7'h0
          : seg_in == 4'h7
              ? 7'h78
              : seg_in == 4'h6
                  ? 7'h2
                  : seg_in == 4'h5
                      ? 7'h12
                      : seg_in == 4'h4
                          ? 7'h19
                          : seg_in == 4'h3
                              ? 7'h30
                              : seg_in == 4'h2
                                  ? 7'h24
                                  : seg_in == 4'h1 ? 7'h79 : {seg_in == 4'h0, 6'h0};	// <stdin>:3:3, :30:3, playground/src/SimTop.scala:49:36
endmodule

module SimTop(	// <stdin>:57:3
  input        clock,	// <stdin>:58:11
               reset,	// <stdin>:59:11
               io_TimeOut,	// playground/src/SimTop.scala:5:14
               io_Begin,	// playground/src/SimTop.scala:5:14
               io_Zero,	// playground/src/SimTop.scala:5:14
  output [6:0] io_Hex1,	// playground/src/SimTop.scala:5:14
               io_Hex2	// playground/src/SimTop.scala:5:14
);

  reg [24:0] clkcount;	// playground/src/SimTop.scala:14:24
  reg [6:0]  clk1scount;	// playground/src/SimTop.scala:15:26
  reg [3:0]  clk10scount;	// playground/src/SimTop.scala:16:27
  always @(posedge clock) begin	// <stdin>:58:11
    if (reset) begin	// <stdin>:58:11
      clkcount <= 25'h0;	// playground/src/SimTop.scala:14:24
      clk1scount <= 7'h0;	// playground/src/SimTop.scala:15:26
      clk10scount <= 4'h0;	// playground/src/SimTop.scala:16:27
    end
    else begin	// <stdin>:58:11
      automatic logic _GEN;	// playground/src/SimTop.scala:23:16
      _GEN = clkcount == 25'h17D783F;	// playground/src/SimTop.scala:14:24, :23:16
      if (_GEN | io_Zero)	// playground/src/SimTop.scala:17:12, :18:25, :19:14, :23:{16,36}, :24:14
        clkcount <= 25'h0;	// playground/src/SimTop.scala:14:24
      else	// playground/src/SimTop.scala:17:12, :18:25, :19:14, :23:36, :24:14
        clkcount <= clkcount + 25'h1;	// playground/src/SimTop.scala:14:24, :17:24
      if (clk1scount == 7'h64)	// playground/src/SimTop.scala:15:26, :27:18
        clk1scount <= 7'h0;	// playground/src/SimTop.scala:15:26
      else if (_GEN)	// playground/src/SimTop.scala:23:16
        clk1scount <= clk1scount + 7'h1;	// playground/src/SimTop.scala:15:26, :25:29
      else if (io_Zero)	// playground/src/SimTop.scala:5:14
        clk1scount <= 7'h0;	// playground/src/SimTop.scala:15:26
      if (clk10scount == 4'hA)	// playground/src/SimTop.scala:16:27, :30:18, :33:19
        clk10scount <= 4'h0;	// playground/src/SimTop.scala:16:27
      else if (clk1scount == 7'hA)	// playground/src/SimTop.scala:15:26, :30:18
        clk10scount <= clk10scount + 4'h1;	// playground/src/SimTop.scala:16:27, :31:30
      else if (io_Zero)	// playground/src/SimTop.scala:5:14
        clk10scount <= 4'h0;	// playground/src/SimTop.scala:16:27
    end
  end // always @(posedge)
  `ifdef ENABLE_INITIAL_REG_	// <stdin>:57:3
    `ifdef FIRRTL_BEFORE_INITIAL	// <stdin>:57:3
      `FIRRTL_BEFORE_INITIAL	// <stdin>:57:3
    `endif // FIRRTL_BEFORE_INITIAL
    initial begin	// <stdin>:57:3
      automatic logic [31:0] _RANDOM[0:1];	// <stdin>:57:3
      `ifdef INIT_RANDOM_PROLOG_	// <stdin>:57:3
        `INIT_RANDOM_PROLOG_	// <stdin>:57:3
      `endif // INIT_RANDOM_PROLOG_
      `ifdef RANDOMIZE_REG_INIT	// <stdin>:57:3
        for (logic [1:0] i = 2'h0; i < 2'h2; i += 2'h1) begin
          _RANDOM[i[0]] = `RANDOM;	// <stdin>:57:3
        end	// <stdin>:57:3
        clkcount = _RANDOM[1'h0][24:0];	// <stdin>:57:3, playground/src/SimTop.scala:14:24
        clk1scount = _RANDOM[1'h0][31:25];	// <stdin>:57:3, playground/src/SimTop.scala:14:24, :15:26
        clk10scount = _RANDOM[1'h1][3:0];	// <stdin>:57:3, playground/src/SimTop.scala:16:27
      `endif // RANDOMIZE_REG_INIT
    end // initial
    `ifdef FIRRTL_AFTER_INITIAL	// <stdin>:57:3
      `FIRRTL_AFTER_INITIAL	// <stdin>:57:3
    `endif // FIRRTL_AFTER_INITIAL
  `endif // ENABLE_INITIAL_REG_
  bcd7seg seg1 (	// playground/src/SimTop.scala:36:20
    .seg_in  (clk1scount[3:0]),	// playground/src/SimTop.scala:15:26, :38:28
    .seg_out (io_Hex1)
  );
  bcd7seg seg2 (	// playground/src/SimTop.scala:37:20
    .seg_in  (clk10scount),	// playground/src/SimTop.scala:16:27
    .seg_out (io_Hex2)
  );
endmodule

