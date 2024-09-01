package Bundles
import chisel3._
import chisel3.util._
import config.Configs._
import FuncUnit.Control._


class Pf2IfBusBundle extends Bundle{
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
  val pc=UInt(ADDR_WIDTH.W)
  val inst=UInt(DATA_WIDTH.W)
}

class Id2ExBusBundle extends Bundle{
  val pc=UInt(ADDR_WIDTH.W)
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
  val csrWen=Bool()
  val csrWrAddr=UInt(12.W)
  val csrWrData=UInt(32.W)
  val pc=UInt(ADDR_WIDTH.W)
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
  val rd=UInt(5.W)
  val result=UInt(DATA_WIDTH.W)
  val rfWen =Bool()
}
