
import chisel3._
import chisel3.util._
import config.Configs._
import Control._

class LSFR extends Module {
  val io=IO(new Bundle {
    val OutTime=Output(UInt(8.W))
    val Seed=Input(UInt(8.W))
  })
  val delaytime=RegInit(io.Seed)
  val x8=delaytime(0)+delaytime(2)+delaytime(3)+delaytime(4)
  delaytime:=Cat(x8,delaytime(7,1))
  io.OutTime:=delaytime(7,0)
}

object RandomDelay {
  def apply[T <: Data](in: T,Seed: UInt): T = {
    val width=in.getWidth
    val delay=RegInit(0.U(8.W)) // 延迟周期寄存器，初始值为0
    val data =RegInit(0.U.asTypeOf(in)) // 数据寄存器，初始值为0

    val LSFR=Module(new LSFR)
    require(Seed.getWidth <= 8, "Seed length cannot exceed 4")
    LSFR.io.Seed:=Seed
    when(delay===0.U) {
      data:=in // 当延迟周期为0时，将输入信号赋值给数据寄存器
      delay:=LSFR.io.OutTime // 生成1到delayMax之间的随机延迟周期
    }.otherwise {
      data:=0.U
      delay:=delay-1.U // 延迟周期减1
    }
    data
  }
}