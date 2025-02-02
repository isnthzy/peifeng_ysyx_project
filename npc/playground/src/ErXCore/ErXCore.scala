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

trait HasErXCacheConfig{
  def LINE_WIDTH = 128
  def LINE_WORD_NUM = (LINE_WIDTH / 32)

  def TAG_WIDTH = 26
  def INDEX_WIDTH  = 32 - TAG_WIDTH - OFFSET_WIDTH
  def OFFSET_WIDTH = log2Ceil(LINE_WORD_NUM) + 2
  
  def WAY_NUM_I = 2
  def USE_LRU = false
}

trait HasErXCoreConst extends HasErXCoreParameter {
  def INST_NOP = "h00000013".U(XLEN.W)
  def SOC_START_ADDR = "h30000000".U(XLEN.W)
  def NPC_START_ADDR = "h80000000".U(XLEN.W)
}

trait HasErXCoreLog { this: RawModule =>
  implicit val moduleName: String = this.name
}

abstract class ErXCoreModule extends Module with HasErXCoreParameter with HasErXCoreConst with HasErXCoreLog with HasErXCacheConfig
abstract class ErXCoreBundle extends Bundle with HasErXCoreParameter with HasErXCoreConst with HasErXCacheConfig


