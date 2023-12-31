// Generated by CIRCT firtool-1.56.0
import "DPI-C" function void pmem_read(input int raddr, output int rdata);
import "DPI-C" function void get_pc(input int pc,input int nextpc);
// import "DPI-C" function void pmem_write(input int waddr, input int wdata, input byte wmask);

module read_inst(
    input         clock,
    input         reset,
    input  [31:0] nextpc,
    input  [31:0] pc,
    output [31:0] inst
); 
always @(posedge clock) begin  
  if(~reset)begin
    get_pc(pc,nextpc);
    pmem_read(nextpc,inst);
  end
end
endmodule //moduleName
