
import chisel3._
import chisel3.util._
import config.Configs._
import Control._
import java.awt.MouseInfo

class LSFR extends Module {
  val io=IO(new Bundle {
    val out=Output(UInt(4.W))
  })
  val data=RegInit("b0001".U(4.W))
  data:=Cat(data(2,0),data(3)^data(2))
}

object RandomDelay {
  def apply[T <: Data](in: T): T = {
    val width=in.getWidth
    val delay=RegInit(0.U(4.W)) // 延迟周期寄存器，初始值为0
    val data =RegInit(0.U.asTypeOf(in)) // 数据寄存器，初始值为0

    val LSFR=Module(new LSFR)
    when(delay===0.U) {
      data := in // 当延迟周期为0时，将输入信号赋值给数据寄存器
      delay := LSFR.io.out // 生成1到delayMax之间的随机延迟周期
    }.otherwise {
      delay := delay-1.U // 延迟周期减1
    }
    data
  }
}