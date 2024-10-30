package Util

import chisel3._
import chisel3.util._
// import Control._

class LSFR extends Module {
  val io=IO(new Bundle {
    val Out=Output(UInt(8.W))
    val Seed=Input(UInt(8.W))
  })
  val randomtime=RegInit(io.Seed)
  val x8=randomtime(0)+randomtime(2)+randomtime(3)+randomtime(4)
  randomtime:=Cat(x8,randomtime(7,1))
  io.Out:=randomtime(3,0)
}

object RandomNum {
  def apply[T <: Data](Seed: UInt): UInt = {
    val LSFR=Module(new LSFR)
    require(Seed.getWidth <= 8, "Seed length cannot exceed 4")
    LSFR.io.Out
  }
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
      delay:=LSFR.io.Out // 生成1到delayMax之间的随机延迟周期
    }.otherwise {
      data:=0.U
      delay:=delay-1.U // 延迟周期减1
    }
    data
  }
}

object TimeDelay {
  def apply[T <: Data](in: T,DelayTime: Int): T = {
    val width=in.getWidth
    val data=RegInit(0.U.asTypeOf(in)) // 数据寄存器，初始值为0

    // 使用一个计数器来跟踪已过去的时钟周期
    val counter=RegInit(0.U(log2Ceil(DelayTime+1).W))
    // 当计数器达到延迟周期时，将输入数据传递到输出
    when (counter>=DelayTime.U) {
      counter:=0.U
      data:=in
    }.otherwise {
      data:=0.U 
    }
    // 每个时钟周期增加计数器
    when (counter<DelayTime.U) {
      counter:=counter+1.U
    }
    data
  }
}