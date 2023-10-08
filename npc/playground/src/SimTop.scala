import chisel3._
import chisel3.util._

class SimTop extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(8.W))
    val en = Input(Bool())
    val out = Output(UInt(3.W))
  })
  when(io.en){
    io.out:=0.U
    for(i<-0 until 8){
      when(io.in(i)===1.U){
        io.out:=i.U(3.W)
      }
    }
  }otherwise{
    io.out:=0.U
  }
}