

module RegFile(	// @[<stdin>:469:3]
  input         clock,	// @[<stdin>:470:11]
                reset,	// @[<stdin>:471:11]
  input  [4:0]  io_waddr,	// @[playground/src/RegFile.scala:5:12]
  input  [31:0] io_wdata,	// @[playground/src/RegFile.scala:5:12]
  input  [4:0]  io_raddr1,	// @[playground/src/RegFile.scala:5:12]
                io_raddr2,	// @[playground/src/RegFile.scala:5:12]
  input         io_wen,	// @[playground/src/RegFile.scala:5:12]
  output [31:0] io_rdata1,	// @[playground/src/RegFile.scala:5:12]
                io_rdata2	// @[playground/src/RegFile.scala:5:12]
);
  reg [31:0] rf [0:31];
  wire [31:0] wdata_muxed;
  
  assign wdata_muxed = (io_waddr == 5'b00000) ? 32'h00000000 : io_wdata;
  
  always @(posedge clock or posedge reset) begin
    $display("%d waddr = %h, wdata = %h",io_wen,io_waddr,io_wdata);
    if (io_wen)
      rf[io_waddr] <= wdata_muxed;
  end
  
  assign io_rdata1 = (io_raddr1 != 5'b00000) ? rf[io_raddr1] : 32'h00000000;
  assign io_rdata2 = (io_raddr2 != 5'b00000) ? rf[io_raddr2] : 32'h00000000;
  
endmodule
