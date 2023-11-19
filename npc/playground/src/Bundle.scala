import chisel3._
import chisel3.util._  

class Inst extends Bundle{
  val immI=UInt(12.W)
  val immS=UInt(12.W)
  val immB=UInt(12.W)
  val immU=UInt(32.W)
  val immJ=UInt(20.W)
  val rs2 =UInt(5.W)
  val rs1 =UInt(5.W)
  val funct3=UInt(3.W)
  val rd  =UInt(5.W)
  val opcode=UInt(7.W)
}
class Isa extends Bundle{
  val addi=Bool()
  val ebreak=Bool()
}
class ImmType extends Bundle{
  val ImmIType=Bool()
  val ImmSType=Bool()
  val ImmBType=Bool()
  val ImmUType=Bool()
  val ImmJType=Bool()
}

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
    if(num_width<e_width){
      Cat(Fill(e_width-num_width,0.U),num(num_width-1,0))
    }else{
      num(num_width-1,0)
    }
  }
}