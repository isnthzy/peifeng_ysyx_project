package Bundles

import chisel3._
import chisel3.util._  
import config.Configs._

class Ls2CsrBundle extends Bundle{
  val wen=Bool()
  val wrAddr=UInt(12.W)
  val wrData=UInt(DATA_WIDTH.W)
  val excpResult=new ExcpResultBundle()
}

class PipeLine4CsrBundle extends Bundle{
  val rdAddr=Input(UInt(12.W))
  val rdData=Output(UInt(DATA_WIDTH.W))
}

class CsrEntriesBundle extends Bundle{
  val mepc=UInt(ADDR_WIDTH.W)
  val mtvec=UInt(ADDR_WIDTH.W)
}

class CsrCauseBundle extends Bundle{
  val INTRPT=UInt(1.W)
  val ECODE =UInt((ADDR_WIDTH-1).W)
}

class CsrStatusBundle extends Bundle{
  
}

class CsrXtvecBundle extends Bundle{
  
}

class CsrXepcBundle extends Bundle{
  
}

