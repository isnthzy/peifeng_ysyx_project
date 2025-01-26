package ErXCore

import chisel3._
import chisel3.util._

object PipeConnect {
  def apply[T <: Data](
    out: Vec[DecoupledIO[T]], 
    in: Vec[DecoupledIO[T]],  
    flush: Bool,              
  ): Unit = {
    require(in.length == out.length)
    val vecWidth = in.length
    val bits = Reg(Vec(vecWidth, chiselTypeOf(in(0).bits)))
    val outValid = RegInit(VecInit(Seq.fill(vecWidth)(false.B)))

    when (flush) {
      bits.foreach(_ := 0.U.asTypeOf(in(0).bits)) 
      outValid := 0.U.asTypeOf(outValid)
    } .otherwise {
      for (i <- 0 until vecWidth) {
        bits(i) := Mux(in(i).valid, in(i).bits, 0.U.asTypeOf(in(0).bits))
        outValid(i) := in(i).valid
      }
    }

    for(i <- 0 until vecWidth) {
      out(i).bits  := bits
      out(i).valid := outValid(i)
      in(i).ready := out(i).ready
    }
  }
}
