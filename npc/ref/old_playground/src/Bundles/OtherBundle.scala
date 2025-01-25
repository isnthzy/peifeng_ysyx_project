package Bundles
import chisel3._
import chisel3.util._
import CoreConfig.Configs._

class RegFileForwardBundle extends Bundle{
  val wen=Bool()
  val waddr=UInt(5.W)
  val wdata=UInt(DATA_WIDTH.W)
}

class BranchTakeBundle extends Bundle{
  val taken=Bool()
  val target=UInt(ADDR_WIDTH.W)
}

class PipelineFlushsBundle extends Bundle{
  val refetch=Bool()
  val excp=Bool()
  val xret=Bool()
}