package Bundles

import chisel3._
import chisel3.util._  
import CoreConfig.Configs._

class Ls2CsrBundle extends Bundle{
  val wen=Bool()
  val wrAddr=UInt(12.W)
  val wrData=UInt(DATA_WIDTH.W)
  val excpFlush=Bool()
  val mretFlush=Bool()
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
  val interpt=UInt(1.W)
  val ecode  =UInt((ADDR_WIDTH-1).W)
}

class CsrStatusBundle extends Bundle{
  //32位
  val sd=UInt(1.W)
  val wpri30_23=UInt(8.W)
  val todo22_13=UInt(10.W)
  val mpp   =UInt(2.W)
  val vs    =UInt(2.W)
  val spp   =UInt(1.W)
  val mpie  =UInt(1.W)
  val ube   =UInt(1.W)
  val spie  =UInt(1.W)
  val wpri_4=UInt(1.W)
  val mie   =UInt(1.W)
  val wpri_2=UInt(1.W)
  val sie=UInt(1.W)
  val wpri_0=UInt(1.W)
}

class CsrXtvecBundle extends Bundle{
  val base=UInt((ADDR_WIDTH-2).W)
  val mode=UInt(2.W)
}

