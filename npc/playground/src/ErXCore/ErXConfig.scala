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
    "SOC_MODE"      -> true,
    "DEBUG"         -> false,
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
          "DEBUG" -> false,
        )
      case _ =>
        params ++ Map(
          "PERF" -> usePerf,
        )
    }
  }
  def getParam(key: String): Any = params(key)
}

trait HasErXCoreParameter {
  val EnableVerlatorSim = GenerateParams.getParam("VERILATOR_SIM").asInstanceOf[Boolean] 
  val UseRV32E = GenerateParams.getParam("RV32E").asInstanceOf[Boolean]
  val EnableDebug = GenerateParams.getParam("DEBUG").asInstanceOf[Boolean]
  //
  val XLEN = 32
  //
  val InstBuffSize = 8
  //
  val DecodeWidth = 2
  val IssueWidth  = 3
  val ExecuteWidth = 3
  val CommitWidth  = 2

  val RobWidth = DecodeWidth
  val RobSize  = 8
  val RobEntries = RobWidth * RobSize
  val RobIdxWidth = log2Up(RobEntries)
  val RobAgeWidth = RobIdxWidth + 1 //Age massge use to issue select!!!
  val RetireWidth = 2

  val ArfSize = if(UseRV32E) 16 else 32
  val PrfSize = ArfSize * 2
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

trait HasErXCoreLog { 
  this: RawModule =>
  implicit lazy val moduleName: String = this.name
}

abstract class ErXCoreModule extends Module with HasErXCoreParameter with HasErXCoreConst with HasErXCacheConfig with HasErXCoreLog
abstract class ErXCoreBundle extends Bundle with HasErXCoreParameter with HasErXCoreConst with HasErXCacheConfig
abstract class ErXCoreBlackBox extends BlackBox with HasErXCoreParameter with HasErXCoreConst with HasErXCacheConfig

case class ErXCoreParameter (
  EnableDebug: Boolean = GenerateParams.getParam("DEBUG").asInstanceOf[Boolean]
)