package ErXCore

import chisel3._
import chisel3.util._
import ErXCore.UIntUtils.UIntWithGetIdx

class ROB extends ErXCoreModule{
  val io = IO(new Bundle {
    val in = Vec(RobWidth,Flipped(Decoupled(new RenameIO)))
    val from_ex = Input(new ROBFromExecuteUpdate(updSize = IssueWidth))
    val fw_dp   = Output(new RSFromROB)
    val fw_sq   = Output(new StoreQueueFromROB)
    val to_cm   = Vec(RobWidth,Decoupled(new RenameIO))
  })
  val rob = SyncReadMem(RobEntries, new RenameIO, SyncReadMem.WriteFirst)
  val complete = RegInit(VecInit(Seq.fill(RobEntries)(false.B)))
  val packet = RegInit(VecInit(Seq.fill(RobEntries)(new RobPacket)))
  
  val enqNum = PopCount(Cat(io.in.map(_.valid)))
  val deqNum = PopCount(Cat(io.to_cm.map(_.valid)))
  (0 until RobWidth).map(i => io.in(i).ready := ringBuffAllowin)
  //如果用reg rob的大小怕不是要爆炸，同步mem要考虑读写的时候数目的变动
  val ringBuffHead = RegInit(0.U(RobAgeWidth.W))
  val ringBuffTail = RegInit(0.U(RobAgeWidth.W))
  val ringBuffCount = RegInit(0.U(RobIdxWidth.W))
  val headPtr  = ringBuffHead.getIdx(RobIdxWidth)
  val tailPtr  = ringBuffTail.getIdx(RobIdxWidth)
  val headFlag = ringBuffHead(RobAgeWidth)
  val tailFlag = ringBuffTail(RobAgeWidth)
  ringBuffCount := Mux(headFlag === tailFlag, headPtr - tailPtr, RobEntries.U + headPtr - tailPtr)
  val ringBuffEmpty   = (headFlag === tailFlag) && (headPtr === tailPtr)
  val ringBuffAllowin = (ringBuffCount +& enqNum - deqNum) <= RobEntries.U

 
//enqueue
  val enqValid = io.in.map(_.valid).reduce(_||_)
  when(enqValid&&ringBuffAllowin){
    ringBuffHead := ringBuffHead + enqNum
    for(i <- 0 until RobWidth){
      when(io.in(i).valid){
        rob.write(headPtr + i.U, io.in(i).bits)
        complete(headPtr + i.U) := false.B
        io.fw_dp.robAge(i) := headPtr + i.U
      }.otherwise{
        io.fw_dp.robAge(i) := 0.U
      }
    }
  }

  //from execute
  for(i <- 0 until IssueWidth){
    when(io.from_ex.upd(i).valid){
      val robIdx = io.from_ex.upd(i).robIdx
      complete(robIdx) := true.B
      packet(robIdx).br := io.from_ex.upd(i).br
      packet(robIdx).isStore := io.from_ex.upd(i).isStore
    }
  }

  //dequeue check complete
  //easy select
  val validMask = Mux(ringBuffCount >= RetireWidth.U, ((1 << RetireWidth) - 1).U, 
                                                      UIntToOH(ringBuffCount, RetireWidth) - 1.U)
  for(i <- 0 until RetireWidth){
    io.to_cm(i).bits := rob.read(tailPtr + i.U)
    if(i == 0){
      io.to_cm(i).valid := validMask(i) && complete(tailPtr + i.U)
    }else{
      io.to_cm(i).valid := validMask(i) && complete(tailPtr + i.U) && io.to_cm(i-1).valid
    }
  }


} 

