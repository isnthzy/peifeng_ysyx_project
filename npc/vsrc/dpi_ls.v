
module dpi_ls(
   input        clock,
   input        reset,
   input        ld_wen,
   input        st_wen,
   input [31:0] raddr,
   output[31:0] rdata,
   input [ 7:0] wmask,
   input [31:0] waddr,
   input [31:0] wdata
);
   reg [31:0] mem2[255:0];
   reg [31:0] rdata_reg;
   // 时序逻辑：处理读和写操作
   always @(posedge clock or negedge reset) begin
       if (!reset) begin
           // 复位逻辑，初始化 rdata_reg
           rdata_reg <= 0;
           // 复位逻辑，初始化内存（如果需要）
           // 例如：for (int i = 0; i < 256; i++) mem2[i] <= 0;
       end else begin
           if (ld_wen) begin
               // 在读使能有效时读取数据
               rdata_reg <= mem2[raddr];
           end
           if (st_wen) begin
               // 在写使能有效时写入数据
               mem2[waddr] <= wdata;
           end
       end
   end
   // 组合逻辑：输出读数据
   always_comb begin
       if (ld_wen) begin
           rdata <= mem2[raddr];
       end else begin
           rdata <= rdata_reg;
       end
   end
endmodule
    
