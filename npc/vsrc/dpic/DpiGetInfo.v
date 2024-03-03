
import "DPI-C" function void get_info(input int pc,input int inst,input bit dpi_valid);
module Dpi_GetInfo(
    input        clock,
    input        reset,
    input        dpi_valid,
    input [31:0] pc,
    input [31:0] inst
    
);
 always @(*)begin
   if(~reset)begin
     get_info(pc,inst,dpi_valid);
     //有可能因为阻塞等传递了无效的数据，需要在仿真环境中处理这些情况
   end
  end
endmodule
    
