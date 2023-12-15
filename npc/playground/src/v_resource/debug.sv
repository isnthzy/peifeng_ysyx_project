import "DPI-C" function void prt_debug(input bit[4:0] debug_1,input int debug_2);
module debug(
    input        clock,
    input        reset,
    input [4:0] debug_1,
    input [31:0] debug_2
);
always @(posedge clock)begin
    if(~reset)begin
      if(debug_1==5'h1) prt_debug(debug_1,debug_2);
      prt_debug(debug_1,debug_2);
    end
end
endmodule //moduleName
