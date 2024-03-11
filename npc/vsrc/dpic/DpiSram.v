
import "DPI-C" function  int pmem_read (input int raddr, output int rdata);
import "DPI-C" function void pmem_write(input int waddr, input  int wdata, input byte wmask);
module dpi_sram(
   input        clock,
   input [31:0] addr,
   input [31:0] wdata,
   input [ 7:0] wmask,
   input        req,
   input        wr,
   output reg [31:0] rdata
);
 
always @(posedge clock) begin
    if(req) begin
      if(wr) begin 
       pmem_write (addr,wdata,wmask);
      end
      else begin
       rdata<=pmem_read (addr);
      end
    end
end
endmodule
    
