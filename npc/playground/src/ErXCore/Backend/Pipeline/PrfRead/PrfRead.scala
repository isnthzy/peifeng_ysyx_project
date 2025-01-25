package ErXCore

import chisel3._
import chisel3.util._
import DecodeSignal._

class PrfRead extends ErXCoreModule{
  val io = IO(new Bundle{
    val in = Vec(IssueWidth,Flipped(DecoupledIO(new RenameIO)))
    // val from_ex = Input(Bool()) //forward
    val from_cm = Input(new PrfReadFromCommit(updSize = CommitWidth))
    val to_ex = Vec(IssueWidth,DecoupledIO(new IssueIO))
    // val out = DecodedIO()
  })
  val prf = RegInit(VecInit(Seq.fill(PrfSize)(0.U(XLEN.W))))
  
  for(i <- 0 until CommitWidth){
    when(io.from_cm.upd(i).rfWen && io.from_cm.upd(i).rfDst =/= 0.U){
      prf(io.from_cm.upd(i).rfDst) := io.from_cm.upd(i).rdData
    }
  } 

  for(i <- 0 until IssueWidth){
    io.to_ex(i).bits.cs := io.in(i).bits.cs
    io.to_ex(i).bits.pf := io.in(i).bits.pf

    var rsData1 = prf(io.in(i).bits.pf.prfSrc1)
    var rsData2 = prf(io.in(i).bits.pf.prfSrc2)

    // io.to_ex(i).bits.data.rs1 := prf(io.in(i).bits.pf.prfSrc1)
    // io.to_ex(i).bits.data.rs2 := prf(io.in(i).bits.pf.prfSrc2)
    for(j <- 0 until CommitWidth){
      when(io.to_ex(i).bits.pf.prfSrc1 === io.from_cm.upd(j).rfDst && io.from_cm.upd(i).rfWen){
        rsData1 := io.from_cm.upd(j).rdData
      }
      when(io.to_ex(i).bits.pf.prfSrc2 === io.from_cm.upd(j).rfDst && io.from_cm.upd(i).rfWen){
        rsData2 := io.from_cm.upd(j).rdData
      }
    }
    // io.to_ex(i).bits.data.src1 := rsData1
    // io.to_ex(i).bits.data.src2 := rsData2
    io.to_ex(i).bits.data.src1 := MuxLookup(io.in(i).bits.cs.src1Type,rsData1)(Seq(
      SDEF(A_PC) -> io.in(i).bits.cf.pc,
      SDEF(A_RS1) -> rsData1,
    ))
    io.to_ex(i).bits.data.src2 := MuxLookup(io.in(i).bits.cs.src2Type,rsData2)(Seq(
      SDEF(B_IMM) -> io.in(i).bits.cf.imm,
      SDEF(B_RS2) -> rsData2,
      SDEF(B_CSR) -> 0.U,
    ))
    io.to_ex(i).valid := io.in(i).valid
  }

}
