`timescale 1ns / 1ps
module tranAscii(
  input clock,
  input [7:0] scanCode,
  output reg[7:0] asciiCode
);
always @(posedge clock) begin
	case (scanCode)		
        8'h16: asciiCode <= 8'h30;	//0
        8'h1e: asciiCode <= 8'h31;	//1
        8'h26: asciiCode <= 8'h32;	//2
        8'h25: asciiCode <= 8'h33;	//3
        8'h2e: asciiCode <= 8'h34;	//4
        8'h36: asciiCode <= 8'h35;	//5
        8'h3d: asciiCode <= 8'h36;	//6
        8'h3e: asciiCode <= 8'h37;	//7
        8'h46: asciiCode <= 8'h38;	//8
        8'h45: asciiCode <= 8'h39;	//9

        8'h41: asciiCode <= 8'h2c;	//,
        8'h49: asciiCode <= 8'h2e;	//.
        8'h4a: asciiCode <= 8'h2f;	// /
        8'h4c: asciiCode <= 8'h3b;	//;
        8'h52: asciiCode <= 8'h27;	//'
        8'h54: asciiCode <= 8'h5b;	//[
        8'h5b: asciiCode <= 8'h5d;	//]
        8'h5a: asciiCode <= 8'h0a;	//enter


		8'h15: asciiCode <= 8'h51;	//Q
		8'h1d: asciiCode <= 8'h57;	//W
		8'h24: asciiCode <= 8'h45;	//E
		8'h2d: asciiCode <= 8'h52;	//R
		8'h2c: asciiCode <= 8'h54;	//T
		8'h35: asciiCode <= 8'h59;	//Y
		8'h3c: asciiCode <= 8'h55;	//U
		8'h43: asciiCode <= 8'h49;	//I
		8'h44: asciiCode <= 8'h4f;	//O
		8'h4d: asciiCode <= 8'h50;	//P				  	
		8'h1c: asciiCode <= 8'h41;	//A
		8'h1b: asciiCode <= 8'h53;	//S
		8'h23: asciiCode <= 8'h44;	//D
		8'h2b: asciiCode <= 8'h46;	//F
		8'h34: asciiCode <= 8'h47;	//G
		8'h33: asciiCode <= 8'h48;	//H
		8'h3b: asciiCode <= 8'h4a;	//J
		8'h42: asciiCode <= 8'h4b;	//K
		8'h4b: asciiCode <= 8'h4c;	//L
		8'h1a: asciiCode <= 8'h5a;	//Z
		8'h22: asciiCode <= 8'h58;	//X
		8'h21: asciiCode <= 8'h43;	//C
		8'h2a: asciiCode <= 8'h56;	//V
		8'h32: asciiCode <= 8'h42;	//B
		8'h31: asciiCode <= 8'h4e;	//N
		8'h3a: asciiCode <= 8'h4d;	//M
		default: asciiCode <= 8'h00;
		endcase
    end
endmodule