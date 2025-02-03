package ErXCore

import chisel3._
import chisel3.util._
import DecodeSignal._

class InstIO extends ErXCoreBundle {
  val inst = Output(UInt(32.W))
  val pc   = Output(UInt(XLEN.W))
}


class CtrlFlowIO extends ErXCoreBundle {
  val pc = Output(UInt(XLEN.W))
  val imm    = Output(UInt(XLEN.W))

  //useful to debug
}

class CtrlSignalIO extends ErXCoreBundle {
  val src1Type = Output(UInt(A_XXX.length.W)) 
  val src2Type = Output(UInt(B_XXX.length.W)) 
  val fuType = Output(UInt(FU_ALU.length.W)) 
  val aluOp  = Output(UInt(ALU_XXX.length.W)) 
  val brType = Output(UInt(BR_XXX.length.W))
  val lsType = Output(UInt(LS_XXX.length.W))
  val csrOp  = Output(UInt(CSR_XXXX.length.W))
  val rfWen  = Output(Bool())
  val rfSrc1 = Output(UInt(5.W))
  val rfSrc2 = Output(UInt(5.W))
  val rfDest = Output(UInt(5.W))
}

class MicroOpIO extends ErXCoreBundle {
  val cf = new CtrlFlowIO
  val cs = new CtrlSignalIO
}

class PrfFlowIO extends ErXCoreBundle {
  val prfSrc1 = Output(UInt(log2Up(PrfSize).W))
  val prfSrc2 = Output(UInt(log2Up(PrfSize).W))
  val pprfDst = Output(UInt(log2Up(PrfSize).W))
  val prfDst  = Output(UInt(log2Up(PrfSize).W))
}

class RenameIO extends MicroOpIO{
  val pf = new PrfFlowIO
}


class RenameFromExecuteUpdate(updSize: Int) extends ErXCoreBundle {
  val upd = Vec(updSize, new Bundle {
    val wen = Bool()
    val prfDst = UInt(log2Up(PrfSize).W)
  })
}

class RenameFromCommitUpdate(updSize: Int) extends ErXCoreBundle {
  val upd = Vec(updSize, new Bundle {
    val wen = Bool()
    val prfDst = UInt(log2Up(PrfSize).W)
    val freePrfDst = UInt(log2Up(PrfSize).W)
    val rfDst = UInt(5.W)
  })
  val recover = Bool()
}

class ROBFromExecuteUpdate(updSize: Int) extends ErXCoreBundle {
  val upd = Vec(updSize, new Bundle {
    val en = Bool()
    val robIdx = UInt(log2Up(RobEntries).W)
  })
}

class RSFromRename (updSize: Int = 1) extends ErXCoreBundle {
  val upd = Vec(updSize, new Bundle {
    
  })
  val availList = Vec(PrfSize, Bool())
}

class RSFromROB (updSize: Int = 1) extends ErXCoreBundle {
  val upd = Vec(updSize, new Bundle {
    
  })
  val robAge = Vec(RobWidth, UInt(RobAgeWidth.W))
}

class ROBFromExecute (updSize: Int = 1)  extends ErXCoreBundle {
  val upd = Vec(updSize, new Bundle {
    val br = new BranchBundle
  })
  
}

class FrontFromBack extends ErXCoreBundle {
  val flush = Bool()
  val tk = new BranchBundle
}

class StoreQueueFromROB (updSize: Int = 1) extends ErXCoreBundle {
  val upd = Vec(updSize, new Bundle {

  })
  val doDeq = Bool()
}

class ROBCommitIO extends  ErXCoreBundle {
  
}

class PrfReadFromCommit (updSize: Int = 1) extends ErXCoreBundle {
  val upd = Vec(updSize, new Bundle {
    val rfWen = Bool()
    val rfDst = UInt(log2Up(PrfSize).W)
    val rdData = UInt(XLEN.W)
  })
}

class IssueIO extends ErXCoreBundle{
  val cf = new CtrlFlowIO
  val cs = new CtrlSignalIO
  val pf = new PrfFlowIO
  val data = new Bundle {
    val src1 = Output(UInt(XLEN.W))
    val src2 = Output(UInt(XLEN.W))
  }
}

class BranchBundle extends ErXCoreBundle {
  val taken = Bool()
  val target = UInt(XLEN.W)
}


class CommitIO extends ErXCoreBundle{
  val target = Output(UInt(XLEN.W)) 
  // val cs = new CtrlSignalIO
  // val pf = new PrfFlowIO
  // val data = new Bundle {
  //   val rs1 = Output(UInt(XLEN.W))
  //   val rs2 = Output(UInt(XLEN.W))
  // }

}

class PipeExecuteOut extends ErXCoreBundle {
  val result = UInt(XLEN.W)
}

//NOTE: Frontend
class CsrEntriesBundle extends ErXCoreBundle{
  val mepc=UInt(XLEN.W)
  val mtvec=UInt(XLEN.W)
}

class PfExcpTypeBundle extends ErXCoreBundle{
  val iam=UInt(1.W)
}

class IfExcpTypeBundle extends ErXCoreBundle{
  val num=new PfExcpTypeBundle()
  val iaf=UInt(1.W)
  val ipf=UInt(1.W)
}

class BranchTakeBundle extends ErXCoreBundle{
  val taken=Bool()
  val target=UInt(XLEN.W)
}

class PipelineFlushsBundle extends ErXCoreBundle{
  val refetch=Bool()
  val excp=Bool()
  val xret=Bool()
}

class Pf2IfBusBundle extends ErXCoreBundle{
  val excpEn=Bool()
  val excpType=new PfExcpTypeBundle()
  val pc=UInt(XLEN.W)
}

class Pf4IdBusBundle extends ErXCoreBundle{
  val brJump=new BranchTakeBundle()
}

class Pf4ExBusBundle extends ErXCoreBundle{
  val brCond=new BranchTakeBundle()
  val fencei=Bool()
}

class Pf4LsBusBundle extends ErXCoreBundle{
  val flush=new PipelineFlushsBundle()
  val refetchPC=UInt(XLEN.W)
}

class If4IdBusBundle extends ErXCoreBundle{
  val flush=Bool()
}

class If4ExBusBundle extends ErXCoreBundle{
  val flush=Bool()
}

class If4LsBusBundle extends ErXCoreBundle{
  val flush=Bool()
}

class If2IdBusBundle extends ErXCoreBundle{
  val perfMode=Bool()

  val excpEn=Bool()
  val excpType=new IfExcpTypeBundle()
  val pc=UInt(XLEN.W)
  val inst=UInt(XLEN.W)
}
