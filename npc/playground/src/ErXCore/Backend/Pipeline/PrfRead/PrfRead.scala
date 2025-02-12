package ErXCore

import chisel3._
import chisel3.util._
import chisel3.util.experimental._
import DecodeSignal._

class PrfRead extends ErXCoreModule{
  val io = IO(new Bundle{
    val in = Vec(IssueWidth,Flipped(DecoupledIO(new RenameIO)))
    val from_ex = Input(new PrfReadFromExecute(updSize = IssueWidth))
    val to_ex = Vec(IssueWidth,DecoupledIO(new IssueIO))
    val csrReadIO = Flipped(new ReadCsrIO)
  })
  
  for(i <- 0 until IssueWidth){
    io.in(i).ready := io.to_ex(i).ready
  }

  val prf = RegInit(VecInit(Seq.fill(PrfSize)(0.U(XLEN.W))))

  for(i <- 0 until IssueWidth){
    when(io.from_ex.upd(i).rfWen && io.from_ex.upd(i).prfDst =/= 0.U){
      prf(io.from_ex.upd(i).prfDst) := io.from_ex.upd(i).rdData
    }
  } 

  for(i <- 0 until IssueWidth){
    io.to_ex(i).bits.cs := io.in(i).bits.cs
    io.to_ex(i).bits.pf := io.in(i).bits.pf
    io.to_ex(i).bits.cf := io.in(i).bits.cf
    io.to_ex(i).bits.robIdx := io.in(i).bits.robIdx
    var rsData1 = prf(io.in(i).bits.pf.prfSrc1)
    var rsData2 = prf(io.in(i).bits.pf.prfSrc2)
    val rsData1Read = WireDefault(prf(io.in(i).bits.pf.prfSrc1))
    val rsData2Read = WireDefault(prf(io.in(i).bits.pf.prfSrc2))
    dontTouchUtil(rsData1Read)
    dontTouchUtil(rsData2Read)

    io.csrReadIO.addr := io.in(0).bits.cs.csrAddr
    for(j <- 0 until IssueWidth){
      when(io.to_ex(i).bits.pf.prfSrc1 === io.from_ex.upd(j).prfDst && io.from_ex.upd(j).rfWen){
        rsData1 := io.from_ex.upd(j).rdData
      }
      when(io.to_ex(i).bits.pf.prfSrc2 === io.from_ex.upd(j).prfDst && io.from_ex.upd(j).rfWen){
        rsData2 := io.from_ex.upd(j).rdData
      }
    }

    
    io.to_ex(i).bits.data.src1 := MuxLookup(io.in(i).bits.cs.src1Type,rsData1)(Seq(
      SDEF(A_PC) -> io.in(i).bits.cf.pc,
      SDEF(A_RS1) -> rsData1,
    ))
    io.to_ex(i).bits.data.src2 := MuxLookup(io.in(i).bits.cs.src2Type,rsData2)(Seq(
      SDEF(B_IMM) -> io.in(i).bits.cf.imm,
      SDEF(B_RS2) -> rsData2,
      SDEF(B_PC ) -> io.in(i).bits.cf.pc,
      SDEF(B_CSR) -> io.csrReadIO.data,
    ))
    io.to_ex(i).valid := io.in(i).valid
  }

  if(EnableVerlatorSim){
    val archTable = WireInit(VecInit(Seq.fill(ArfSize)(0.U(log2Up(PrfSize).W))))
    ExcitingUtils.addSink(archTable,"archTable",ExcitingUtils.Func)
    val archReg  = Wire(Vec(ArfSize,UInt(XLEN.W)))
    for(i <- 0 until ArfSize){
      archReg(i) := prf(archTable(i))
    }

    ExcitingUtils.addSource(archReg,"DiffGPR",ExcitingUtils.Func)
  }
}
