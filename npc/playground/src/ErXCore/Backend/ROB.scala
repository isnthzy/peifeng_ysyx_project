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
    val csrIO   = Flipped(new CsrRobIO)
    val out_diff = Output(Vec(RobWidth,Valid(new ROBDiffOut)))
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
  val headPtr  = ringBuffHead.getIdx(RobIdxWidth)
  val tailPtr  = ringBuffTail.getIdx(RobIdxWidth)
  val headFlag = ringBuffHead.getFlag(RobAgeWidth)
  val tailFlag = ringBuffTail.getFlag(RobAgeWidth)
  val ringBuffCount = Mux(headFlag === tailFlag, headPtr - tailPtr, RobEntries.U + headPtr - tailPtr)
  val ringBuffEmpty   = (headFlag === tailFlag) && (headPtr === tailPtr)
  val ringBuffAllowin = (ringBuffCount +& 2.U - deqNum) <= RobEntries.U
  (0 until RobWidth).map(i => io.in(i).ready := ringBuffAllowin)

//
  val flushAll = WireDefault(false.B)
  val flushROB = WireDefault(false.B)
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
      when(io.in(i).fire){
        rob.write(headPtr + i.U, io.in(i).bits)
        complete(headPtr + i.U) := false.B
        packet(headPtr + i.U) := 0.U.asTypeOf(new RobPacket)
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
      packet(robIdx).csr     := io.from_ex.upd(i).bits.csr
    }
  }


  val validMask = Mux(ringBuffCount >= RetireWidth.U, ((1 << RetireWidth) - 1).U, 
                                                      UIntToOH(ringBuffCount, RetireWidth) - 1.U)
  val retireValid = Wire(Vec(RetireWidth,Bool()))
  
  //TODO:写成def函数
  //NOTE:Branch
  //NOTE:Mask的作用是当前一条指令是跳转时，后面的指令无效化。mask概念适用于branch excp store
  val branchMask = Wire(Vec(RetireWidth,Bool()))
  val branchSelectIdx = WireDefault(0.U(log2Up(RobEntries).W))
  val branchValid = Wire(Vec(RetireWidth,Bool()))  
  for(i <- 0 until RetireWidth){
    val idx = tailPtr + i.U
    branchValid(i) := packet(idx).isBranch && packet(idx).br.taken
    if(i == 0){
      branchMask(i) := true.B
    }else{
      branchMask(i) := branchMask(i - 1) && !branchValid(i - 1)
    }
  }
  for(i <- (0 until RetireWidth).reverse){
    val idx = tailPtr + i.U
    when(branchValid(i)){
      branchSelectIdx := idx
    }
  }

  //NOTE:excp
  val excpTypeValid = Wire(Vec(RetireWidth,Bool()))
  val xretValid = Wire(Vec(RetireWidth,Bool()))
  val excpValid = (0 until RetireWidth).map(i=> excpTypeValid(i) || xretValid(i))
  val excpMask = Wire(Vec(RetireWidth,Bool()))
  val excpResult = Wire(Vec(RetireWidth,new ExcpResultBundle()))
  for(i <- 0 until RetireWidth){
    excpTypeValid(i) := packet(tailPtr + i.U).csr.excpType.asUInt.orR
    xretValid(i) := packet(tailPtr + i.U).csr.isXret
    val excpNum = packet(tailPtr + i.U).csr.excpType.asUInt
    // val 
    excpResult(i) := MuxCase(0.U,Seq(
      excpNum(0)  -> Cat(ECODE.IAM,1.U(1.W),commitBits(i).cf.pc                  ),
      excpNum(1)  -> Cat(ECODE.IAF,1.U(1.W),commitBits(i).cf.pc                  ),
      excpNum(2)  -> Cat(ECODE.INE,0.U(1.W),0.U(XLEN.W)                          ),
      excpNum(3)  -> Cat(ECODE.BKP,0.U(1.W),0.U(XLEN.W)                          ),
      excpNum(4)  -> Cat(ECODE.LAM,1.U(1.W),packet(tailPtr + i.U).csr.memBadAddr ),
      excpNum(5)  -> Cat(ECODE.LAF,0.U(1.W),0.U(XLEN.W)                          ),
      excpNum(6)  -> Cat(ECODE.SAM,1.U(1.W),packet(tailPtr + i.U).csr.memBadAddr ),
      excpNum(7)  -> Cat(ECODE.SAF,0.U(1.W),0.U(XLEN.W)                          ),
      excpNum(8)  -> Cat(ECODE.ECU,1.U(1.W),commitBits(i).cf.pc                  ),
      excpNum(9)  -> Cat(ECODE.ECS,1.U(1.W),commitBits(i).cf.pc                  ),
      excpNum(10) -> Cat(ECODE.ECM,1.U(1.W),commitBits(i).cf.pc                  ),
      excpNum(11) -> Cat(ECODE.IPF,1.U(1.W),packet(tailPtr + i.U).csr.memBadAddr ),
      excpNum(12) -> Cat(ECODE.LPF,1.U(1.W),packet(tailPtr + i.U).csr.memBadAddr ),
      excpNum(13) -> Cat(ECODE.SPF,1.U(1.W),packet(tailPtr + i.U).csr.memBadAddr ),
    )).asTypeOf(new ExcpResultBundle())
    if(i == 0){
      excpMask(i) := true.B
    }else{
      excpMask(i) := excpMask(i - 1) && !excpValid(i - 1)
    }
  }
  val excpSelectIdx = WireDefault(0.U(log2Up(RetireWidth).W))
  for(i <- (0 until RetireWidth).reverse){
    when(excpTypeValid(i)){
      excpSelectIdx := i.U
    }
  }
  io.csrIO.update.excpFlush := excpFlush
  io.csrIO.update.mretFlush := xretFlush
  io.csrIO.update.excpResult := excpResult(excpSelectIdx)
  //NOTE:csr:
  val csrWriteMask = Wire(Vec(RetireWidth,Bool()))
  val csrWriteSelectIdx = WireDefault(0.U(log2Up(RetireWidth).W))
  val csrWriteValid = Wire(Vec(RetireWidth,Bool()))
  val csrWriteResult = WireDefault(0.U.asTypeOf(new WriteCsr))
  for(i <- 0 until RetireWidth){
    csrWriteValid(i) := packet(tailPtr + i.U).csr.write.wen
    if(i == 0){
      csrWriteMask(i) := true.B
    }else{
      csrWriteMask(i) := csrWriteMask(i - 1) && !csrWriteValid(i - 1)
    }
  }
  for(i <- (0 until RetireWidth).reverse){
    when(csrWriteValid(i)){
      csrWriteResult := packet(tailPtr + i.U).csr.write
      csrWriteSelectIdx := i.U
    }
  }
  io.csrIO.write := csrWriteResult
  io.csrIO.write.wen := csrWriteResult.wen && retireValid(csrWriteSelectIdx)

  //NOTE:store:
  val storeMask = Wire(Vec(RetireWidth,Bool()))
  val storeValid = Wire(Vec(RetireWidth,Bool()))
  for(i <- 0 until RetireWidth){
    storeValid(i) := packet(tailPtr + i.U).isStore
    if(i == 0){
      storeMask(i) := true.B
    }else{
      storeMask(i) := storeMask(i - 1) && !storeValid(i - 1)
    }
  }
  //flush & taken
  val frtNext = Reg(new FrontFromBack)
  io.fw_frt := frtNext
  //public flush 
  flushAll := frtNext.flush
  flushROB := (0 until RetireWidth).map(i => retireValid(i) 
    && (branchValid(i) || excpValid(i) || csrWriteValid(i))).reduce(_||_)
  /*NOTE:flush,br,excp使用Reg类型延缓一拍，因为可能有一种情况是第一条指令正常执行，
  后面几因为种种原因需要冲刷，但是第一条指令还没有写入完成。
  解决方法1：因此需要flush延缓一拍生效ROB需要立即冲刷（冲刷2次），确保后边几条指令不会因为ROB残留启动第二次
  解决方法2：维护一个掩码，当退休宽度里有需要对流水线冲刷的指令，把这个指令放在下一次第一个槽位执行。
  两种方法对性能影响效果等效*/
  lazy val excpFlush = excpTypeValid.asUInt.orR
  lazy val xretFlush = xretValid.asUInt.orR
  val refetchPC = csrWriteValid.asUInt.orR
  io.fw_sq.doDeq := (0 until RetireWidth).map(i => retireValid(i) && storeValid(i)).reduce(_||_)
  frtNext.flush := flushROB
  frtNext.tk.taken := flushROB && (packet(branchSelectIdx).br.taken || excpFlush || xretFlush || refetchPC)
  //TODO:excp
  frtNext.tk.target := Mux(packet(branchSelectIdx).br.taken,packet(branchSelectIdx).br.target,
                        Mux(excpFlush,io.csrIO.entry.mtvec,
                          Mux(xretFlush,io.csrIO.entry.mepc,
                            Mux(refetchPC,commitBits(csrWriteSelectIdx).cf.pc + 4.U,0.U))))

  //dequeue check complete
  //NOTE:easy select
  commitValid := retireValid
  ringBuffTail := ringBuffTail + deqNum

  val syncReadTailPtr = (ringBuffTail + deqNum).getIdx(RobIdxWidth)
  //NOTE:commitBits由于是syncReadMem，读慢一拍，因此需要使用要移动的tail指针提前读结果
  for(i <- 0 until RetireWidth){
    commitBits(i) := rob.read(syncReadTailPtr + i.U)
    if(i == 0){
      retireValid(i) := validMask(i) && complete(tailPtr + i.U)
    }else{
      retireValid(i) :=(validMask(i) && complete(tailPtr + i.U) &&
        retireValid(i - 1) && storeMask(i) && csrWriteMask(i) && excpMask(i) && branchMask(i))
    } 

    io.fw_dr.upd(i).wen := retireValid(i) && commitBits(i).cs.rfWen
    io.fw_dr.upd(i).prfDst := commitBits(i).pf.prfDst
    io.fw_dr.upd(i).freePrfDst := commitBits(i).pf.pprfDst
    io.fw_dr.upd(i).rfDst  := commitBits(i).cs.rfDest
  }

  //----robCommitDiff----
  if(EnableVerlatorSim){
    import ErXCore.Difftest._
    val diffStoreValid = Wire(Bool())
    val diffLoadValid  = Wire(Bool())
    val diffLSAddr = Wire(UInt(XLEN.W))
    val diffLSData = Wire(UInt(XLEN.W))
    val diffLSLen  = Wire(UInt(8.W))
    val robIdx     = Wire(UInt(RobIdxWidth.W))
    ExcitingUtils.addSink(diffStoreValid,"diffStoreValid",ExcitingUtils.Func)
    ExcitingUtils.addSink(diffLoadValid ,"iffLoadValid"  ,ExcitingUtils.Func)
    ExcitingUtils.addSink(diffLSAddr    ,"diffLSAddr"    ,ExcitingUtils.Func)
    ExcitingUtils.addSink(diffLSData    ,"diffLSData"    ,ExcitingUtils.Func)
    ExcitingUtils.addSink(diffLSLen     ,"diffLSLen"     ,ExcitingUtils.Func)
    ExcitingUtils.addSink(robIdx        ,"diffRobIdx"    ,ExcitingUtils.Func)

    val outStorePacket = Reg(Vec(RobEntries,new DiffStoreBundle()))
    val outLoadPacket = Reg(Vec(RobEntries,new DiffLoadBundle()))
    when(enqValid && ringBuffAllowin){
      for(i <- 0 until RobWidth){
        when(io.in(i).fire){
          outStorePacket(headPtr + i.U).valid := false.B
          outLoadPacket(headPtr + i.U).valid  := false.B
        }
      }
    }
    when(diffStoreValid){
      outStorePacket(robIdx).valid := true.B
      outStorePacket(robIdx).paddr := diffLSAddr
      outStorePacket(robIdx).vaddr := diffLSAddr
      outStorePacket(robIdx).data  := diffLSData
      outStorePacket(robIdx).len   := diffLSLen
    }
    when(diffLoadValid){
      outLoadPacket(robIdx).valid := true.B
      outLoadPacket(robIdx).paddr := diffLSAddr
      outLoadPacket(robIdx).vaddr := diffLSAddr
      outLoadPacket(robIdx).data  := diffLSData
      outLoadPacket(robIdx).len   := diffLSLen
    }
    for(i <- 0 until RetireWidth){
      io.out_diff(i).valid  := retireValid(i)
      io.out_diff(i).bits.pf := commitBits(i).pf
      io.out_diff(i).bits.cf := commitBits(i).cf
      io.out_diff(i).bits.cs := commitBits(i).cs
      io.out_diff(i).bits.robIdx := commitBits(i).robIdx

      io.out_diff(i).bits.excp.isMret := xretValid(i) && retireValid(i)
      io.out_diff(i).bits.excp.intrptNo := false.B
      io.out_diff(i).bits.excp.en := excpTypeValid(i) && retireValid(i)
      io.out_diff(i).bits.excp.cause := excpResult(i).ecode

      io.out_diff(i).bits.store := outStorePacket(tailPtr + i.U)
      io.out_diff(i).bits.store.valid  := outStorePacket(tailPtr + i.U).valid(0) & retireValid(i)
      io.out_diff(i).bits.load  := outLoadPacket(tailPtr + i.U)
      io.out_diff(i).bits.load.valid  := outLoadPacket(tailPtr + i.U).valid(0) & retireValid(i)
    }

    when(flushROB || flushAll){ 
      outStorePacket.foreach(_.valid := false.B)
      outLoadPacket.foreach(_.valid := false.B)
    }
  }


  //flush
  when(flushROB || flushAll){ 
    //NOTE:when a branch instruction is taken or mispredict branch instruction. The instruction after "rob" is wrong, and it needs to be cleared.
    complete := 0.U.asTypeOf(complete)
    packet   := 0.U.asTypeOf(packet)
    ringBuffHead := 0.U
    ringBuffTail := 0.U
  }

} 

