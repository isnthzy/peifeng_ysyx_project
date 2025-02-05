package ErXCore

import chisel3._
import chisel3.util._

class InstBuff extends ErXCoreModule {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new InstIO))
    val fw_pf = Output(new Pf4IbBundle())
    val fw_if = Output(new If4IbBundle())
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
  val validMask = WireDefault(VecInit(Seq.fill(DecodeWidth)(false.B)))
  when (canDequeue) {
    for (i <- 0 until DecodeWidth) {
      validMask(i) := canDequeue
    }
    queueHead := queueHead + 2.U
    queueCount := queueCount - 2.U
  }
  for (i <- 0 until DecodeWidth) {
    io.out(i).valid := queue(queueHead + i.U).valid && validMask(i)
    io.out(i).bits := queue(queueHead + i.U).bits
  }

  // jal
  val br = WireDefault(0.U.asTypeOf(new BranchBundle))
  io.fw_if.br := br
  io.fw_pf.br := br
  for(i <- 0 until DecodeWidth) {
    when(io.out(i).bits.inst(6,0) === "b1101111".U) {
      br.taken := true.B
      val inst = io.out(i).bits.inst
      val offset = Cat(inst(31), inst(19, 12), inst(20), inst(30, 21), 0.U(1.W))
      br.target := io.out(i).bits.pc + Sext(offset, 32)
    }
  }
  // io.out(i)
}