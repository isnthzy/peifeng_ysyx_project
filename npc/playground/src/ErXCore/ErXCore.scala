package ErXCore

import chisel3._
import chisel3.util._

trait HasErXCoreParameter {
  val XLEN = 32
  //
  val DecodeWidth = 2
  val PrfSize = 64
  val IssueWidth  = 3
  val ExecuteWidth = 3
  val CommitWidth  = 2

  val RobWidth = DecodeWidth
  val RobSize  = 8
  val RobEntries = RobWidth * RobSize
  val RobIdxWidth = log2Up(RobEntries)
  val RobAgeWidth = RobIdxWidth + 1 //Age massge use to issue select!!!
  val RetireWidth = 2
}

trait HasErXCoreConst extends HasErXCoreParameter {
}

trait HasErXCoreLog { this: RawModule =>
  implicit val moduleName: String = this.name
}

abstract class ErXCoreModule extends Module with HasErXCoreParameter with HasErXCoreConst with HasErXCoreLog
abstract class ErXCoreBundle extends Bundle with HasErXCoreParameter with HasErXCoreConst


