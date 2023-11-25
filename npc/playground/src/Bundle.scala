import chisel3._
import chisel3.dontTouch
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
class IsaR extends Bundle{
  val add  =Bool()
  val sub  =Bool()
  val sll  =Bool()
  val slt  =Bool()
  val sltu =Bool()
  val xor  =Bool()
  val srl  =Bool()
  val sra  =Bool()
  val or   =Bool()
  val and  =Bool()
}
class IsaI extends Bundle{
  val jalr =Bool()
  val lb   =Bool()
  val lh   =Bool()
  val lw   =Bool()
  val lbu  =Bool()
  val lhu  =Bool()
  val addi =Bool()
  val slti =Bool()
  val sltiu=Bool()
  val xori =Bool()
  val ori  =Bool()
  val andi =Bool()
  val slli =Bool()
  val srli =Bool()
  val srai =Bool()
  val ebreak=Bool()
}
class IsaS extends Bundle{
  val sb   =Bool()
  val sh   =Bool()
  val sw   =Bool()
}
class IsaB extends Bundle{
  val beq  =Bool()
  val bne  =Bool()
  val blt  =Bool()
  val bge  =Bool()
  val bltu =Bool()
  val bgeu =Bool()
}
class IsaU extends Bundle{ //jal指令放在u里
  val lui  =Bool()
  val auipc=Bool()
  val jal  =dontTouch(Bool()) //避免被优化
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