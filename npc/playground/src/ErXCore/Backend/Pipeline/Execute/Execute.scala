package ErXCore

import chisel3._
import chisel3.util._
import DecodeSignal._

class Execute extends ErXCoreModule{
  val io = IO(new Bundle{
    val in = Vec(IssueWidth,Flipped(DecoupledIO(new IssueIO)))
    val fw_dr  = Output(new RenameFromExecuteUpdate(updSize = IssueWidth))
    val fw_rob = Output(new ROBFromExecuteUpdate(updSize = IssueWidth))
    val dmemStore = new SimpleMemIO
    val dmemLoad  = new SimpleMemIO
  })
  
  //在设计时就考虑到，csr等指令只能第一个弹出队列，所以issue送来的东西直接分配即可
  val pipeALUorCSR = Module(new PipeALUorCSR)
  val pipeALU = Module(new PipeALU)
  val pipeMEM = Module(new PipeMem(useDmem = true))
  val pipe = List(pipeALUorCSR,pipeALU,pipeMEM)
  for(i <- 0 until pipe.length){
    pipe(i).io.in <> io.in(i)
  } //从发射上限制CSR，当读到第二条指令是csr时，阻塞发射到第一条队列上
  io.dmemStore <> pipeMEM.io.dmemStore.get
  io.dmemLoad  <> pipeMEM.io.dmemLoad.get

  def checkBranchTaken(brType: UInt, result: Bool): Bool = { 
      ((brType===SDEF(BR_EQ) &&  result(0))
    || (brType===SDEF(BR_NE) && ~result(0))
    || (brType===SDEF(BR_LT) &&  result(0))
    || (brType===SDEF(BR_LTU)&&  result(0))
    || (brType===SDEF(BR_GE) && ~result(0))
    || (brType===SDEF(BR_GEU)&& ~result(0))) && isBranch(brType)
  }

  for(i <- 0 until IssueWidth){
    io.fw_rob.upd(i)     := pipe(i).io.out
    io.fw_rob.upd(i).bits.br.taken  := checkBranchTaken(io.in(i).bits.cs.brType,pipe(i).io.out.bits.result(0))
    io.fw_rob.upd(i).bits.br.target := io.in(i).bits.cf.pc + io.in(i).bits.cf.imm
  } 
}

abstract class AbstaceExecutePipe(useDmem: Boolean = false) extends ErXCoreModule{
  val io = IO(new Bundle{
    val in = Flipped(DecoupledIO(new IssueIO))
    val out = Output(Valid(new PipeExecuteOut))
    val dmemStore = if(useDmem) Some(new SimpleMemIO) else None
    val dmemLoad  = if(useDmem) Some(new SimpleMemIO) else None
  })
  io.out.bits.br := DontCare
} 

class PipeALUorCSR extends AbstaceExecutePipe{
  val Alu = Module(new Alu)
  io.in.ready := true.B
  Alu.io.op := io.in.bits.cs.aluOp
  Alu.io.src1 := io.in.bits.data.src1
  Alu.io.src2 := io.in.bits.data.src2
  io.out.valid := io.in.valid
  io.out.bits.result := Alu.io.result
  io.out.bits.isBranch := isBranch(io.in.bits.cs.brType)
  io.out.bits.robIdx   := 0.U
  io.out.bits.isStore  := false.B
}

class PipeALU extends AbstaceExecutePipe{
  val Alu = Module(new Alu)
  io.in.ready := true.B
  Alu.io.op := io.in.bits.cs.aluOp
  Alu.io.src1 := io.in.bits.data.src1
  Alu.io.src2 := io.in.bits.data.src2
  io.out.valid := io.in.valid
  io.out.bits.result := Alu.io.result
  io.out.bits.isBranch := isBranch(io.in.bits.cs.brType)
  io.out.bits.isStore  := false.B
}

class PipeMem(useDmem: Boolean = false) extends AbstaceExecutePipe(useDmem){
  val lsu = Module(new LSU)
  io.dmemStore.get <> lsu.io.DMemStore
  io.dmemLoad.get  <> lsu.io.DMemLoad
  io.in.ready := !lsu.io.busy
  lsu.io.valid := io.in.valid
  io.out.valid := false.B
  io.out.bits.result := lsu.io.ldData
  io.out.bits.isBranch := false.B
  io.out.bits.isStore  := false.B
}

// class PipeMem

