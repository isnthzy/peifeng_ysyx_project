

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

    reg [31:0] rf [31:0];

    always @(posedge clock) begin

        if (io_wen) begin
            rf[io_waddr] <= io_wdata;
            $display("waddr = %h, wdata = %h", io_waddr, io_wdata);
        end
    end
    
    assign io_rdata1 = rf[io_raddr1];
    assign io_rdata2 = rf[io_raddr2];    

endmodule
