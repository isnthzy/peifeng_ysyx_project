import "DPI-C" function void pmem_read(input int raddr, output int rdata);
import "DPI-C" function void get_pc(input int pc,input int nextpc);
wire [63:0] rdata;
module fetch_inst(
    input         clock,
    input         reset,
    input  [31:0] pc,
    input  [31:0] nextpc,
    output [31:0] inst
); 
always @(posedge clock) begin  
  if(~reset)begin
    get_pc(pc,nextpc);
    pmem_read(nextpc,inst);
  end
end
endmodule //moduleName