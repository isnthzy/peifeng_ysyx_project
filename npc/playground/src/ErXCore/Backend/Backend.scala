package ErXCore

import chisel3._
import chisel3.util._


class Backend extends ErXCoreModule{
  val io = IO(new Bundle {
    val in = Vec(DecodeWidth,Flipped(Decoupled(new InstIO)))
  })

  val DecodeStage = Module(new DecodeStage)  // include "Decode&rename"

//------Decode and Rename Stage------
  DecodeStage.io.in <> io.in

//


}
