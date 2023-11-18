import circt.stage._

object TopMain extends App {
  def top = new SimTop()
  val ChiselStageOptions=Seq(
    chisel3.stage.ChiselGeneratorAnnotation(() => top),
    CIRCTTargetAnnotation(CIRCTTarget.Verilog)
  )
  // (new ChiselStage).execute(args, generator :+ CIRCTTargetAnnotation(CIRCTTarget.Verilog))

  val firtoolOptions=Seq(
    FirtoolOption(
      "--lowering-options=disallowLocalVariables,disallowPackedArrays,locationInfoStyle=wrapInAtSquareBracket"
    ),
    FirtoolOption("--split-verilog"),
    FirtoolOption("-o-build/sv-gen"),
    FirtoolOption("--disable-all-randomization")
  )
  val executeOptions=ChiselStageOptions++firtoolOptions
  (new ChiselStage).execute(args,executeOptions)
}
