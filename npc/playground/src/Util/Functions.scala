package Util
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

object Mux1hMap{ //便于把宏转换成Mux1h的索引
  def apply(rd_addr: UInt, Map: Map[UInt, UInt]): UInt = {
    Mux1H(Map.map { case (addr, csr) => (rd_addr === addr) -> csr })
  }
}

object Mux1hDefMap{ //便于把宏转换成Mux1h的索引
  def apply(rd_addr: UInt, Map: Map[String, UInt]): UInt = {
    val UIntMap=Map.map{
      case(c,v)=>
        val addr= s"b${c}".U
        addr -> v
    }
    Mux1hMap(rd_addr,UIntMap)
  }
}

object SDEF{ //将string定义的宏转换为uint类型便于使用
  def apply(s: String) = {
    val bnum="b"+s
    bnum.U
  }
}//String def -> uint def

object LYDebugLog{ //将string定义的宏转换为uint类型便于使用
  def apply(s: String) = {
    if(CoreConfig.GenerateParams.getParam("VERILATOR_SIM").asInstanceOf[Boolean]){
      println(s)
    }
  }
}//String def -> uint def
