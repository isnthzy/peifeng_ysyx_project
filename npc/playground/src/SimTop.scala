import chisel3._
import chisel3.util._  

class SimTop extends Module {
  val io = IO(new Bundle {
    val in = Input(Bool()) //为1暂停,为0开始
    val seg1 =Output(UInt(7.W))
    val seg2 =Output(UInt(7.W))
  })  
  val reg =RegInit("b01000000".U)
  val x8 =reg(0)+reg(2)+reg(3)+reg(4)
  when(io.in===1.U){
    reg := Cat(x8,reg(6,0))
  }
  val bcd7seg1 = Module(new bcd7seg())
  val bcd7seg2 = Module(new bcd7seg())
  bcd7seg1.seg.in := reg(3,0)
  bcd7seg2.seg.in := reg(7,4)
  io.seg1 := bcd7seg1.seg.out
  io.seg2 := bcd7seg2.seg.out
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
    8.U -> "b0000000".U, 9.U -> "b0010000".U,
    10.U -> "b0110001".U, 11.U -> "b1000010".U,
    12.U -> "b0000110".U, 13.U -> "b0100001".U,
    14.U -> "b0000011".U, 15.U -> "b0000111".U
  ))
}
