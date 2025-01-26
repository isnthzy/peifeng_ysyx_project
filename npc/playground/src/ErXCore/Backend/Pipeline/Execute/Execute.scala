package ErXCore

import chisel3._
import chisel3.util._
import DecodeSignal._

class Execute extends ErXCoreModule{
  val io = IO(new Bundle{
    val in = Vec(IssueWidth,Flipped(DecoupledIO(new IssueIO)))
    val fw_dr  = Output(new RenameFromExecuteUpdate(updSize = IssueWidth))
    val fw_rob = Output(new ROBFromExecute(updSize = IssueWidth))
    // val to_cm  = Output(new Bundle {})
    // val out = DecodedIO()
  })
  
  //在设计时就考虑到，csr等指令只能第一个弹出队列，所以issue送来的东西直接分配即可
  val pipeALUorCSR = Module(new PipeALUorCSR)
  val pipeALU = Module(new PipeALU)
  val pipeMEM = Module(new PipeMem)
  (0 until IssueWidth).map(i => io.in(i).ready := 
      pipeALUorCSR.io.in.ready && pipeALU.io.in.ready && pipeMEM.io.in.ready)
  pipeALUorCSR.io.in.bits := io.in(0).bits
  //从发射上限制CSR，当读到第二条指令是csr时，阻塞发射到第一条队列上
  pipeALU.io.in.bits := io.in(1).bits
  pipeMEM.io.in.bits := io.in(2).bits

  def checkBranchTaken(brType: UInt, result: Bool): Bool = { 
      ((brType===SDEF(BR_EQ) &&  pipeALUorCSR.io.out.result(0))
    || (brType===SDEF(BR_NE) && ~pipeALUorCSR.io.out.result(0))
    || (brType===SDEF(BR_LT) &&  pipeALUorCSR.io.out.result(0))
    || (brType===SDEF(BR_LTU)&&  pipeALUorCSR.io.out.result(0))
    || (brType===SDEF(BR_GE) && ~pipeALUorCSR.io.out.result(0))
    || (brType===SDEF(BR_GEU)&& ~pipeALUorCSR.io.out.result(0))) && isBranch(brType)
  }
  for(i <- 0 until 2){
    if(i == 0){
      io.fw_rob.upd(i).br.taken  := checkBranchTaken(io.in(i).bits.cs.brType,pipeALUorCSR.io.out.result(0))
    }else if(i == 1){
      io.fw_rob.upd(i).br.taken  := checkBranchTaken(io.in(i).bits.cs.brType,pipeALU.io.out.result(0))
    }
    io.fw_rob.upd(i).br.target := io.in(i).bits.cf.pc + io.in(i).bits.cf.imm
  }
}

abstract class AbstaceExecutePipe extends ErXCoreModule{
  val io = IO(new Bundle{
    val in = Flipped(DecoupledIO(new IssueIO))
    val out = Output(new PipeExecuteOut)
  })
}

class PipeALUorCSR extends AbstaceExecutePipe{
  val Alu = Module(new Alu)
  io.in.ready := true.B
  Alu.io.op := io.in.bits.cs.aluOp
  Alu.io.src1 := io.in.bits.data.src1
  Alu.io.src2 := io.in.bits.data.src2
  io.out.result := Alu.io.result
}

class PipeALU extends AbstaceExecutePipe{
  val Alu = Module(new Alu)
  io.in.ready := true.B
  Alu.io.op := io.in.bits.cs.aluOp
  Alu.io.src1 := io.in.bits.data.src1
  Alu.io.src2 := io.in.bits.data.src2
  io.out.result := Alu.io.result
}
class PipeMem extends AbstaceExecutePipe{
  val lsu = Module(new LSU)
  io.in.ready := !lsu.io.busy
  lsu.io.valid := io.in.valid

}

// class PipeMem

