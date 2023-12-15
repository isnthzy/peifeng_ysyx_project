import chisel3._
import chisel3.util._  

class RegFile extends Module{
  val io=IO(new Bundle {
    val waddr =Input(UInt(5.W))
    val wdata =Input(UInt(32.W))
    val raddr1=Input(UInt(5.W))
    val rdata1=Output(UInt(32.W))
    val raddr2=Input(UInt(5.W))
    val rdata2=Output(UInt(32.W))
    val wen=Input(Bool())
  })
  val rf=RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
  when(io.wen){ 
    when(io.waddr=/=0.U){
      rf(io.waddr):=wdata 
    }
  }
  io.rdata1:=rf(io.raddr1)
  io.rdata2:=rf(io.raddr2)
  // val debug = Module(new debug())
  // debug.io.clock:=clock
  // debug.io.reset:=reset
  // debug.io.debug_1:=io.waddr
  // debug.io.debug_2:=wdata
} 

// class debug extends BlackBox with HasBlackBoxPath {
//   val io = IO(new Bundle {
//     val clock       = Input(Clock())
//     val reset       = Input(Bool())
//     val debug_1     = Input(UInt(5.W))
//     val debug_2     = Input(UInt(32.W))
//   })
//   addPath("playground/src/v_resource/debug.sv")
// }
