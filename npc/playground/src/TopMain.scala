import circt.stage._
import scala.annotation.tailrec

abstract class GenParamsApp extends App {
  case class GenParams(
    mode: Option[String] = None,
  )
  def parseArgs(args: Array[String]): (GenParams, Array[String]) = {
    val default = new GenParams()
    @tailrec
    def nextOption(param: GenParams, list: List[String]): GenParams = {
      list match {
        case Nil                            => param
        case "--mode" :: str :: tail        => nextOption(param.copy(mode = Some(str)), tail)
        case option :: tail =>
          nextOption(param, tail)
      }
    }
    nextOption(default, args.toList)
  }
  val param = parseArgs(args)
  val gen = if (param.profile.isDefined) { () =>
    new SimTop(param.profile.get)
  } else { () =>
    new SimTop("soc")
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
      "--lowering-options=disallowLocalVariables,disallowPackedArrays,locationInfoStyle=wrapInAtSquareBracket"
    ), 
    // FirtoolOption("---lowering-options=disallowPackedArrays"),
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
