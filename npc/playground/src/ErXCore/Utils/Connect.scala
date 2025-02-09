package ErXCore

import chisel3._
import chisel3.util._

class PipeConnectInnerModule[T <: Data](gen: T, vecWidth: Int) extends ErXCoreModule {
  val io = IO(new Bundle {
    val in = Flipped(Vec(vecWidth, DecoupledIO(gen)))
    val out = Vec(vecWidth, DecoupledIO(gen))
    val flush = Input(Bool())
  })

  val bits = Reg(Vec(vecWidth, chiselTypeOf(io.in(0).bits)))
  val bitsEmpty = RegInit(VecInit(Seq.fill(vecWidth)(true.B)))

  for (i <- 0 until vecWidth) {
    io.in(i).ready := bitsEmpty(i) || io.out(i).fire
    when(io.in(i).fire && (bitsEmpty(i) || io.out(i).fire)) {
      bits(i) := io.in(i).bits
      bitsEmpty(i) := false.B
    }.elsewhen(io.out(i).fire){
      bitsEmpty(i) := true.B
      bits(i) := 0.U.asTypeOf(io.in(0).bits)
    }
  }

  for (i <- 0 until vecWidth) {
    io.out(i).bits := bits(i)
    io.out(i).valid := ~bitsEmpty(i)
  }

  when(io.flush) {
    bits.foreach(_ := 0.U.asTypeOf(io.in(0).bits))
    io.out.foreach(_.valid := false.B)
    bitsEmpty.foreach(_ := true.B)
  }
  override def desiredName = s"PipeIO${gen.typeName}"
}

object PipeConnect {
  def apply[T <: Data](
    out: Vec[DecoupledIO[T]], 
    in: Vec[DecoupledIO[T]],  
    flush: Bool,
    Width: Int = 0
  ): Unit = {
    require(in.length == out.length)
    val vecWidth = if (Width == 0) in.length else Width
    val module = Module(new PipeConnectInnerModule(chiselTypeOf(in(0).bits), vecWidth))

    for(i <- 0 until vecWidth) {
      module.io.in(i) <> in(i)
      out(i) <> module.io.out(i)
    }
    module.io.flush := flush
  }
}
/*NOTE:
  为什么这么设计：按照其他核的实现，握手方式是这样的
               0  1  2
  masterValid  1  1
  masterData   d  d
  slaveReady   0  1
  slaveValid         1
  slaveData          d
  这样，即可以在1的地方发生握手，并在2向slave传入数据
  这种维护侧重于在master与slave之间维护握手
  但是ErX对于slave的维护依赖fire，这种维护把握手的维护交给了PipeConnect
  可以把ErXCore的维护当做一个Queue，但是消除了Queue进入弹出的卡顿
  因为这样整体都是慢一拍，所以不会出现卡顿，两种方式性能基本一致
               0  1  2
  masterValid  1  
  masterData   d
  BitsEmpty    1
  Bits            d
  slaveReady      1
  slaveValid      1
  slaveData       d

*/

// object PipeConnect {
//   def apply[T <: Data](
//     out: Vec[DecoupledIO[T]], 
//     in: Vec[DecoupledIO[T]],  
//     flush: Bool,
//     Width: Int = 0,              
//   ): Unit = {
//     require(in.length == out.length)
//     val vecWidth = if(Width == 0) in.length else Width
//     // for(i <- 0 until vecWidth) {
//     //   out(i) <> Queue(in(i), 1 ,flush = Some(flush))
//     // }
//     // val bits = Reg(Vec(vecWidth, chiselTypeOf(in(0).bits)))
//     // val outValid = RegInit(VecInit(Seq.fill(vecWidth)(false.B)))

//     // when (flush) {
//     //   bits.foreach(_ := 0.U.asTypeOf(in(0).bits)) 
//     //   outValid := 0.U.asTypeOf(outValid)
//     // } .otherwise {
//     //   for (i <- 0 until vecWidth) {
//     //     bits(i) := Mux(in(i).valid, in(i).bits, 0.U.asTypeOf(in(0).bits))
//     //     outValid(i) := in(i).fire
//     //   }
//     // }

//     // for(i <- 0 until vecWidth) {
//     //   out(i).bits  := bits(i)
//     //   out(i).valid := outValid(i)
//     //   in(i).ready := out(i).ready
//     // }
//   }
// }

object PipeQueueConnect {
  def apply[T <: Data](
    out: DecoupledIO[T], 
    in: DecoupledIO[T],  
    flush: Bool,          
  ): Unit = {
    out <> Queue(in, 2 ,flush = Some(flush))
  }
}
