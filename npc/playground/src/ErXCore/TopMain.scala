import circt.stage._
import scala.annotation.tailrec
import ErXCore.GenerateParams
import ErXCore.SimTop

abstract class GenParamsApp extends App {
  case class GenParams( //NOTE:搬运来源：https://github.com/OpenXiangShan/difftest
    mode: Option[String] = None,
    perf: Boolean = false, //Option有None选项，perf不用option默认false
  )
  def parseArgs(args: Array[String]): (GenParams) = {
    val default = new GenParams()
    @tailrec
    def nextOption(param: GenParams, list: List[String]): GenParams = {
      list match {
        case Nil                            => param
        case "--mode" :: str :: tail        => nextOption(param.copy(mode = Some(str)), tail)
        case "--perf" :: tail               => nextOption(param.copy(perf = true), tail)
        case option :: tail =>
          nextOption(param, tail)
      }
    }
    nextOption(default, args.toList)
  }
  val param = parseArgs(args)
  val gen = if (param.mode.isDefined) { 
    GenerateParams.setParams(param.mode.get,param.perf)
    () => new SimTop
  } else {
    GenerateParams.setParams("soc",param.perf)
    () => new SimTop
  }
} 


object TopMain extends GenParamsApp {
  val ChiselStageOptions=Seq(
    chisel3.stage.ChiselGeneratorAnnotation(gen),
    CIRCTTargetAnnotation(CIRCTTarget.SystemVerilog)
  )
  // (new ChiselStage).execute(args, generator :+ CIRCTTargetAnnotation(CIRCTTarget.Verilog))
  /*  firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info",
        "--lowering-options=disallowLocalVariables,disallowPackedArrays,locationInfoStyle=wrapInAtSquareBracket,noAlwaysComb")*/
  val firtoolOptions=Seq(
    FirtoolOption(
      "--lowering-options=disallowLocalVariables,locationInfoStyle=wrapInAtSquareBracket"
    ), 
    // FirtoolOption("--lowering-options=disallowPackedArrays"),
    // FirtoolOption("--preserve-aggregate=vec"), 
    //NOTE:前者和后者不能同时开启
    FirtoolOption("--split-verilog"), //分离生成的verilog文件
    FirtoolOption("-o=vsrc"), //设置分离后verilog文件的路径
    FirtoolOption("--disable-all-randomization") //禁止随机数
  )
  val executeOptions=ChiselStageOptions++firtoolOptions
  val executeArgs=Array("-td","build")
  // (new ChiselStage).execute(args,executeOptions)
  (new ChiselStage).execute(executeArgs,executeOptions)
}
