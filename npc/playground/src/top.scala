import chisel3._
import chisel3.util._

class top extends Module{
    val io=IO(new Bundle{
        val in=Input(UInt(4.W))
        val sel=Input(UInt(2.W))
        val out=Output(UInt(1.W))
    })
    io.out :=MuxLookup(io.sel,0.U)(Seq(
        0.U -> io.in(0),
        1.U -> io.in(1),
        2.U -> io.in(2),
        3.U -> io.in(3)
    ))
}