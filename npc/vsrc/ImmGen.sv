// Generated by CIRCT firtool-1.56.0
module ImmGen(	// @[<stdin>:771:3]
  input  [31:0] io_inst,	// @[playground/src/ImmGen.scala:8:12]
  input  [2:0]  io_sel,	// @[playground/src/ImmGen.scala:8:12]
  output [31:0] io_out	// @[playground/src/ImmGen.scala:8:12]
);

  reg [31:0] casez_tmp;	// @[playground/src/ImmGen.scala:19:35]
  always_comb begin	// @[playground/src/ImmGen.scala:19:35]
    casez (io_sel)	// @[playground/src/ImmGen.scala:19:35]
      3'b000:
        casez_tmp = 32'h0;	// @[playground/src/ImmGen.scala:19:35]
      3'b001:
        casez_tmp = {{20{io_inst[31]}}, io_inst[31:20]};	// @[playground/src/Bundles.scala:211:{10,15,37}, playground/src/ImmGen.scala:13:21, :19:35]
      3'b010:
        casez_tmp = {{20{io_inst[31]}}, io_inst[31:25], io_inst[11:7]};	// @[playground/src/Bundles.scala:211:{10,15,37}, playground/src/ImmGen.scala:14:{25,42}, :19:35]
      3'b011:
        casez_tmp = {io_inst[31:12], 12'h0};	// @[playground/src/ImmGen.scala:16:{17,25}, :19:35]
      3'b100:
        casez_tmp =
          {{12{io_inst[31]}}, io_inst[19:12], io_inst[20], io_inst[30:21], 1'h0};	// @[playground/src/Bundles.scala:211:10, playground/src/ImmGen.scala:15:{17,25}, :17:{38,55,68}, :19:35]
      3'b101:
        casez_tmp = {{20{io_inst[31]}}, io_inst[7], io_inst[30:25], io_inst[11:8], 1'h0};	// @[playground/src/Bundles.scala:211:10, playground/src/ImmGen.scala:15:{17,25,38,50,67}, :19:35]
      3'b110:
        casez_tmp = 32'h0;	// @[playground/src/ImmGen.scala:19:35]
      default:
        casez_tmp = 32'h0;	// @[playground/src/ImmGen.scala:19:35]
    endcase	// @[playground/src/ImmGen.scala:19:35]
  end // always_comb
  assign io_out = casez_tmp;	// @[<stdin>:771:3, playground/src/ImmGen.scala:19:35]
endmodule

