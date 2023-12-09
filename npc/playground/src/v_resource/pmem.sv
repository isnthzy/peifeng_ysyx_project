import "DPI-C" function void pmem_read(input int raddr, output int rdata);
// import "DPI-C" function void pmem_write(input int waddr, input int wdata, input byte wmask);
import "DPI-C" function void get_pc(input int nextpc);
wire [63:0] rdata;
module pmem_dpi(
    input        clock,
    input        reset,
    input [31:0] pc,
    input [31:0] nextpc,
    output [31:0] inst
); 
always @(posedge clock) begin  
  if(~reset)begin
    get_pc(nextpc);
    pmem_read(nextpc,inst);
    // if (valid) begin // 有读写请求时
    //     pmem_read(raddr, rdata);
    //     // if (wen) begin // 有写请求时
    //     // pmem_write(waddr, wdata, wmask);
    //     // end
    // end
    // else begin
    //     rdata = 0;
    // end
  end
end
endmodule //moduleName
