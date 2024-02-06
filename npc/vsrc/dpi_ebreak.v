
import "DPI-C" function void sim_break(input int pc,input int ret_reg);
module dpi_ebreak(
    input        clock,
    input        reset,
    input        ebreak_flag,
    input [31:0] pc,
    input [31:0] ret_reg
);
 always @(posedge clock)begin
   if(~reset)begin
     if(ebreak_flag)  sim_break(pc,ret_reg);
   end
  end
endmodule
    
