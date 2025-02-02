package ErXCore

import chisel3._
import chisel3.util._
import DecodeSignal._

class Execute extends ErXCoreModule{
  val io = IO(new Bundle{
    val in = Vec(IssueWidth,Flipped(DecoupledIO(new IssueIO)))
    val fw_dr  = Output(new RenameFromExecuteUpdate(updSize = IssueWidth))
    val fw_rob = Output(new ROBFromExecute(updSize = IssueWidth))
    val dmemStore = Output(new SimpleMemIO)
    val dmemLoad  = Output(new SimpleMemIO)
  })
  
  //在设计时就考虑到，csr等指令只能第一个弹出队列，所以issue送来的东西直接分配即可
  val pipeALUorCSR = Module(new PipeALUorCSR)
  val pipeALU = Module(new PipeALU)
  val pipeMEM = Module(new PipeMem(useDmem = true))
  val pipe = List(pipeALUorCSR,pipeALU,pipeMEM)
  for(i <- 0 until pipe.length){
    pipe(i).io.in := io.in(i)
  } //从发射上限制CSR，当读到第二条指令是csr时，阻塞发射到第一条队列上
  io.dmemStore := pipeMEM.io.dmemStore.get
  io.dmemLoad  := pipeMEM.io.dmemLoad.get

  def checkBranchTaken(brType: UInt, result: Bool): Bool = { 
      ((brType===SDEF(BR_EQ) &&  result(0))
    || (brType===SDEF(BR_NE) && ~result(0))
    || (brType===SDEF(BR_LT) &&  result(0))
    || (brType===SDEF(BR_LTU)&&  result(0))
    || (brType===SDEF(BR_GE) && ~result(0))
    || (brType===SDEF(BR_GEU)&& ~result(0))) && isBranch(brType)
  }

  for(i <- 0 until 2){
    io.fw_rob.upd(i).br.taken  := checkBranchTaken(io.in(i).bits.cs.brType,pipe(i).io.out.result(0))
    io.fw_rob.upd(i).br.target := io.in(i).bits.cf.pc + io.in(i).bits.cf.imm
  } 
}

abstract class AbstaceExecutePipe(useDmem: Boolean = false) extends ErXCoreModule{
  val io = IO(new Bundle{
    val in = DecoupledIO(new IssueIO)
    val out = Output(new PipeExecuteOut)
    val dmemStore = if(useDmem) Some(new SimpleMemIO) else None
    val dmemLoad  = if(useDmem) Some(new SimpleMemIO) else None
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

class PipeMem(useDmem: Boolean = false) extends AbstaceExecutePipe(useDmem){
  val lsu = Module(new LSU)
  io.dmemStore.get := lsu.io.DMemStore
  io.dmemLoad.get  := lsu.io.DMemLoad
  io.in.ready := !lsu.io.busy
  lsu.io.valid := io.in.valid
  
}

// class PipeMem

