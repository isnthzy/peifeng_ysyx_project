
import "DPI-C" function void get_pc(input int pc,input int nextpc);
module dpi_getpc(
    input        clock,
    input        reset,
    input        dpi_valid,
    input [31:0] pc,
    input [31:0] nextpc
    
);
 always @(posedge clock)begin
   if(~reset)begin
     if(dpi_valid) get_pc(pc,nextpc);
   end
  end
endmodule
    
