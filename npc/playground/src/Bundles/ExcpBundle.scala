package Bundles

import chisel3._
import chisel3.util._
import CoreConfig.Configs._

object ECODE{ //简化写法，按照手册英文首字母简写
  val IAM= 0.U(6.W) //Instruction address misaligned
  val IAF= 1.U(6.W) //Instruction access fault
  val INE= 2.U(6.W) //Illegal instruction
  val BKP= 3.U(6.W) //Breakpoint
  val LAM= 4.U(6.W) //Load address misaligned
  val LAF= 5.U(6.W) //Load access fault
  val SAM= 6.U(6.W) //Store/AMO address misaligned
  val SAF= 7.U(6.W) //Store/AMO  access fault
  val ECU= 8.U(6.W) //Environment call from U-mode
  val ECS= 9.U(6.W) //Environment call from S-mode
  val ECM=11.U(6.W) //Environment call from M-mode
  val IPF=12.U(6.W) //Instruction page fault
  val LPF=13.U(6.W) //Load page fault
  val SPF=15.U(6.W) //Store/AMO page fault
}

class PfExcpTypeBundle extends Bundle{
  val iam=UInt(1.W)
}

class IfExcpTypeBundle extends Bundle{
  val num=new PfExcpTypeBundle()
  val iaf=UInt(1.W)
  val ipf=UInt(1.W)
}

class IdExcpTypeBundle extends Bundle{
  val num=new IfExcpTypeBundle()
  val ine=UInt(1.W)
  val bkp=UInt(1.W)
  val ecu=UInt(1.W)
  val ecs=UInt(1.W)
  val ecm=UInt(1.W)
}

class ExExcpTypeBundle extends Bundle{
  val num=new IdExcpTypeBundle()
  val lam=UInt(1.W)
  val sam=UInt(1.W)
}

class LsExcpTypeBundle extends Bundle{
  val laf=UInt(1.W)
  val saf=UInt(1.W)
  val lpf=UInt(1.W)
  val spf=UInt(1.W)
}

class ExcpTypeBundle extends Bundle{
  val spf=UInt(1.W)
  val lpf=UInt(1.W)
  val ipf=UInt(1.W)
  val ecm=UInt(1.W)
  val ecs=UInt(1.W)
  val ecu=UInt(1.W)
  val saf=UInt(1.W)
  val sam=UInt(1.W)
  val laf=UInt(1.W)
  val lam=UInt(1.W)
  val bkp=UInt(1.W)
  val ine=UInt(1.W)
  val iaf=UInt(1.W)
  val iam=UInt(1.W)
}//从下到上从0开始逐渐递增

class ExcpResultBundle extends Bundle{
  val ecode=UInt(6.W)
  val vaError=Bool()
  val vaBadAddr=UInt(ADDR_WIDTH.W)
} 