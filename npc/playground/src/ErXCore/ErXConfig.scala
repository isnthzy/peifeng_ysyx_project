package ErXCore


// object GenerateParams {
//   private var coreMode = "soc"
//   private var usePerf = false

//   private var params: Map[String, Any] = Map(
//     "RV32E"         -> true,
//     "VERILATOR_SIM" -> true,
//     "PERF"          -> false,
//     "YOSYS_MODE"    -> false,
//     "SOC_MODE"      -> true,
//     ""
//   )
//   def setParams(mode: String, perf: Boolean): Unit = {
//     coreMode = mode
//     usePerf  = perf
//     println(s"set mode: $coreMode perf: $usePerf")
//     updateParams()
//   }
//   private def updateParams(): Unit = {
//     params = coreMode match {
//       case "npc" =>
//         params ++ Map(
//           "SOC_MODE" -> false, 
//           "PERF" -> usePerf,
//         ) //NOTE:scala中 ++ 添加/更新键值对
//       case "yosys" =>
//         params ++ Map(
//           "SOC_MODE" -> true,
//           "PERF" -> false,
//           "VERILATOR_SIM" -> false,
//           "YOSYS_MODE" -> true,
//         )
//       case _ =>
//         params ++ Map(
//           "PERF" -> usePerf,
//         )
//     }
//   }
//   def getParam(key: String): Any = params(key)
// }

