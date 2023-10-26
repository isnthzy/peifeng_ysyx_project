`timescale 1ns / 1ps
module SimTop(
  input reset,
  output [6:0]io_seg1,
  output [6:0]io_seg2,
  output [6:0]io_seg3,
  output [6:0]io_seg4
);

/* parameter */
parameter [31:0] clock_period = 10;

/* ps2_keyboard interface signals */
reg clrn,clk;
wire [7:0] data;
wire ready,overflow;
wire kbd_clk, kbd_data;
reg nextdata_n;

ps2_keyboard_model model(
    .ps2_clk(kbd_clk),
    .ps2_data(kbd_data)
);

ps2_keyboard inst(
    .clk(clk),
    .clrn(clrn),
    .ps2_clk(kbd_clk),
    .ps2_data(kbd_data),
    .data(data),
    .ready(ready),
    .nextdata_n(nextdata_n),
    .overflow(overflow)
);
wire [7:0]asciicode;
wire [3:0]seg1_in;
wire [3:0]seg2_in;
wire [3:0]seg3_in;
wire [3:0]seg4_in;
assign seg1_in = data[3:0];
assign seg2_in = data[7:4];
assign seg3_in = asciicode[3:0];
assign seg4_in = asciicode[7:4];
tranAscii tranAscii(
    .clock(clk),
    .scanCode(data),
    .asciiCode(asciicode)
);

bcd7seg seg1(
    .seg_in(seg1_in),
    .seg_out(io_seg1)
);
bcd7seg seg2(
    .seg_in(seg2_in),
    .seg_out(io_seg2)
);
bcd7seg seg3(
    .seg_in(seg3_in),
    .seg_out(io_seg3)
);
bcd7seg seg4(
    .seg_in(seg4_in),
    .seg_out(io_seg4)
);

// bcd7seg seg5(
//     .seg_in(),
//     .seg_out(io_seg5)
// );
// bcd7seg seg6(
//     .seg_in(),
//     .seg_out(io_seg6)
// );

initial begin /* clock driver */
    clk = 1;
    forever
        #(clock_period/2) clk = ~clk;
end

initial begin
    clrn = 1'b0;  #20;
    clrn = 1'b1;  #20;
    model.kbd_sendcode(8'h1C); // press 'A'
    #20 nextdata_n =1'b0; #20 nextdata_n =1'b1;//read data
    model.kbd_sendcode(8'hF0); // break code
    #20 nextdata_n =1'b0; #20 nextdata_n =1'b1; //read data
    model.kbd_sendcode(8'h1C); // release 'A'
    #20 nextdata_n =1'b0; #20 nextdata_n =1'b1; //read data
    model.kbd_sendcode(8'h1B); // press 'S'
    #20 model.kbd_sendcode(8'h1B); // keep pressing 'S'
    #20 model.kbd_sendcode(8'h1B); // keep pressing 'S'
    model.kbd_sendcode(8'hF0); // break code
    model.kbd_sendcode(8'h1B); // release 'S'
    #20;
    $stop;
end

endmodule