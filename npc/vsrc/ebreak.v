import "DPI-C" function void sim_exit();
module ebreak(
    input clock,
    input flag
);
always @(posedge clock)begin
    if(flag==1) sim_exit();
end
endmodule //moduleName
