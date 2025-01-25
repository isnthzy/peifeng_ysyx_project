package ErXCore

import chisel3._
import chisel3.util._

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
