package ErXCore

import chisel3._
import chisel3.util._

class DecodeStage extends ErXCoreModule{
  val io=IO(new Bundle {
    val in  = Flipped(Decoupled(Vec(DecodeWidth,new InstIO)))
    val out = Vec(DecodeWidth,Decoupled(new RenameIO))
  })
  val Decode = Module(new Decode)
  val Rename = Module(new Rename)

  io.in <> Decode.io.in
  Decode.io.out <> Rename.io.in
  io.out <> Rename.io.out
}