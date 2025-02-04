package ErXCore
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

  io.dmem := DCache.io.out
//------Decode and Rename Stage------
  DRstage.io.in <> io.in
  DRstage.io.from_ex := EXstage.io.fw_dr

//-----     Dispatch Stage     ------
  PipeConnect(DSstage.io.in,DRstage.io.to_dp,flush)
  ROB.io.in <> DSstage.io.fw_rob
  ROB.io.from_ex := EXstage.io.fw_rob
  
//-----     PrfRead  stage     ------
  PipeConnect(PRstage.io.in,DSstage.io.to_pr,flush)

//-----     Execute  stage     ------
  PipeConnect(EXstage.io.in,PRstage.io.to_ex,flush)
  StoreQueue.io.st := EXstage.io.dmemStore
  StoreQueue.io.ld := EXstage.io.dmemLoad
  DCache.io.dl := StoreQueue.io.out.ld
  DCache.io.ds := StoreQueue.io.out.st
//-----     Commit   stage     ------


}
