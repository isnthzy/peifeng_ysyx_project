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
  val clkcount= RegInit(0.asUInt(8.W))
  val clk1scount= RegInit(0.asUInt(2.W))
  clkcount := clkcount + 1.U
  when(io.Zero===true.B){
    clkcount := 0.U
    clk1scount :=0.U
  }
  when(clkcount==="d24999999".U){
      clkcount := 0.U
      clk1scount := clk1scount+1.U
  }
  when(clk1scount==="d99".U){
    clk1scount :=0.U
  }
  val seg1 = Module(new bcd7seg())
  val seg2 = Module(new bcd7seg())
  seg1.seg.in := clk1scount(0)
  seg2.seg.in := clk1scount(1)
  io.Hex1 := seg1.seg.out
  io.Hex2 := seg2.seg.out
}

class bcd7seg extends Module{
  val seg = IO(new Bundle {
    val in = Input(UInt(1.W))
    val out= Output(UInt(7.W))
  })
  seg.out := MuxLookup(seg.in, 0.U)(Seq(
    0.U -> "b1000000".U, 1.U -> "b1111001".U, 
    2.U -> "b0100100".U, 3.U -> "b0110000".U,
    4.U -> "b0011001".U, 5.U -> "b0010010".U,
    6.U -> "b0000010".U, 7.U -> "b1111000".U, 
  ))
}
