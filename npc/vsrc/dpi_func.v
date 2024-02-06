
import "DPI-C" function void cpu_use_func(input int pc,input int nextpc,input int inst,input bit is_jal,input int rd);
module dpi_func(
    input        clock,
    input        reset,
    input        func_flag,
    input        is_jal,
    input [31:0] pc,
    input [31:0] nextpc,
    input [31:0] rd,
    input [31:0] inst
);
 always @(posedge clock)begin
   if(~reset)begin
     if(func_flag)   cpu_use_func(pc,nextpc,inst,is_jal,rd);
   end
  end
endmodule
    
