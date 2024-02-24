
import "DPI-C" function void sync_csrfile_regs(input int waddr,input int wdata);
import "DPI-C" function void sync_csr_exception_regs(input int mcause_in,input int pc_wb);
module Dpi_CsrCommit(
    input        clock,
    input        reset,
    input        dpi_valid,
    input        csr_wen,
    input [31:0] waddr,
    input [31:0] wdata,
    input        exception_wen,
    input [31:0] mcause_in,
    input [31:0] pc_wb
);
 always @(*)begin
   if(~reset)begin
     if(csr_wen&&dpi_valid) sync_csrfile_regs(waddr,wdata);
     if(exception_wen&&dpi_valid) sync_csr_exception_regs(mcause_in,pc_wb);
   end
  end
endmodule
    
