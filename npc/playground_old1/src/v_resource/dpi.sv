import "DPI-C" function void sim_break(input int pc,input int ret_reg);
import "DPI-C" function void inv_break(input int pc);
import "DPI-C" function void cpu_use_func(input int pc,input int nextpc,input int inst,input bit is_jal,input int rd);
// import "DPI-C" function void get_pc(input int pc);
wire [63:0] rdata;
module singal_dpi(
    input        clock,
    input        reset,
    input [31:0] pc,
    input [31:0] nextpc,
    input [31:0] inst,
    input [31:0] rd,
    input        is_jal,
    input        func_flag,
    input        ebreak_flag,
    input        inv_flag,
    input [31:0] ret_reg
);
always @(posedge clock)begin
    if(~reset)begin
        if(ebreak_flag) sim_break(nextpc,ret_reg);
        if(inv_flag) inv_break(nextpc);
        if(func_flag) cpu_use_func(pc,nextpc,inst,is_jal,rd);
    end
end
endmodule //moduleName
