package ErXCore
import chisel3._
import chisel3.util._

object GenerateParams {
  private var coreMode = "soc"
  private var usePerf = false

  private var params: Map[String, Any] = Map(
    "RV32E"         -> true,
    "VERILATOR_SIM" -> true,
    "PERF"          -> false,
    "YOSYS_MODE"    -> false,
    "SOC_MODE"      -> true
  )
  def setParams(mode: String, perf: Boolean): Unit = {
    coreMode = mode
    usePerf  = perf
    println(s"set mode: $coreMode perf: $usePerf")
    updateParams()
  }
  private def updateParams(): Unit = {
    params = coreMode match {
      case "npc" =>
        params ++ Map(
          "SOC_MODE" -> false, 
          "PERF" -> usePerf,
        ) //NOTE:scala中 ++ 添加/更新键值对
      case "yosys" =>
        params ++ Map(
          "SOC_MODE" -> true,
          "PERF" -> false,
          "VERILATOR_SIM" -> false,
          "YOSYS_MODE" -> true,
        )
      case _ =>
        params ++ Map(
          "PERF" -> usePerf,
        )
    }
  }
  def getParam(key: String): Any = params(key)
}


object ISAConfig{
  def RV32E = true
  def SOC_MODE = true //NOTE:true时生成soc电路，false生成npc电路
}

object GenCtrl{
  def VERILATOR_SIM = true
  def PERF = true
  def YOSYS_MODE = false
} 
/*
NOTE:如果需要yosys评估，需要关闭VERILATOR_SIM(difftest)和(PERF)性能测试
      开启YOSYS_MODE后，访存会把DPIC操作会更改成对memory的操作
     */

trait HasErXCoreParameter {
  val XLEN = 32
  //
  val InstBuffSize = 8
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
abstract class ErXCoreBlackBox extends BlackBox with HasErXCoreParameter with HasErXCoreConst with HasErXCacheConfig