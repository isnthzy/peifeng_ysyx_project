import "DPI-C" function int get_inst(input int raddr);
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
    inst<=get_inst(nextpc);
  end
end
endmodule //moduleName
