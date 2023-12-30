// Generated by CIRCT firtool-1.56.0
module IF_stage(	// @[<stdin>:11:3]
  input         clock,	// @[<stdin>:12:11]
                reset,	// @[<stdin>:13:11]
                IF_br_bus_is_jump,	// @[playground/src/IF_stage.scala:6:12]
  input  [31:0] IF_br_bus_dnpc,	// @[playground/src/IF_stage.scala:6:12]
  output [31:0] IF_IO_pc,	// @[playground/src/IF_stage.scala:6:12]
                IF_IO_inst	// @[playground/src/IF_stage.scala:6:12]
);

  reg  [31:0] REGpc;	// @[playground/src/IF_stage.scala:10:24]
  wire [31:0] snpc = REGpc + 32'h4;	// @[playground/src/IF_stage.scala:10:24, :11:31, :15:18]
  wire [31:0] nextpc = IF_br_bus_is_jump ? IF_br_bus_dnpc : snpc;	// @[playground/src/IF_stage.scala:11:31, :12:31, :16:15]
  always @(posedge clock) begin	// @[<stdin>:12:11]
    if (reset)	// @[<stdin>:12:11]
      REGpc <= 32'h7FFFFFFC;	// @[playground/src/IF_stage.scala:10:24]
    else	// @[<stdin>:12:11]
      REGpc <= nextpc;	// @[playground/src/IF_stage.scala:10:24, :12:31]
  end // always @(posedge)
  read_inst Fetch (	// @[playground/src/IF_stage.scala:13:23]
    .clock  (clock),
    .reset  (reset),
    .nextpc (nextpc),	// @[playground/src/IF_stage.scala:12:31]
    .pc     (/* unused */),
    .inst   (IF_IO_inst)
  );
  assign IF_IO_pc = REGpc;	// @[<stdin>:11:3, playground/src/IF_stage.scala:10:24]
endmodule

