package ErXCore

import chisel3._
import chisel3.util._

class StoreQueue extends Module {
  val io = IO(new Bundle {
    // val store = Flipped(Decoupled(new StoreBundle))
    // val storeData = Decoupled(new StoreDataBundle)
  })

  // val storeQueue = Reg(Vec(32, new StoreBundle))
  // val storeHead = RegInit(0.U(5.W))
  // val storeTail = RegInit(0.U(5.W))
  // val storeCount = RegInit(0.U(5.W))

  // val storeDataQueue = Reg(Vec(32, new StoreDataBundle))
}