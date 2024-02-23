
import "DPI-C" function void sim_break(input int pc,input int ret_reg_data);
module Dpi_Ebreak(
    input        clock,
    input        reset,
    input        dpi_valid,
    input        is_ebreak,
    input [31:0] pc,
    input [31:0] ret_reg_data
);
 always @(posedge clock)begin
   if(~reset)begin
     if(is_ebreak&&dpi_valid)  sim_break(pc,ret_reg_data);
   end
  end
endmodule
    
