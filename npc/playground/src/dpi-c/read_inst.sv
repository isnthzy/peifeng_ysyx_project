import "DPI-C" function int get_inst(input int raddr);
// import "DPI-C" function void pmem_write(input int waddr, input int wdata, input byte wmask);

module read_inst(
    input         clock,
    input         reset,
    input  [31:0] nextpc,
    output reg [31:0] inst,
    input         fetch_wen
); 
always @(posedge clock) begin  
  if(~reset&&fetch_wen)begin
    inst<=get_inst(nextpc);
  end
end
endmodule //moduleName


// //yosys-sta_test
// module read_inst(
//     input         clock,
//     input         reset,
//     input  [31:0] nextpc,
//     input  [31:0] pc,
//     output reg [31:0] inst
// ); 
// reg [31:0] mem1[255:0];
// always @(posedge clock) begin  
//   if(~reset)begin
//     inst[31:0]<=mem1[nextpc];
//   end
// end
// endmodule //moduleName
