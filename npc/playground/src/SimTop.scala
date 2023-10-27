import chisel3._
import chisel3.util._  

class SimTop extends Module {
  val io = IO(new Bundle {
    val ps2_clk = Input(Bool())
    val ps2_data = Input(Bool())
    val seg1 = Output(UInt(7.W))
    val seg2 = Output(UInt(7.W))
    val seg5 = Output(UInt(7.W))
    val seg6 = Output(UInt(7.W))
  })  
  val PS2Key   = Module(new PS2Keyboard())
  val bcd7seg1 = Module(new bcd7seg())
  val bcd7seg2 = Module(new bcd7seg())
  val bcd7seg5 = Module(new bcd7seg())
  val bcd7seg6 = Module(new bcd7seg())
  PS2Key.keyboard.ps2_clk := io.ps2_clk
  PS2Key.keyboard.ps2_data := io.ps2_data

  bcd7seg1.seg.in := PS2Key.keyboard.out(3,0)
  bcd7seg2.seg.in := PS2Key.keyboard.out(7,4)
  bcd7seg5.seg.in := PS2Key.keyboard.num(3,0)
  bcd7seg6.seg.in := PS2Key.keyboard.num(7,4)
  io.seg1 := bcd7seg1.seg.out
  io.seg2 := bcd7seg2.seg.out
  io.seg5 := bcd7seg1.seg.out
  io.seg6 := bcd7seg2.seg.out
}

class TranAscii extends Module {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val scanCode = Input(UInt(8.W))
    val asciiCode = Output(UInt(8.W))
  })

  val asciiCodeReg = RegInit(0.U(8.W))

  asciiCodeReg := MuxLookup(io.scanCode, 0.U,(Seq(
    "h16".U -> "h30".U, // 0
    "h1e".U -> "h31".U, // 1
    "h26".U -> "h32".U, // 2
    "h25".U -> "h33".U, // 3
    "h2e".U -> "h34".U, // 4
    "h36".U -> "h35".U, // 5
    "h3d".U -> "h36".U, // 6
    "h3e".U -> "h37".U, // 7
    "h46".U -> "h38".U, // 8
    "h45".U -> "h39".U, // 9
    "h15".U -> "h51".U, // Q
    "h1d".U -> "h57".U, // W
    "h24".U -> "h45".U, // E
    "h2d".U -> "h52".U, // R
    "h2c".U -> "h54".U, // T
    "h35".U -> "h59".U, // Y
    "h3c".U -> "h55".U, // U
    "h43".U -> "h49".U, // I
    "h44".U -> "h4f".U, // O
    "h4d".U -> "h50".U, // P
    "h1c".U -> "h41".U, // A
    "h1b".U -> "h53".U, // S
    "h23".U -> "h44".U, // D
    "h2b".U -> "h46".U, // F
    "h34".U -> "h47".U, // G
    "h33".U -> "h48".U, // H
    "h3b".U -> "h4a".U, // J
    "h42".U -> "h4b".U, // K
    "h4b".U -> "h4c".U, // L
    "h1a".U -> "h5a".U, // Z
    "h22".U -> "h58".U, // X
    "h21".U -> "h43".U, // C
    "h2a".U -> "h56".U, // V
    "h32".U -> "h42".U, // B
    "h31".U -> "h4e".U, // N
    "h3a".U -> "h4d".U  // M
  )))
  io.asciiCode := asciiCodeReg
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

  val buffer = RegInit(VecInit(Seq.fill(10)(0.U(1.W))))
  val buffer_orR =buffer.slice(1, 10).reduce(_ ^ _).asBool
  val buffer_UInt = buffer.asUInt
  val count = RegInit(0.U(4.W))
  val ps2_clk_sync = RegInit(0.U(3.W))
  val num_b = RegInit(0.U(8.W))
  val Part = RegInit(0.U(9.W))
  keyboard.out := Part
  keyboard.num := num_b
  ps2_clk_sync := Cat(ps2_clk_sync(1, 0), keyboard.ps2_clk)

  val sampling = ps2_clk_sync(2) & ~ps2_clk_sync(1)
  when(sampling) {
    when(count === 10.U) {
      when((buffer(0) === 0.U) && keyboard.ps2_data && buffer_orR) { // start bit, stop bit, odd parity
        Part := buffer_UInt(9,1)
      }
      count := 0.U // for next
    }.otherwise {
      buffer(count) := keyboard.ps2_data // store ps2_data
      count := count + 1.U
    }
  }

}