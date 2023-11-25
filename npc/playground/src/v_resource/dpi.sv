import "DPI-C" function void sim_break();
module singal_ebreak(
    input        clock,
    input [32:0] pc,
    input        ebreak_flag,
    input        inv_flag
);
always @(posedge clock)begin
    if(ebreak_flag==1) sim_break();
    if(inv_flag==1) inv_break();
end
endmodule //moduleName
