import chisel3._
import chisel3.util._  

class SimTop extends Module {
  val io = IO(new Bundle {
    val ps2_clk = Input(Bool())
    val ps2_data = Input(Bool())
    val seg1 = Output(UInt(7.W))
    val seg2 = Output(UInt(7.W))
    val seg3 = Output(UInt(7.W))
    val seg4 = Output(UInt(7.W))
    val seg5 = Output(UInt(7.W))
    val seg6 = Output(UInt(7.W))
  })  
  val PS2Key   = Module(new PS2Keyboard())
  val bcd7seg1 = Module(new bcd7seg())
  val bcd7seg2 = Module(new bcd7seg())
  val bcd7seg3 = Module(new bcd7seg())
  val bcd7seg4 = Module(new bcd7seg())
  val bcd7seg5 = Module(new bcd7seg())
  val bcd7seg6 = Module(new bcd7seg())
  PS2Key.keyboard.ps2_clk := io.ps2_clk
  PS2Key.keyboard.ps2_data := io.ps2_data

  bcd7seg1.seg.in := PS2Key.keyboard.out(3,0)
  bcd7seg2.seg.in := PS2Key.keyboard.out(7,4)
  bcd7seg1.seg.in := PS2Key.keyboard.num(3,0)
  bcd7seg2.seg.in := PS2Key.keyboard.num(7,4)
  io.seg1 := bcd7seg1.seg.out
  io.seg2 := bcd7seg2.seg.out
  io.seg5 := bcd7seg1.seg.out
  io.seg6 := bcd7seg2.seg.out
}

class KeycodeToAscii extends Module {
  val io = IO(new Bundle {
    val keycode = Input(UInt(8.W))
    val ascii = Output(UInt(8.W))
  })
  // 没思路
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
    10.U -> "b0001000".U, 11.U -> "b0000011".U,
    12.U -> "b1000110".U, 13.U -> "b0100001".U,
    14.U -> "b0000110".U, 15.U -> "b0001110".U
  ))
}

class PS2Keyboard extends Module {
  val keyboard = IO(new Bundle {
    val ps2_clk = Input(Bool())
    val ps2_data = Input(Bool())
    val out = Output(UInt(8.W))
    val num = Output(UInt(8.W))
  })

  val buffer = RegInit(0.U(10.W))
  val count = RegInit(0.U(4.W))
  val ps2_clk_sync = RegInit(0.U(3.W))
  val numReg = RegInit(0.U(8.W))
  ps2_clk_sync := Cat(ps2_clk_sync(1, 0), keyboard.ps2_clk)

  val sampling = ps2_clk_sync(2) & ~ps2_clk_sync(1)
  keyboard.num := numReg

  when(sampling === true.B) {
    when(count === 10.U) {
      when((buffer(0) === 0.U) && keyboard.ps2_data && (~buffer(9, 1).orR)) { // start bit, stop bit, odd parity
        keyboard.out := buffer(8, 1)

        val incrementedNum = numReg + 1.U // 递增numReg的值
        numReg := incrementedNum // 更新numReg的值
      }
      count := 0.U // for next
    }.otherwise {
      buffer(count) := keyboard.ps2_data // store ps2_data
      count := count + 1.U
    }
  }
}