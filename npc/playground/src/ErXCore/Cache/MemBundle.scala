package ErXCore

import chisel3._
import chisel3.util._

class SimpleReqIO extends ErXCoreBundle{
  val addr = Output(UInt(XLEN.W))
  val size = Output(UInt(3.W))
  val wen   = Output(Bool()) //read:0 write:1
  val wmask = Output(UInt((XLEN/8).W))
  val wdata = Output(UInt(XLEN.W))
}

class SimpleRespIO extends ErXCoreBundle{
  val data  = Output(UInt(XLEN.W))
}


class SimpleMemIO extends ErXCoreBundle{
  //设计思路：
  //Icache通过SimpleIO接口发送到Axi仲裁器
  //lsu把信号转换成SimpleIO，不使用仲裁器
  val req  = DecoupledIO(new SimpleReqIO())
  val resp = Flipped(DecoupledIO(new SimpleRespIO()))
}

class Core2AxiReadIO extends ErXCoreBundle{
  val req  = Output(Bool())
  val addr = Output(UInt(XLEN.W))
  val size = Output(UInt(3.W))
  val addrOk = Input(Bool())
}

class Core2AxiRespondIO extends ErXCoreBundle{
  val dataOk= Input(Bool())
  val data  = Input(UInt(XLEN.W))
}

class AxiCacheReadIO extends ErXCoreBundle{
  val stype = Output(UInt(3.W))
  val addr = Output(UInt(XLEN.W))
}

class AxiCacheWriteIO extends ErXCoreBundle{
  val stype = Output(UInt(3.W))
  val addr = Output(UInt(XLEN.W))
  val strb = Output(UInt((XLEN/8).W))
  val data = Output(UInt(XLEN.W))
}

class AxiCacheReadReturnIO extends ErXCoreBundle{
  val last  = Input(Bool())
  val resp  = Input(UInt(2.W))
  val data  = Input(UInt(XLEN.W))
}

class AxiCacheWriteReturnIO extends ErXCoreBundle{
  val last  = Input(Bool())
  val resp  = Input(UInt(2.W))
}

class AxiCacheIO extends ErXCoreBundle{
  val rd  = DecoupledIO(new AxiCacheReadIO())
  val wr  = DecoupledIO(new AxiCacheWriteIO())
  val rret = Flipped(DecoupledIO(new AxiCacheReadReturnIO()))
}

