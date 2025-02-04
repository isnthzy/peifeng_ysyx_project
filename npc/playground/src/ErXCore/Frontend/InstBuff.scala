package ErXCore

import chisel3._
import chisel3.util._

class InstBuff extends ErXCoreModule {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new InstIO))
    val from_bck = Input(new Bundle {
      val flush = Input(Bool())
    })
    
    val out = Vec(DecodeWidth,Decoupled(new InstIO))
  })

  val queue = RegInit(VecInit(Seq.fill(InstBuffSize)(0.U.asTypeOf(Valid(new InstIO)))))
  val queueHead = RegInit(0.U(log2Up(InstBuffSize).W))
  val queueTail = RegInit(0.U(log2Up(InstBuffSize).W))
  val queueCount = RegInit(0.U((log2Up(InstBuffSize) + 1).W))
  val queueFull = queueCount === InstBuffSize.U

  io.in.ready := !queueFull
  // Flush logic
  when (io.from_bck.flush) {
    queueHead := 0.U
    queueTail := 0.U
    queueCount := 0.U
    queue.foreach(_.valid := false.B)
  }

  // Enqueue logic
  when (io.in.valid && !queueFull) {
    queue(queueTail).valid := true.B
    queue(queueTail).bits := io.in.bits
    queueTail := queueTail + 1.U
    queueCount := queueCount + 1.U
  }

  // Dequeue logic
  val canDequeue = queueCount >= 2.U
  when (canDequeue) {
    for (i <- 0 until DecodeWidth) {
      io.out(i).valid := queue(queueHead + i.U).valid
      io.out(i).bits := queue(queueHead + i.U).bits
    }
    queueHead := queueHead + 2.U
    queueCount := queueCount - 2.U
  } otherwise {
    for (i <- 0 until DecodeWidth) {
      io.out(i).valid := false.B
    }
  }

}