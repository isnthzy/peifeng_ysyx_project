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
  
  val flush = false.B
  val DRstage   = Module(new DecodeRename)  // include "Decode&rename"
  val DSstage   = Module(new Dispatch) 
  val PRstage   = Module(new PrfRead)
  val EXstage   = Module(new Execute)
  // val CMstage   = Module(new Commit)
  val ROB       = Module(new ROB)
  val DCache    = Module(new DCache)
  val StoreQueue = Module(new StoreQueue)

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
  PipeConnect(PRstage.io.in,DSstage.io.to_pr,flush)
  PRstage.io.from_ex  := EXstage.io.fw_pr
//-----     Execute  stage     ------
  PipeConnect(EXstage.io.in,PRstage.io.to_ex,flush)
  StoreQueue.io.st <> EXstage.io.dmemStore
  StoreQueue.io.ld <> EXstage.io.dmemLoad
  DCache.io.dl <> StoreQueue.io.out.ld
  DCache.io.ds <> StoreQueue.io.out.st

//-----     Commit   stage     ------
  if(EnableVerlatorSim){
    val Diff = Module(new DiffCommit)
    val gpr = Wire(Vec(32,UInt(XLEN.W)))
    ExcitingUtils.addSink(gpr,"DiffGPR",ExcitingUtils.Func)
    Diff.diff.reg := gpr
    Diff.diff.instr.index := 0.U
    Diff.diff.instr.valid := ROB.io.out_diff(0).valid
    Diff.diff.instr.pc    := ROB.io.out_diff(0).bits.cf.pc
    Diff.diff.instr.instr := ROB.io.out_diff(1).bits.cf.inst
    Diff.diff.instr.skip  := 0.U
    Diff.diff.instr.wen   := ROB.io.out_diff(0).bits.cs.rfWen
    Diff.diff.instr.wdest := ROB.io.out_diff(0).bits.cs.rfDest
    Diff.diff.instr.wdata := 0.U //no need
    Diff.diff.instr.csrRstat  := 0.U
    Diff.diff.instr.csrData   := 0.U

    Diff.diff.instr1.index := 1.U
    Diff.diff.instr1.valid := ROB.io.out_diff(1).valid
    Diff.diff.instr1.pc    := ROB.io.out_diff(1).bits.cf.pc
    Diff.diff.instr1.instr := ROB.io.out_diff(1).bits.cf.inst
    Diff.diff.instr1.skip  := 0.U
    Diff.diff.instr1.wen   := ROB.io.out_diff(1).bits.cs.rfWen
    Diff.diff.instr1.wdest := ROB.io.out_diff(1).bits.cs.rfWen
    Diff.diff.instr1.wdata := 0.U
    Diff.diff.instr1.csrRstat  := 0.U
    Diff.diff.instr1.csrData   := 0.U
    
    
    Diff.diff.load  := 0.U.asTypeOf(Diff.diff.load)
    Diff.diff.load1 := 0.U.asTypeOf(Diff.diff.load)
    Diff.diff.store := 0.U.asTypeOf(Diff.diff.store)
    Diff.diff.store1:= 0.U.asTypeOf(Diff.diff.store)
    Diff.diff.excp := 0.U.asTypeOf(Diff.diff.excp)
    Diff.diff.csr.mcause  := 0x1800.U
    Diff.diff.csr.mepc    := 0.U
    Diff.diff.csr.mtvec   := 0.U
    Diff.diff.csr.mstatus := 0.U

  }

  
  
}
