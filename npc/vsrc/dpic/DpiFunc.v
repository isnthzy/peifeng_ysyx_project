
import "DPI-C" function void cpu_use_func(input int pc,input int nextpc,input bit is_ret,input bit is_jal,input bit is_rd0);
module Dpi_Func(
    input        clock,
    input        reset,
    input        dpi_valid,
    input        func_flag,
    input        is_jal,
    input [31:0] pc,
    input [31:0] nextpc,
    input        is_rd0,
    input        is_ret
);
 always @(posedge clock)begin
   if(~reset)begin
     if(func_flag&&dpi_valid)   cpu_use_func(pc,nextpc,is_ret,is_jal,is_rd0);
   end
  end
endmodule
    
