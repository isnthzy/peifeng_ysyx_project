package ErXCore
import ErXCore.Difftest._
import ErXCore.Cache._
import chisel3._
import chisel3.util._


class Backend extends ErXCoreModule{
  val io = IO(new Bundle {
    val in = Vec(DecodeWidth,Flipped(Decoupled(new InstIO)))
    val fw_frt = new FrontFromBack
    val dmem = new AxiCacheIO()
  })
  
  val DRstage   = Module(new DecodeRename)  // include "Decode&rename"
  val DSstage   = Module(new Dispatch) 
  val PRstage   = Module(new PrfRead)
  val EXstage   = Module(new Execute)
  // val CMstage   = Module(new Commit)
  val ROB       = Module(new ROB)
  val DCache    = Module(new DCache)
  val StoreQueue = Module(new StoreQueue)

  val flush = ROB.io.fw_frt.flush
  io.fw_frt := ROB.io.fw_frt
  DRstage.io.from_rob := ROB.io.fw_dr
  DSstage.io.from_rob := ROB.io.fw_dp
  StoreQueue.io.from_rob := ROB.io.fw_sq

  io.dmem <> DCache.io.out
//------Decode and Rename Stage------
  DRstage.io.in <> io.in
  DRstage.io.from_ex := EXstage.io.fw_dr

//-----     Dispatch Stage     ------
  PipeConnect(DSstage.io.in,DRstage.io.to_dp,flush)
  DSstage.io.from_dr := DRstage.io.fw_dp
  ROB.io.in <> DSstage.io.fw_rob
  ROB.io.from_ex := EXstage.io.fw_rob
  
//-----     PrfRead  stage     ------
  PRstage.io.in <> DSstage.io.to_pr
  PRstage.io.from_ex  := EXstage.io.fw_pr

//-----     Execute  stage     ------
  PipeConnect(EXstage.io.in,PRstage.io.to_ex,flush,Width = 2)
  PipeQueueConnect(EXstage.io.in(2),PRstage.io.to_ex(2),flush)
  //IntRS运算使用PipeConnet链接，MemRS使用PipeQueue链接
  StoreQueue.io.st <> EXstage.io.dmemStore
  StoreQueue.io.ld <> EXstage.io.dmemLoad
  DCache.io.dl <> StoreQueue.io.out.ld
  DCache.io.ds <> StoreQueue.io.out.st

//-----     Commit   stage     ------
  if(EnableVerlatorSim){
    val Diff = Module(new DiffCommit)
    val gpr = Wire(Vec(ArfSize,UInt(XLEN.W)))
    dontTouchUtil(VecInit(ROB.io.out_diff.map(_.bits.robIdx)))
    ExcitingUtils.addSink(gpr,"DiffGPR",ExcitingUtils.Func)
    Diff.io.reg := gpr.asTypeOf(Diff.io.reg)
    Diff.io.instr.index := 0.U
    Diff.io.instr.valid := ROB.io.out_diff(0).valid
    Diff.io.instr.pc    := ROB.io.out_diff(0).bits.cf.pc
    Diff.io.instr.instr := ROB.io.out_diff(0).bits.cf.inst
    Diff.io.instr.skip  := 0.U
    Diff.io.instr.wen   := ROB.io.out_diff(0).bits.cs.rfWen
    Diff.io.instr.wdest := ROB.io.out_diff(0).bits.cs.rfDest
    Diff.io.instr.wdata := 0.U //no need
    Diff.io.instr.csrRstat  := 0.U
    Diff.io.instr.csrData   := 0.U

    Diff.io.instr1.index := 1.U
    Diff.io.instr1.valid := ROB.io.out_diff(1).valid
    Diff.io.instr1.pc    := ROB.io.out_diff(1).bits.cf.pc
    Diff.io.instr1.instr := ROB.io.out_diff(1).bits.cf.inst
    Diff.io.instr1.skip  := 0.U
    Diff.io.instr1.wen   := ROB.io.out_diff(1).bits.cs.rfWen
    Diff.io.instr1.wdest := ROB.io.out_diff(1).bits.cs.rfWen
    Diff.io.instr1.wdata := 0.U
    Diff.io.instr1.csrRstat  := 0.U
    Diff.io.instr1.csrData   := 0.U
    
    
    Diff.io.load  := 0.U.asTypeOf(Diff.io.load)
    Diff.io.load1 := 0.U.asTypeOf(Diff.io.load)
    Diff.io.store := 0.U.asTypeOf(Diff.io.store)
    Diff.io.store1:= 0.U.asTypeOf(Diff.io.store)

    val excpSelectIdx = WireDefault(0.U(log2Up(RetireWidth).W))
    for(i <- (0 until RetireWidth).reverse){
      when(ROB.io.out_diff(i).bits.excp.en){
        excpSelectIdx := i.U
      }
    }
    Diff.io.excp.excpValid := ROB.io.out_diff(excpSelectIdx).bits.excp.en
    Diff.io.excp.cause := ROB.io.out_diff(excpSelectIdx).bits.excp.cause
    Diff.io.excp.isMret := ROB.io.out_diff(excpSelectIdx).bits.excp.isMret
    Diff.io.excp.intrptNo := false.B
    Diff.io.excp.exceptionPC := ROB.io.out_diff(excpSelectIdx).bits.cf.pc
    Diff.io.excp.exceptionInst := ROB.io.out_diff(excpSelectIdx).bits.cf.inst

    Diff.io.csr.mcause  := 0x1800.U
    Diff.io.csr.mepc    := 0.U
    Diff.io.csr.mtvec   := 0.U
    Diff.io.csr.mstatus := 0.U

  }

  
  
}
