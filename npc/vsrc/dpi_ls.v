
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
   input [31:0] wdata,
   output reg rdata_ok,
   output reg wdata_ok
);
 
always @(posedge clock) begin
  if(~reset)begin
    if(ld_wen) begin
      pmem_read (raddr,rdata);
      rdata_ok<=1;
    end
    else begin
      rdata_ok<=0;
      rdata[31:0]=0;
    end

    if(st_wen) begin
      pmem_write(waddr,wdata,wmask);
      wdata_ok<=1;
    end
    else begin
      wdata_ok<=0;
    end
 end
end
endmodule
    
