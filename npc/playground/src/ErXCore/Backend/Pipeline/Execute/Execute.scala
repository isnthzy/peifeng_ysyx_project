package ErXCore

import chisel3._
import chisel3.util._
import DecodeSignal._

class Execute extends ErXCoreModule{
  val io = IO(new Bundle{
    val in = Vec(IssueWidth,Flipped(DecoupledIO(new IssueIO)))
    val flush = Input(Bool())
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
    pipe(i).io.flush := io.flush
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
    val flush = Input(Bool())
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
  lsu.io.req.bits.addr   := io.in.bits.data.src1 + io.in.bits.cf.imm
  lsu.io.req.bits.wdata  := io.in.bits.data.src2
  
  if(EnableDebug){
    val loadBuff = RegInit(0.U.asTypeOf(io.in.bits))
    val isLoadBuff = RegInit(false.B)
    when(io.in.fire&&isLoadInst(io.in.bits.cs.lsType)){
      loadBuff := io.in.bits
      isLoadBuff := true.B
    }.elsewhen(lsu.io.resp.fire){
      loadBuff := 0.U.asTypeOf(io.in.bits)
      isLoadBuff := false.B
    }
    Info(lsu.io.req.fire&&isStoreInst(io.in.bits.cs.lsType), p"[st] pc: 0x${Hexadecimal(io.in.bits.cf.pc)} inst: 0x${Hexadecimal(io.in.bits.cf.inst)} addr: 0x${Hexadecimal(lsu.io.req.bits.addr)} wdata: 0x${Hexadecimal(lsu.io.req.bits.wdata)}\n")
    Info(lsu.io.resp.fire&&isLoadBuff, p"[ld] pc: 0x${Hexadecimal(loadBuff.cf.pc)} inst: 0x${Hexadecimal(loadBuff.cf.inst)} addr: 0x${Hexadecimal(loadBuff.data.src1 + loadBuff.cf.imm)} rdata: 0x${Hexadecimal(lsu.io.resp.bits.rdata)}\n")
  }
  

  val outBuff = RegInit(0.U.asTypeOf(io.out.bits))
  val isStoreBuff = RegInit(false.B)
  val pendingLoadResp = RegInit(false.B)
  when(io.in.fire){
    isStoreBuff := isStoreInst(io.in.bits.cs.lsType)
    outBuff.robIdx   := io.in.bits.robIdx
    outBuff.rfWen    := io.in.bits.cs.rfWen
    outBuff.prfDst   := io.in.bits.pf.prfDst
    pendingLoadResp  := isLoadInst(io.in.bits.cs.lsType)
  }
  when(lsu.io.resp.fire){
    pendingLoadResp := false.B
  }

  val flush_idle :: flush_wait_resp :: Nil = Enum(2)
  val flushState =  RegInit(flush_idle)
  switch(flushState){
    is(flush_idle){
      when(io.flush && pendingLoadResp && !lsu.io.resp.fire){
        flushState := flush_wait_resp
      }
    }
    is(flush_wait_resp){
      when(lsu.io.resp.fire){
        flushState := flush_idle
      }
    }
  }

  io.out.valid       := lsu.io.resp.fire && !(flushState === flush_wait_resp)
  lsu.io.resp.ready  := true.B
  io.out.bits.result := lsu.io.resp.bits.rdata
  io.out.bits.isBranch := false.B
  io.out.bits.isStore  := isStoreBuff
  io.out.bits.robIdx   := outBuff.robIdx
  io.out.bits.rfWen    := outBuff.rfWen && io.out.valid
  io.out.bits.prfDst   := outBuff.prfDst
  io.out.bits.csr      := DontCare


  if(EnableVerlatorSim){
    val diffStoreValid = Wire(Bool())
    val diffLoadValid  = Wire(Bool())
    val diffLSAddr = Wire(UInt(XLEN.W))
    val diffLSData = Wire(UInt(XLEN.W))
    val diffLSLen  = Wire(UInt(8.W))
    val isLoad = isLoadInst(io.in.bits.cs.lsType)
    val isStore = isStoreInst(io.in.bits.cs.lsType)
    val robIdx = Wire(UInt(RobIdxWidth.W))

    diffStoreValid := io.in.fire && isStore
    diffLoadValid  := io.in.fire && isLoad
    diffLSAddr := io.in.bits.data.src1 + io.in.bits.cf.imm
    diffLSData := io.in.bits.data.src2
    diffLSLen  := Cat(0.U(5.W),io.in.bits.cs.lsType === SDEF(ST_SW) || io.in.bits.cs.lsType === SDEF(LD_LW),
                               io.in.bits.cs.lsType === SDEF(ST_SH) || io.in.bits.cs.lsType === SDEF(LD_LH) || io.in.bits.cs.lsType === SDEF(LD_LHU),
                               io.in.bits.cs.lsType === SDEF(ST_SB) || io.in.bits.cs.lsType === SDEF(LD_LB) || io.in.bits.cs.lsType === SDEF(LD_LBU))
    robIdx     := io.in.bits.robIdx
    ExcitingUtils.addSource(diffStoreValid,"diffStoreValid",ExcitingUtils.Func)
    ExcitingUtils.addSource(diffLoadValid ,"iffLoadValid"  ,ExcitingUtils.Func)
    ExcitingUtils.addSource(diffLSAddr    ,"diffLSAddr"    ,ExcitingUtils.Func)
    ExcitingUtils.addSource(diffLSData    ,"diffLSData"    ,ExcitingUtils.Func)
    ExcitingUtils.addSource(diffLSLen     ,"diffLSLen"     ,ExcitingUtils.Func)
    ExcitingUtils.addSource(robIdx        ,"diffRobIdx"    ,ExcitingUtils.Func)
  }
}

// class PipeMem

