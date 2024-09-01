package Bundles

import chisel3._
import chisel3.util._  
import config.Configs._

class Ls2CsrBundle extends Bundle{
  val mepcIn=UInt(DATA_WIDTH.W)
  val wen=Bool()
  val wrAddr=UInt(12.W)
  val wrData=UInt(DATA_WIDTH.W)
}

class PipeLine4CsrBundle extends Bundle{
  val rdAddr=Input(UInt(12.W))
  val rdData=Output(UInt(DATA_WIDTH.W))
}

class CsrEntriesBundle extends Bundle{
  val mepc=UInt(DATA_WIDTH.W)
}