import "DPI-C" function void prt_debug(input int debug_1,input int debug_2);
// import "DPI-C" function void get_pc(input int pc);
wire [63:0] rdata;
module debug(
    input        clock,
    input        reset,
    input [4:0] debug_1,
    input [31:0] debug_2
);
always @(posedge clock)begin
    if(~reset)begin
      prt_debug(debug_1,debug_2);
    end
end
endmodule //moduleName
