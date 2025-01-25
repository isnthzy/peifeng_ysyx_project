package ErXCore

import chisel3._
import chisel3.util._
import DecodeSignal._

class ImmGen extends ErXCoreModule {
  val io=IO(new Bundle {
    val inst=Input(UInt(32.W))
    val sel =Input(UInt(3.W))
    val out =Output(UInt(32.W))
  })
  val ImmI = io.inst(31, 20)
  val ImmS = Cat(io.inst(31, 25), io.inst(11, 7))
  val ImmB = Cat(io.inst(31), io.inst(7), io.inst(30, 25), io.inst(11, 8), 0.U(1.W))
  val ImmU = Cat(io.inst(31, 12), 0.U(12.W))
  val ImmJ = Cat(io.inst(31), io.inst(19, 12), io.inst(20), io.inst(30, 21), 0.U(1.W))

  io.out := Mux1hDefMap(io.sel,Map(
    IMM_I -> Sext(ImmI, 32),
    IMM_S -> Sext(ImmS, 32),
    IMM_B -> Sext(ImmB, 32),
    IMM_U -> Sext(ImmU, 32),
    IMM_J -> Sext(ImmJ, 32)
  ))
}
