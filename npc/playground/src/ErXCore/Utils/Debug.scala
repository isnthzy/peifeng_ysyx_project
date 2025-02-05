package ErXCore

import chisel3._
// import ErXCore.LogLevel.LogLevel

// object LogLevel extends Enumeration {
//   type LogLevel = Value

//   val ALL   = Value(0, "ALL  ")
//   val DEBUG = Value("DEBUG")
//   val INFO  = Value("INFO ")
//   val WARN  = Value("WARN ")
//   val ERROR = Value("ERROR")
//   val OFF   = Value("OFF  ")
// }

// object LogUtil {

//   def control(): (Bool, UInt) = {
//     val control = LogPerfControl()
//     (control.logEnable, control.timer)
//   }

//   def apply(debugLevel: LogLevel)
//            (prefix: Boolean, cond: Bool, pable: Printable)
//            (implicit name: String): Any = {
//     if (NutCoreConfig().EnableDebug){
//       val c = control()
//       val commonInfo = p"[${c._2}] $name: "
//       when (cond && c._1) {
//         if(prefix) printf(commonInfo)
//         printf(pable)
//       }
//     } 
//   }
// }

object dontTouchUtil {
  /**
    * 包裹传入的信号，根据 GenerateParams 参数决定是否调用 dontTouch。
    *
    * @param signal 待处理的 Chisel Data 对象
    * @tparam T Data 类型
    * @return 如果 GenerateParams 为 true，则返回 dontTouch 后的信号，否则返回原信号
    */
  def apply[T <: Data](signal: T): T = {
    if (GenerateParams.getParam("VERILATOR_SIM").asInstanceOf[Boolean]) {
      dontTouch(signal)
    } else {
      signal
    }
  }
}