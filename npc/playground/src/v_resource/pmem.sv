import "DPI-C" function void pmem_read(input int raddr, output int rdata);
// import "DPI-C" function void pmem_write(input int waddr, input int wdata, input byte wmask);
wire [63:0] rdata;
module pmem_dpi(
    input         clock,
    input         reset,
    input         sram_valid,
    input         sram_wen,
    input  [31:0] raddr,
    output [31:0] rdata,
    input  [31:0] waddr,
    input  [31:0] wdata,
    input  [ 5:0] wmask
); 
always @(posedge clock) begin  
  if(~reset)begin
    if (sram_valid) begin // 有读写请求时
      pmem_read(raddr, rdata);
      if (sram_wen) begin // 有写请求时
      pmem_write(waddr, wdata, wmask);
      end
    end
    else begin
      rdata = 0;
    end
  end
end
endmodule //moduleName
