// Generated by CIRCT firtool-1.56.0
// Standard header to adapt well known macros to our needs.

// Users can define 'PRINTF_COND' to add an extra gate to prints.
`ifndef PRINTF_COND_
  `ifdef PRINTF_COND
    `define PRINTF_COND_ (`PRINTF_COND)
  `else  // PRINTF_COND
    `define PRINTF_COND_ 1
  `endif // PRINTF_COND
`endif // not def PRINTF_COND_

module RegFile(	// @[<stdin>:826:3]
  input         clock,	// @[<stdin>:827:11]
                reset,	// @[<stdin>:828:11]
  input  [4:0]  io_waddr,	// @[playground/src/Regfile.scala:6:12]
  input  [31:0] io_wdata,	// @[playground/src/Regfile.scala:6:12]
  input  [4:0]  io_raddr1,	// @[playground/src/Regfile.scala:6:12]
                io_raddr2,	// @[playground/src/Regfile.scala:6:12]
  input         io_wen,	// @[playground/src/Regfile.scala:6:12]
  output [31:0] io_rdata1,	// @[playground/src/Regfile.scala:6:12]
                io_rdata2	// @[playground/src/Regfile.scala:6:12]
);

  reg [31:0] casez_tmp;	// @[playground/src/Regfile.scala:23:12]
  reg [31:0] casez_tmp_0;	// @[playground/src/Regfile.scala:24:12]
  reg [31:0] rf_0;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_1;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_2;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_3;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_4;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_5;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_6;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_7;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_8;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_9;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_10;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_11;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_12;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_13;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_14;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_15;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_16;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_17;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_18;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_19;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_20;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_21;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_22;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_23;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_24;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_25;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_26;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_27;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_28;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_29;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_30;	// @[playground/src/Regfile.scala:15:17]
  reg [31:0] rf_31;	// @[playground/src/Regfile.scala:15:17]
  always_comb begin	// @[playground/src/Regfile.scala:23:12]
    casez (io_raddr1)	// @[playground/src/Regfile.scala:23:12]
      5'b00000:
        casez_tmp = rf_0;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b00001:
        casez_tmp = rf_1;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b00010:
        casez_tmp = rf_2;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b00011:
        casez_tmp = rf_3;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b00100:
        casez_tmp = rf_4;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b00101:
        casez_tmp = rf_5;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b00110:
        casez_tmp = rf_6;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b00111:
        casez_tmp = rf_7;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01000:
        casez_tmp = rf_8;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01001:
        casez_tmp = rf_9;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01010:
        casez_tmp = rf_10;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01011:
        casez_tmp = rf_11;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01100:
        casez_tmp = rf_12;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01101:
        casez_tmp = rf_13;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01110:
        casez_tmp = rf_14;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01111:
        casez_tmp = rf_15;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10000:
        casez_tmp = rf_16;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10001:
        casez_tmp = rf_17;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10010:
        casez_tmp = rf_18;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10011:
        casez_tmp = rf_19;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10100:
        casez_tmp = rf_20;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10101:
        casez_tmp = rf_21;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10110:
        casez_tmp = rf_22;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10111:
        casez_tmp = rf_23;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b11000:
        casez_tmp = rf_24;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b11001:
        casez_tmp = rf_25;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b11010:
        casez_tmp = rf_26;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b11011:
        casez_tmp = rf_27;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b11100:
        casez_tmp = rf_28;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b11101:
        casez_tmp = rf_29;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b11110:
        casez_tmp = rf_30;	// @[playground/src/Regfile.scala:15:17, :23:12]
      default:
        casez_tmp = rf_31;	// @[playground/src/Regfile.scala:15:17, :23:12]
    endcase	// @[playground/src/Regfile.scala:23:12]
  end // always_comb
  always_comb begin	// @[playground/src/Regfile.scala:23:12]
    casez (io_raddr2)	// @[playground/src/Regfile.scala:23:12]
      5'b00000:
        casez_tmp_0 = rf_0;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b00001:
        casez_tmp_0 = rf_1;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b00010:
        casez_tmp_0 = rf_2;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b00011:
        casez_tmp_0 = rf_3;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b00100:
        casez_tmp_0 = rf_4;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b00101:
        casez_tmp_0 = rf_5;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b00110:
        casez_tmp_0 = rf_6;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b00111:
        casez_tmp_0 = rf_7;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01000:
        casez_tmp_0 = rf_8;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01001:
        casez_tmp_0 = rf_9;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01010:
        casez_tmp_0 = rf_10;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01011:
        casez_tmp_0 = rf_11;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01100:
        casez_tmp_0 = rf_12;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01101:
        casez_tmp_0 = rf_13;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01110:
        casez_tmp_0 = rf_14;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b01111:
        casez_tmp_0 = rf_15;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10000:
        casez_tmp_0 = rf_16;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10001:
        casez_tmp_0 = rf_17;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10010:
        casez_tmp_0 = rf_18;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10011:
        casez_tmp_0 = rf_19;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10100:
        casez_tmp_0 = rf_20;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10101:
        casez_tmp_0 = rf_21;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10110:
        casez_tmp_0 = rf_22;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b10111:
        casez_tmp_0 = rf_23;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b11000:
        casez_tmp_0 = rf_24;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b11001:
        casez_tmp_0 = rf_25;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b11010:
        casez_tmp_0 = rf_26;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b11011:
        casez_tmp_0 = rf_27;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b11100:
        casez_tmp_0 = rf_28;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b11101:
        casez_tmp_0 = rf_29;	// @[playground/src/Regfile.scala:15:17, :23:12]
      5'b11110:
        casez_tmp_0 = rf_30;	// @[playground/src/Regfile.scala:15:17, :23:12]
      default:
        casez_tmp_0 = rf_31;	// @[playground/src/Regfile.scala:15:17, :23:12]
    endcase	// @[playground/src/Regfile.scala:23:12]
  end // always_comb
  always @(posedge clock) begin	// @[<stdin>:827:11]
    if (reset) begin	// @[<stdin>:827:11]
      rf_0 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_1 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_2 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_3 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_4 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_5 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_6 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_7 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_8 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_9 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_10 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_11 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_12 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_13 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_14 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_15 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_16 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_17 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_18 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_19 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_20 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_21 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_22 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_23 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_24 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_25 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_26 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_27 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_28 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_29 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_30 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      rf_31 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
    end
    else begin	// @[<stdin>:827:11]
      if (~io_wen | (|io_waddr)) begin	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}]
      end
      else	// @[playground/src/Regfile.scala:15:17, :16:15, :17:25]
        rf_0 <= 32'h0;	// @[playground/src/Regfile.scala:15:{17,25}]
      if (io_wen & (|io_waddr) & io_waddr == 5'h1)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_1 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h2)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_2 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h3)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_3 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h4)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_4 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h5)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_5 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h6)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_6 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h7)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_7 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h8)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_8 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h9)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_9 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'hA)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_10 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'hB)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_11 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'hC)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_12 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'hD)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_13 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'hE)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_14 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'hF)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_15 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h10)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_16 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h11)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_17 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h12)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_18 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h13)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_19 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h14)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_20 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h15)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_21 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h16)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_22 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h17)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_23 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h18)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_24 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h19)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_25 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h1A)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_26 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h1B)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_27 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h1C)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_28 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h1D)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_29 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & io_waddr == 5'h1E)	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_30 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
      if (io_wen & (|io_waddr) & (&io_waddr))	// @[playground/src/Regfile.scala:15:17, :16:15, :17:{18,25}, :18:19]
        rf_31 <= io_wdata;	// @[playground/src/Regfile.scala:15:17]
    end
  end // always @(posedge)
  assign io_rdata1 = casez_tmp;	// @[<stdin>:826:3, playground/src/Regfile.scala:23:12]
  assign io_rdata2 = casez_tmp_0;	// @[<stdin>:826:3, playground/src/Regfile.scala:24:12]
endmodule

