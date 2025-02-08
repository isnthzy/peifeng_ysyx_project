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
  val queueCount = Mux(queueTail >= queueHead, queueTail - queueHead, InstBuffSize.U + queueTail - queueHead)
  val queueFull = queueCount === InstBuffSize.U

  val br = Wire(Vec(DecodeWidth,new BranchBundle()))
  val flushAll = io.from_bck.flush || br.map(_.taken).reduce(_|_)

  io.in.ready := !queueFull
  // Flush logic
  when (flushAll) {
    queueHead := 0.U
    queueTail := 0.U
    queue.foreach(_.valid := false.B)
  }

  // Enqueue logic
  when (io.in.fire) {
    queue(queueTail).valid := true.B
    queue(queueTail).bits := io.in.bits
    queueTail := queueTail + 1.U
  }

  // Dequeue logic
  val canDequeue = queueCount >= 2.U
  //宽松的发射条件，queueCount == 1 时往往第二条指令已经进入队列了
  val validMask = WireDefault(VecInit(Seq.fill(DecodeWidth)(false.B)))
  val jumpMask  = Wire(Vec(DecodeWidth,Bool()))
  //NOTE: 当我前面有跳转指令发射时，后边的指令均不可发射
  for(i <- 0 until DecodeWidth) {
    if(i == 0){
      jumpMask(i) := true.B
    }else{
      jumpMask(i) := jumpMask(i-1) && !br(i-1).taken
    }
  }
  when (canDequeue) {
    for (i <- 0 until DecodeWidth) {
      validMask(i) := canDequeue && jumpMask(i)
      queue(queueHead + i.U).valid := false.B
    }
    queueHead := queueHead + 2.U
  }
  for (i <- 0 until DecodeWidth) {
    io.out(i).valid := queue(queueHead + i.U).valid && validMask(i)
    io.out(i).bits := queue(queueHead + i.U).bits
  }

  //NOTE: PreDecode jal
  val brSelectIdx = WireDefault(0.U(log2Up(DecodeWidth).W))
  io.fw_pf.br := br(brSelectIdx)
  io.fw_if.br := br(brSelectIdx)
  for(i <- 0 until DecodeWidth) {
    val inst = io.out(i).bits.inst
    val taken = inst(6, 0) === "b1101111".U
    val offset = Cat(inst(31), inst(19, 12), inst(20), inst(30, 21), 0.U(1.W))
    br(i).taken := io.out(i).fire && taken
    br(i).target := io.out(i).bits.pc + Sext(offset, 32)
  }
  for(i <- (0 until DecodeWidth).reverse) {
    when(br(i).taken){
      brSelectIdx := i.U
    }
  }
}