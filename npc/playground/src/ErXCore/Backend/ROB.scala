package ErXCore

import chisel3._
import chisel3.util._
import ErXCore.UIntUtils.{UIntWithGetIdx,UIntWithGetAge}

class ROB extends ErXCoreModule{
  val io = IO(new Bundle {
    val in = Vec(RobWidth,Flipped(Decoupled(new RenameIO)))
    val from_ex = Input(new ROBFromExecuteUpdate(updSize = IssueWidth))
    val fw_frt  = Output(new FrontFromBack)
    val fw_dr   = Output(new RenameFromCommitUpdate(updSize = RetireWidth))
    val fw_dp   = Output(new RSFromROB)
    val fw_sq   = Output(new StoreQueueFromROB)

    val out_diff = Output(Vec(RobWidth,Valid(new RenameIO)))
  })
  val rob = SyncReadMem(RobEntries, new RenameIO, SyncReadMem.WriteFirst)
  val complete = RegInit(VecInit(Seq.fill(RobEntries)(false.B)))
  val packet = RegInit(VecInit(Seq.fill(RobEntries)(0.U.asTypeOf(new RobPacket))))
  val commitValid = Wire(Vec(RobWidth,Bool()))
  val commitBits  = Wire(Vec(RobWidth,new RenameIO))

  val enqNum = PopCount(Cat(io.in.map(_.valid)))
  val deqNum = PopCount(Cat(commitValid))
  //如果用reg rob的大小怕不是要爆炸，同步mem要考虑读写的时候数目的变动
  val ringBuffHead = RegInit(0.U(RobAgeWidth.W))
  val ringBuffTail = RegInit(0.U(RobAgeWidth.W))
  val ringBuffCount = RegInit(0.U(RobIdxWidth.W))
  val headPtr  = ringBuffHead.getIdx(RobIdxWidth)
  val tailPtr  = ringBuffTail.getIdx(RobIdxWidth)
  val headFlag = ringBuffHead.getFlag(RobAgeWidth)
  val tailFlag = ringBuffTail.getFlag(RobAgeWidth)
  ringBuffCount := Mux(headFlag === tailFlag, headPtr - tailPtr, RobEntries.U + headPtr - tailPtr)
  val ringBuffEmpty   = (headFlag === tailFlag) && (headPtr === tailPtr)
  val ringBuffAllowin = (ringBuffCount +& enqNum - deqNum) <= RobEntries.U
  (0 until RobWidth).map(i => io.in(i).ready := ringBuffAllowin)

//
  val flushAll = WireDefault(false.B)
  io.fw_frt.flush := flushAll
  io.fw_dr.recover:= flushAll
  io.fw_dp.flush  := flushAll
  io.fw_sq.flush  := flushAll
//enqueue
  io.fw_dp.robAge := 0.U.asTypeOf(io.fw_dp.robAge) //override
  val enqValid = io.in.map(_.valid).reduce(_||_)
  when(enqValid&&ringBuffAllowin){
    ringBuffHead := ringBuffHead + enqNum
    for(i <- 0 until RobWidth){
      when(io.in(i).valid){
        rob.write(headPtr + i.U, io.in(i).bits)
        complete(headPtr + i.U) := false.B
        io.fw_dp.robAge(i) := ringBuffHead + i.U
      }
    }
  }

  //from execute
  for(i <- 0 until IssueWidth){
    when(io.from_ex.upd(i).valid){
      val robIdx = io.from_ex.upd(i).bits.robIdx
      complete(robIdx) := true.B
      packet(robIdx).br := io.from_ex.upd(i).bits.br
      packet(robIdx).isStore := io.from_ex.upd(i).bits.isStore
      packet(robIdx).isBranch:= io.from_ex.upd(i).bits.isBranch
    }
  }

  //dequeue check complete
  //easy select
  val validMask = Mux(ringBuffCount >= RetireWidth.U, ((1 << RetireWidth) - 1).U, 
                                                      UIntToOH(ringBuffCount, RetireWidth) - 1.U)
  val retireValid = Wire(Vec(RetireWidth,Bool()))
  commitValid := retireValid
  ringBuffTail := ringBuffTail + deqNum 
  //
  flushAll := (0 until RetireWidth).map(i => retireValid(i) && packet(i).isBranch && packet(i).br.taken).reduce(_||_)
  val brTakenSelect = OHToUInt(Cat((0 until RetireWidth).map(i => retireValid(i) && packet(i).isBranch && packet(i).br.taken)))
  io.fw_frt := 0.U.asTypeOf(io.fw_frt)   //override
  io.fw_sq.doDeq := (0 until RetireWidth).map(i => retireValid(i) && packet(i).isStore).reduce(_||_)
  io.fw_frt.tk := packet(tailPtr + brTakenSelect).br

  //
  for(i <- 0 until RetireWidth){
    commitBits(i) := rob.read(tailPtr + i.U)
    if(i == 0){
      retireValid(i) := validMask(i) && complete(tailPtr + i.U)
    }else{
      retireValid(i) :=(validMask(i) && complete(tailPtr + i.U) &&
        retireValid(i - 1) && !packet(tailPtr + (i - 1).U).br.taken && !packet(tailPtr + (i - 1).U).isStore)
    }

    io.fw_dr.upd(i).wen := retireValid(i) && commitBits(i).cs.rfWen
    io.fw_dr.upd(i).prfDst := commitBits(i).pf.prfDst
    io.fw_dr.upd(i).freePrfDst := commitBits(i).pf.pprfDst
    io.fw_dr.upd(i).rfDst  := commitBits(i).cs.rfDest
  }

  //----robCommit----
  for(i <- 0 until RetireWidth){
    io.out_diff(i).valid  := RegNext(retireValid)
    io.out_diff(i).bits := commitBits(i)
  }

  //flush
  when(flushAll){ 
    //NOTE:when a branch instruction is taken or mispredict branch instruction. The instruction after "rob" is wrong, and it needs to be cleared.
    complete := 0.U.asTypeOf(complete)
    packet   := 0.U.asTypeOf(packet)
    ringBuffHead := 0.U
    ringBuffTail := 0.U
  }

} 

