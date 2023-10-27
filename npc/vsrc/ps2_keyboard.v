module ps2_keyboard(clk,resetn,ps2_clk,ps2_data,data,num);
    input clk,resetn,ps2_clk,ps2_data;
    output reg [7:0]data;
    output reg [7:0]num;

    reg [9:0] buffer;        // ps2_data bits
    reg [3:0] count;  // count ps2_data bits
    reg [2:0] ps2_clk_sync;
    reg brek;
    always @(posedge clk) begin
        ps2_clk_sync <=  {ps2_clk_sync[1:0],ps2_clk};
    end

    wire sampling = ps2_clk_sync[2] & ~ps2_clk_sync[1];

    always @(posedge clk) begin
        if (resetn == 0) begin // reset
            count <= 0;
            num<=0;
            brek<=0;
        end
        else begin
            if (sampling) begin
              if (count == 4'd10) begin
                if ((buffer[0] == 0) &&  // start bit
                    (ps2_data)       &&  // stop bit
                    (^buffer[9:1])) begin      // odd  parity
                    $display("receive %x", buffer[8:1]);
                    if(brek==1)begin
                        data<=8'b0;
                        brek<=0;
                    end
                    else begin
                        data <=buffer[8:1];
                    end
                    if(data==8'hf0)begin
                        brek<=1;
                        num<=num+8'b1;
                        $display("num %x", num);
                    end
                end
                count <= 0;     // for next
              end else begin
                buffer[count] <= ps2_data;  // store ps2_data
                count <= count + 3'b1;
              end
            end
        end
    end

endmodule