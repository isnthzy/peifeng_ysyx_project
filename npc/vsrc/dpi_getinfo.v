
import "DPI-C" function void get_info(input int pc,input int nextpc,input int inst,input bit dpi_valid);
module dpi_getinfo(
    input        clock,
    input        reset,
    input        dpi_valid,
    input [31:0] pc,
    input [31:0] nextpc,
    input [31:0] inst
    
);
 always @(negedge clock)begin
   if(~reset)begin
     get_info(pc,nextpc,inst,dpi_valid);
     //有可能因为阻塞等传递了无效的数据，需要在仿真环境中处理这些情况
   end
  end
endmodule
    
