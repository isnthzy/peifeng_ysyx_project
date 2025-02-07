package ErXCore

import chisel3._
import chisel3.util._
import DecodeSignal._

class Execute extends ErXCoreModule{
  val io = IO(new Bundle{
    val in = Vec(IssueWidth,Flipped(DecoupledIO(new IssueIO)))
    val fw_dr  = Output(new RenameFromExecuteUpdate(updSize = IssueWidth))
    val fw_pr  = Output(new PrfReadFromExecute(updSize = IssueWidth))
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
      ((brType === SDEF(BR_EQ) &&  result(0))
    || (brType === SDEF(BR_NE) && ~result(0))
    || (brType === SDEF(BR_LT) &&  result(0))
    || (brType === SDEF(BR_LTU)&&  result(0))
    || (brType === SDEF(BR_GE) && ~result(0))
    || (brType === SDEF(BR_GEU)&& ~result(0))
    || (brType === SDEF(BR_JALR))) && isBranch(brType)
  }

  for(i <- 0 until IssueWidth){
    io.fw_rob.upd(i)     := pipe(i).io.out
    if(i >= 0 && i < 2){
      io.fw_rob.upd(i).bits.br.taken  := checkBranchTaken(io.in(i).bits.cs.brType,pipe(i).io.out.bits.result(0))
      io.fw_rob.upd(i).bits.br.target := Mux(io.in(i).bits.cs.brType =/= SDEF(BR_JALR),io.in(i).bits.cf.pc + io.in(i).bits.cf.imm,
        Cat((io.in(i).bits.data.src1 + io.in(i).bits.cf.imm).asUInt(31,1),0.U(1.W)))
    }else{
      io.fw_rob.upd(i).bits.br := DontCare
    }
    io.fw_dr.upd(i).wen    := pipe(i).io.out.bits.rfWen
    io.fw_dr.upd(i).prfDst := pipe(i).io.out.bits.prfDst
    io.fw_pr.upd(i).rfWen  := pipe(i).io.out.bits.rfWen
    io.fw_pr.upd(i).prfDst := pipe(i).io.out.bits.prfDst
    io.fw_pr.upd(i).rdData := pipe(i).io.out.bits.result
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
  io.out.bits.isStore  := false.B
  io.out.bits.robIdx   := io.in.bits.robIdx
  io.out.bits.rfWen    := io.in.bits.cs.rfWen
  io.out.bits.prfDst   := io.in.bits.pf.prfDst
  io.out.bits.csr.excpType := 0.U.asTypeOf(io.out.bits.csr.excpType)
  io.out.bits.csr.excpType.bkp := io.in.bits.cs.csrOp === SDEF(CSR_BREK)
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
  io.out.bits.robIdx   := io.in.bits.robIdx
  io.out.bits.rfWen    := io.in.bits.cs.rfWen
  io.out.bits.prfDst   := io.in.bits.pf.prfDst
  io.out.bits.csr      := DontCare
}

class PipeMem(useDmem: Boolean = false) extends AbstaceExecutePipe(useDmem){
  val lsu = Module(new LSU)
  io.dmemStore.get <> lsu.io.DMemStore
  io.dmemLoad.get  <> lsu.io.DMemLoad

  lsu.io.req.valid := io.in.valid
  io.in.ready      := lsu.io.req.ready
  lsu.io.req.bits.lsType := io.in.bits.cs.lsType
  lsu.io.req.bits.addr   := io.in.bits.cf.pc + io.in.bits.cf.imm
  lsu.io.req.bits.wdata  := io.in.bits.data.src2

  val outBuff = RegInit(0.U.asTypeOf(io.out.bits))
  val isStoreBuff = RegInit(false.B)
  when(io.in.fire){
    isStoreBuff := isStoreInst(io.in.bits.cs.lsType)
    outBuff.robIdx   := io.in.bits.robIdx
    outBuff.rfWen    := io.in.bits.cs.rfWen
    outBuff.prfDst   := io.in.bits.pf.prfDst
  }

  io.out.valid       := lsu.io.resp.fire
  lsu.io.resp.ready  := true.B
  io.out.bits.result := lsu.io.resp.bits.rdata
  io.out.bits.isBranch := false.B
  io.out.bits.isStore  := isStoreBuff
  io.out.bits.robIdx   := outBuff.robIdx
  io.out.bits.rfWen    := outBuff.rfWen
  io.out.bits.prfDst   := outBuff.prfDst
  io.out.bits.csr      := DontCare
}

// class PipeMem

