package ErXCore

import chisel3._
import chisel3.util._

class Commit extends ErXCoreModule {
  val io = IO(new Bundle {
    val fw_dr = new RenameFromCommitUpdate(updSize = CommitWidth)
  })
}