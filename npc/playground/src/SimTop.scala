import chisel3._
import chisel3.util._  

class SimTop extends Module {
  val io = IO(new Bundle {
    val TimeOut = Input(Bool())
    val Begin = Input(Bool())
    val Zero = Input(Bool())
    val Hex1 =Output(UInt(7.W))
    val Hex2 =Output(UInt(7.W))
  })  
  val counterMax50hz=24999999
  val counterMax1s=100
  val clkcount= RegInit(0.asUInt(log2Ceil(counterMax50hz).W))
  val clk1scount= RegInit(0.asUInt(log2Ceil(counterMax1s).W))
  val clk10scount= RegInit(0.asUInt(log2Ceil(10).W))
  clkcount := clkcount + 1.U
  when(io.Zero===true.B){
    clkcount := 0.U
    clk1scount :=0.U
    clk10scount :=0.U
  }
  when(clkcount===counterMax50hz.U){
    clkcount := 0.U
    clk1scount := clk1scount+1.U
  }
  when(clk1scount===counterMax1s.U){
    clk1scount :=0.U
  }
  when(clk1scount===10.U){
    clk10scount :=clk10scount+1.U
  }
  when(clk10scount===10.U){
    clk10scount :=0.U
  }
  val seg1 = Module(new bcd7seg())
  val seg2 = Module(new bcd7seg())
  seg1.seg.in := clk1scount(4)
  seg2.seg.in := clk10scount(4)
  io.Hex1 := seg1.seg.out
  io.Hex2 := seg2.seg.out
}

class bcd7seg extends Module{
  val seg = IO(new Bundle {
    val in = Input(UInt(4.W))
    val out= Output(UInt(7.W))
  })
  seg.out := MuxLookup(seg.in, 0.U)(Seq(
    0.U -> "b1000000".U, 1.U -> "b1111001".U, 
    2.U -> "b0100100".U, 3.U -> "b0110000".U,
    4.U -> "b0011001".U, 5.U -> "b0010010".U,
    6.U -> "b0000010".U, 7.U -> "b1111000".U, 
    8.U -> "b0000000".U, 9.U -> "b0010000".U
  ))
}
