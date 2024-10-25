package Bundles
import chisel3._
import chisel3.util._

import CoreConfig.Configs.{ADDR_WIDTH,DATA_WIDTH}
import FuncUnit.Control._
import Difftest._


class Pf2IfBusBundle extends Bundle{
  val excpEn=Bool()
  val excpType=new PfExcpTypeBundle()
  val pc=UInt(ADDR_WIDTH.W)
}

class Pf4IdBusBundle extends Bundle{
  val brJump=new BranchTakeBundle()
}

class Pf4ExBusBundle extends Bundle{
  val brCond=new BranchTakeBundle()
}

class Pf4LsBusBundle extends Bundle{
  val flush=new PipelineFlushsBundle()
  val refetchPC=UInt(32.W)
}

class If4IdBusBundle extends Bundle{
  val flush=Bool()
}

class If4ExBusBundle extends Bundle{
  val flush=Bool()
}

class If4LsBusBundle extends Bundle{
  val flush=Bool()
}

class If2IdBusBundle extends Bundle{
  val perfMode=Bool()

  val excpEn=Bool()
  val excpType=new IfExcpTypeBundle()
  val pc=UInt(ADDR_WIDTH.W)
  val inst=UInt(DATA_WIDTH.W)
}

class Id2ExBusBundle extends Bundle{
  val perfMode=Bool()

  val excpEn=Bool()
  val excpType=new IdExcpTypeBundle()
  val pc=UInt(ADDR_WIDTH.W)
  val inst=UInt(32.W)
  val rd=UInt(5.W)
  val src1=UInt(DATA_WIDTH.W)
  val src2=UInt(DATA_WIDTH.W)
  val imm=UInt(ADDR_WIDTH.W)
  val aluOp=UInt(ALU_XXX.length.W)
  val csrWrAddr=UInt(12.W)
  val csrOp=UInt(CSR_XXXX.length.W)
  val brType=UInt(BR_XXX.length.W)
  val stType=UInt(ST_XXX.length.W)
  val ldType=UInt(LD_XXX.length.W)
  val wbSel=UInt(WB_ALU.length.W)
  val rfWen=Bool()
}

class Id4ExBusBundle extends Bundle{
  val flush=Bool()
  val dataUnReady=Bool()
  val rf=new RegFileForwardBundle()
}

class Id4LsBusBundle extends Bundle{
  val flush=Bool()
  val rf=new RegFileForwardBundle()
  val dataUnReady=Bool()
}

class Id4WbBusBundle extends Bundle{
  val rf=new RegFileForwardBundle()
}

class Ex2LsBusBundle extends Bundle{
  val perfMode=Bool()

  val diffLoad =new DiffLoadBundle()
  val diffStore=new DiffStoreBundle()

  val isDeviceSkip=Bool()
  val excpEn=Bool()
  val excpType=new ExExcpTypeBundle()
  val memBadAddr=UInt(ADDR_WIDTH.W)
  val isMret=Bool()
  val csrWen=Bool()
  val csrWrAddr=UInt(12.W)
  val csrWrData=UInt(32.W)
  val pc=UInt(ADDR_WIDTH.W)
  val inst=UInt(32.W)
  val rd=UInt(5.W)
  val result=UInt(DATA_WIDTH.W)
  val addrLow2Bit=UInt(2.W)
  val storeEn=Bool()
  val loadEn=Bool()
  val ldType=UInt(LD_XXX.length.W)
  val wbSel=UInt(WB_ALU.length.W)
  val rfWen =Bool()
}

class Ex4LsBusBundle extends Bundle{
  val flush=Bool()
}

class Ls2WbBusBundle extends Bundle{
  val perfMode=Bool()

  val diffLoad =new DiffLoadBundle()
  val diffStore=new DiffStoreBundle()
  val diffExcp =new DiffExcpBundle()

  val isDeviceSkip=Bool()
  val excpEn=Bool()
  val pc=UInt(ADDR_WIDTH.W)
  val inst=UInt(32.W)
  val rd=UInt(5.W)
  val result=UInt(DATA_WIDTH.W)
  val rfWen =Bool()
}
