
import "DPI-C" function void inv_break(input int pc);
module inv_break(
    input        clock,
    input        reset,
    input        inv_flag,
    input [31:0] pc
);
 always @(posedge clock)begin
   if(~reset)begin
     if(inv_flag)  inv_break(pc);
   end
  end
endmodule
    
