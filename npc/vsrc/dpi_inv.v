
import "DPI-C" function void inv_break(input int pc);
module dpi_inv(
    input        clock,
    input        reset,
    input        dpi_valid,
    input        inv_flag,
    input [31:0] pc
);
 always @(posedge clock)begin
   if(~reset)begin
     if(inv_flag&&dpi_valid)  inv_break(pc);
   end
  end
endmodule
    
