package ErXCore

import chisel3._
import chisel3.util._


object ECODE{ //简化写法，按照手册英文首字母简写
  val IAM =  0.U(6.W) //Instruction address misaligned
  val IAF =  1.U(6.W) //Instruction access fault
  val INE =  2.U(6.W) //Illegal instruction
  val BKP =  3.U(6.W) //Breakpoint
  val LAM =  4.U(6.W) //Load address misaligned
  val LAF =  5.U(6.W) //Load access fault
  val SAM =  6.U(6.W) //Store/AMO address misaligned
  val SAF =  7.U(6.W) //Store/AMO  access fault
  val ECU =  8.U(6.W) //Environment call from U-mode
  val ECS =  9.U(6.W) //Environment call from S-mode
  val ECM = 11.U(6.W) //Environment call from M-mode
  val IPF = 12.U(6.W) //Instruction page fault
  val LPF = 13.U(6.W) //Load page fault
  val SPF = 15.U(6.W) //Store/AMO page fault
}

class ExcpTypeBundle extends ErXCoreBundle{
  val spf = Bool()
  val lpf = Bool()
  val ipf = Bool()
  val ecm = Bool()
  val ecs = Bool()
  val ecu = Bool()
  val saf = Bool()
  val sam = Bool()
  val laf = Bool()
  val lam = Bool()
  val bkp = Bool()
  val ine = Bool()
  val iaf = Bool()
  val iam = Bool()
}//从下到上从0开始逐渐递增

class ExcpResultBundle extends ErXCoreBundle{
  val ecode = UInt(6.W)
  val vaError = Bool()
  val vaBadAddr = UInt(XLEN.W)
} 