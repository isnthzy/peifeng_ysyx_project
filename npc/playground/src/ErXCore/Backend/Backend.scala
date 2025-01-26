package ErXCore

import chisel3._
import chisel3.util._


class Backend extends ErXCoreModule{
  val io = IO(new Bundle {
    val in = Vec(DecodeWidth,Flipped(Decoupled(new InstIO)))
  })
  
  val flush = false.B
  val DRstage   = Module(new DecodeRename)  // include "Decode&rename"
  val DSstage   = Module(new Dispatch) 
  val PRstage   = Module(new PrfRead)
  val EXstage   = Module(new Execute)
  val CMstage   = Module(new Commit)
  val ROB       = Module(new ROB)
//------Decode and Rename Stage------
  DRstage.io.in <> io.in
  DRstage.io.from_ex := EXstage.io.fw_dr
  DRstage.io.from_cm := CMstage.io.fw_dr

//-----     Dispatch Stage     ------
  PipeConnect(DSstage.io.in,DRstage.io.to_dp,flush)
  ROB.io.in <> DSstage.io.fw_rob
  ROB.io.from_ex := EXstage.io.fw_rob
  
//-----     PrfRead  stage     ------
  PipeConnect(PRstage.io.in,DSstage.io.to_pr,flush)

//-----     Execute  stage     ------
  PipeConnect(EXstage.io.in,PRstage.io.to_ex,flush)

//-----     Commit   stage     ------


}
