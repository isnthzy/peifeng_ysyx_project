package ErXCore

import chisel3._
import chisel3.util._

object Sext{ //有符号位宽扩展
  def apply(num:UInt,e_width:Int) = {
    val num_width=num.getWidth
    if(num_width<e_width){
      Cat(Fill(e_width-num_width,num(num_width-1)),num(num_width-1,0))
    }else{
      num(num_width-1,0)
    }
  }
}

object Zext{ //无符号位宽扩展
  def apply(num:UInt,e_width:Int) = {
    val num_width=num.getWidth
    if(num_width==1){
      Cat(Fill(e_width-num_width,0.U),num)
    }else if(num_width<e_width){
      Cat(Fill(e_width-num_width,0.U),num(num_width-1,0))
    }else{
      num(num_width-1,0)
    }
  }
}

object SDEF{ //将string定义的宏转换为uint类型便于使用
  def apply(s: String) = {
    val bnum="b"+s
    bnum.U
  }
}//String def -> uint def

object UIntUtils{
  implicit class UIntWithGetIdx(val x: UInt) extends AnyVal {
    def getIdx(idxWidth: Int): UInt = x(idxWidth - 1, 0)
  }
  implicit class UIntWithGetAge(val x: UInt) extends AnyVal {
    def getAge(ageIdx: Int): UInt = x(ageIdx - 1)
  }
}