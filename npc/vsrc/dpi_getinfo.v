
import "DPI-C" function void get_info(input int pc,input int nextpc,input int inst);
module dpi_getinfo(
    input        clock,
    input        reset,
    input        dpi_valid,
    input [31:0] pc,
    input [31:0] nextpc,
    input [31:0] inst
    
);
 always @(posedge clock)begin
   if(~reset)begin
     if(dpi_valid) get_info(pc,nextpc,inst);
   end
  end
endmodule
    
