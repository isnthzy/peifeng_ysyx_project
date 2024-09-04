package Bundles

import chisel3._
import chisel3.util._
import config.Configs._

class DiffInstrBundle extends Bundle{
  val valid=Bool()
  val pc=UInt(32.W)
  val instr=UInt(32.W)
  val skip=Bool()
  val wen=Bool()
  val wdest=UInt(8.W)
  val wdata=UInt(32.W)
  val csrRstat=Bool()
  val csrData=UInt(32.W)
}

class DiffExcpBundle extends Bundle{
  val excpValid=Bool()
  val isMret=Bool()
  val intrptNo=Bool()
  val cause =UInt(32.W)
  val exceptionPC=UInt(ADDR_WIDTH.W)
  val exceptionInst=UInt(32.W)
}

class DiffStoreBundle extends Bundle{
  val valid=UInt(8.W)
  val paddr=UInt(ADDR_WIDTH.W) //给未对齐的地址
  val vaddr=UInt(ADDR_WIDTH.W) //给未对齐的地址
  val data =UInt(DATA_WIDTH.W)
}

class DiffLoadBundle extends Bundle{
  val valid=UInt(8.W)
  val paddr=UInt(ADDR_WIDTH.W) //给未对齐的地址
  val vaddr=UInt(ADDR_WIDTH.W) //给未对齐的地址
  val data =UInt(DATA_WIDTH.W)
}

class DiffCsrRegBundle extends Bundle{
  val mstatus=UInt(DATA_WIDTH.W)
  val mtvec  =UInt(DATA_WIDTH.W)
  val mepc   =UInt(DATA_WIDTH.W)
  val mcause =UInt(DATA_WIDTH.W)
}
