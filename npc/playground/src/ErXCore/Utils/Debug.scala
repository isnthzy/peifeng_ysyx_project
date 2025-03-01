package ErXCore

import chisel3._
import ErXCore.LogLevel.LogLevel

object LogLevel extends Enumeration {
  type LogLevel = Value

  val ALL   = Value(0, "ALL  ")
  val DEBUG = Value("DEBUG")
  val INFO  = Value("INFO ")
  val WARN  = Value("WARN ")
  val ERROR = Value("ERROR")
  val OFF   = Value("OFF  ")
}

object LogUtil {
  def apply(debugLevel: LogLevel)
           (prefix: Boolean, cond: Bool, pable: Printable)
           (implicit name: String): Any = {
    if (ErXCoreParameter().EnableDebug) {
      val commonInfo = p"$name: "
      when(cond) {
        if (prefix) printf(commonInfo)
        printf(pable)
      }
    }
  }
}

sealed abstract class LogHelper(val logLevel: LogLevel) {

  def apply(cond: Bool, fmt: String, data: Bits*)(implicit name: String): Any =
    apply(cond, Printable.pack(fmt, data: _*))
  def apply(cond: Bool, pable: Printable)(implicit name: String): Any = apply(true, cond, pable)
  def apply(fmt: String, data: Bits*)(implicit name: String): Any =
    apply(true.B, Printable.pack(fmt, data: _*))
  def apply(pable: Printable)(implicit name: String): Any = apply(true.B, pable)
  def apply(prefix: Boolean, fmt: String, data: Bits*)(implicit name: String): Any =
    apply(prefix, true.B, Printable.pack(fmt, data: _*))
  def apply(prefix: Boolean, pable: Printable)(implicit name: String): Any = apply(prefix, true.B, pable)
  def apply(prefix: Boolean, cond: Bool, fmt: String, data: Bits*)(implicit name: String): Any =
    apply(prefix, cond, Printable.pack(fmt, data: _*))
  def apply(prefix: Boolean, cond: Bool, pable: Printable)(implicit name: String): Any =
    LogUtil(logLevel)(prefix, cond, pable)

  // NOOP/NutShell style debug
  def apply(flag: Boolean = ErXCoreParameter().EnableDebug, cond: Bool = true.B)(body: => Unit): Any = {
    if (flag) { when(cond) { body } }
  }
}

object Debug extends LogHelper(LogLevel.DEBUG)
object Info  extends LogHelper(LogLevel.INFO)
object Warn  extends LogHelper(LogLevel.WARN)
object Error extends LogHelper(LogLevel.ERROR)

object ShowType {
  def apply[T: Manifest](t: T) = println(manifest[T])
}

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