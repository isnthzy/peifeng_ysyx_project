import "DPI-C" function void sim_break();
module singal_ebreak(
    input clock,
    input flag
);
always @(posedge clock)begin
    if(flag==1) sim_break();
end
endmodule //moduleName
