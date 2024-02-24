
//import "DPI-C" function void sim_break(input int pc,input int ret_reg_data);
module Dpi_CsrCommit(
    input        clock,
    input        reset,
    input        dpi_valid,
    input        csr_wen,
    input        waddr,
    input        wdata,
    input        exception_wen,
    input        mcause_in,
    input        pc_wb
);
 always @(posedge clock)begin
   if(~reset)begin
     
   end
  end
endmodule
    
