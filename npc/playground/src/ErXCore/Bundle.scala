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


class RenameFromExecuteUpdate(updateSize: Int) extends ErXCoreBundle {
  val update = Vec(updateSize, new Bundle {
    val wen = Input(Bool())
    val prfDst = Input(UInt(log2Up(PrfSize).W))
  })
}

class RenameFromCommitUpdate(updateSize: Int) extends ErXCoreBundle {
  val recover = Input(Bool())
  val update  = Vec(updateSize, new Bundle {
    val wen = Input(Bool())
    val prfDst = Input(UInt(log2Up(PrfSize).W))
    val freePrfDst = Input(UInt(log2Up(PrfSize).W))
    val rfDst = Input(UInt(5.W))
  })
}

class ROBFromExecuteUpdate(updateSize: Int) extends ErXCoreBundle {
  val update = Vec(updateSize, new Bundle {
    val en = Input(Bool())
    val robIdx = Input(UInt(log2Up(RobEntries).W))
  })
}

class RSFromRename (updSize: Int = 1) extends ErXCoreBundle {
  val upd = Vec(updSize, new Bundle {
    
  })
  val availList = Input(Vec(PrfSize, Bool()))
}

class RSFromROB (updSize: Int = 1) extends ErXCoreBundle {
  val upd = Vec(updSize, new Bundle {
    
  })
  val robAge = Input(Vec(RobEntries, UInt(log2Up(RobEntries).W)))
}

class ROBCommitIO extends  ErXCoreBundle {
  
}

class PrfReadFromCommit (updSize: Int = 1) extends ErXCoreBundle {
  val upd = Vec(updSize, new Bundle {
    val rfWen = Input(Bool())
    val rfDst = Input(UInt(log2Up(PrfSize).W))
    val rdData = Input(UInt(XLEN.W))
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

class ROBFromExecute (updSize: Int = 1)  extends ErXCoreBundle {
  val upd = Vec(updSize, new Bundle {
    val br = new BranchBundle
  })
  
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