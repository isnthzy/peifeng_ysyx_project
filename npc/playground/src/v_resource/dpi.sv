import "DPI-C" function void sim_break(input int pc,input int ret_reg);
import "DPI-C" function void inv_break(input int pc);
module singal_dpi(
    input        clock,
    input        reset,
    input [31:0] pc,
    input        ebreak_flag,
    input        inv_flag,
    input [31:0] ret_reg
);
always @(posedge clock)begin
    if(~reset){
        if(ebreak_flag==1) sim_break(pc,ret_reg);
        if(inv_flag==1) inv_break(pc);
    }

end
endmodule //moduleName
