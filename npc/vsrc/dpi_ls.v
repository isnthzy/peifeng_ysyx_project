
import "DPI-C" function void pmem_read (input int raddr, output int rdata);
import "DPI-C" function void pmem_write(input int waddr, input  int wdata, input byte wmask);
module dpi_ls(
   input        clock,
   input        reset,
   input        ld_wen,
   input        st_wen,
   input [31:0] raddr,
   output[31:0] rdata,
   input [ 7:0] wmask,
   input [31:0] waddr,
   input [31:0] wdata
);
 
always @(posedge clock) begin
  if(~reset)begin
    if(ld_wen&&clock) begin
      pmem_read (raddr,rdata);
    end
    else begin
      rdata[31:0]=0;
    end

    if(st_wen&&clock) begin
      pmem_write(waddr,wdata,wmask);
    end
  end
 end
endmodule
    
