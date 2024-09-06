package DiffTest
import chisel3._
import chisel3.util._
import CoreConfig.Configs._
import Bundles.Difftest._
import dpic._

class DiffCommit extends Module{
  val diff=IO(new Bundle {
    val instr=Input(new DiffInstrBundle())
    val excp =Input(new DiffExcpBundle())
    val store=Input(new DiffStoreBundle())
    val load =Input(new DiffLoadBundle())
    val csr  =Input(new DiffCsrRegBundle())
    val reg  =Input(Vec(32,UInt(32.W)))
  })

  val dpicInstr=DPIC(new InsrtCommit())
  val dpicExcp =DPIC(new ExcpEvent())
  val dpicLoad =DPIC(new LoadEvent())
  val dpicStore=DPIC(new StoreEvent())
  val dpicCsr  =DPIC(new CSRRegState())
  val dpicReg  =DPIC(new GRegState())
  dpicInstr.index:=diff.instr.index
  dpicInstr.valid:=diff.instr.valid
  dpicInstr.pc:=diff.instr.pc
  dpicInstr.instr:=diff.instr.instr
  dpicInstr.skip:=diff.instr.skip
  dpicInstr.wen:=diff.instr.wen
  dpicInstr.wdest:=diff.instr.wdest  
  dpicInstr.wdata:=diff.instr.wdata
  dpicInstr.csrRstat:=diff.instr.csrRstat
  dpicInstr.csrData:=diff.instr.csrData

  dpicExcp.excp_valid:=diff.excp.excpValid
  dpicExcp.isMret:=diff.excp.isMret
  dpicExcp.intrptNo:=diff.excp.intrptNo
  dpicExcp.cause:=diff.excp.cause
  dpicExcp.exceptionPC:=diff.excp.exceptionPC
  dpicExcp.exceptionInst:=diff.excp.exceptionInst

  dpicLoad.index:=diff.load.index
  dpicLoad.valid:=diff.load.valid
  dpicLoad.vaddr:=diff.load.vaddr
  dpicLoad.paddr:=diff.load.paddr
  dpicLoad.data:=diff.load.data

  dpicStore.index:=diff.store.index
  dpicStore.valid:=diff.store.valid
  dpicStore.paddr:=diff.store.paddr
  dpicStore.vaddr:=diff.store.vaddr
  dpicStore.data:=diff.store.data

  dpicCsr.mstatus:=diff.csr.mstatus
  dpicCsr.mepc:=diff.csr.mepc
  dpicCsr.mcause:=diff.csr.mcause
  dpicCsr.mtvec:=diff.csr.mtvec

  dpicReg.gpr:=diff.reg
  DPIC.collect()
}

trait DiffParameter{
  def DPIC_ARG_BIT  = 8
  def DPIC_ARG_BYTE = 8
  def DPIC_ARG_INT  = 32
  def DPIC_ARG_LONG = 64
}


class InsrtCommit extends DPICBundle with DiffParameter{
  val index=Input(UInt(DPIC_ARG_BYTE.W))
  val valid=Input(UInt(DPIC_ARG_BIT.W))
  val pc   =Input(UInt(DPIC_ARG_LONG.W))
  val instr=Input(UInt(DPIC_ARG_INT.W))
  val skip =Input(UInt(DPIC_ARG_BIT.W))
  val wen  =Input(UInt(DPIC_ARG_BIT.W))
  val wdest=Input(UInt(DPIC_ARG_BYTE.W))
  val wdata=Input(UInt(DPIC_ARG_LONG.W))
  val csrRstat=Input(UInt(DPIC_ARG_BIT.W))
  val csrData =Input(UInt(DPIC_ARG_INT.W))
}

class ExcpEvent extends DPICBundle with DiffParameter{
  val excp_valid=Input(UInt(DPIC_ARG_BYTE.W))
  val isMret    =Input(UInt(DPIC_ARG_BIT.W))
  val intrptNo  =Input(UInt(DPIC_ARG_INT.W))
  val cause     =Input(UInt(DPIC_ARG_INT.W))
  val exceptionPC  =Input(UInt(DPIC_ARG_LONG.W))
  val exceptionInst=Input(UInt(DPIC_ARG_INT.W))
}

class StoreEvent extends DPICBundle with DiffParameter{
  val index=Input(UInt(DPIC_ARG_BYTE.W))
  val valid=Input(UInt(DPIC_ARG_BIT.W))
  val paddr=Input(UInt(DPIC_ARG_LONG.W))
  val vaddr=Input(UInt(DPIC_ARG_LONG.W))
  val data =Input(UInt(DPIC_ARG_LONG.W))
}

class LoadEvent extends DPICBundle with DiffParameter{
  val index=Input(UInt(DPIC_ARG_BYTE.W))
  val valid=Input(UInt(DPIC_ARG_BIT.W))
  val paddr=Input(UInt(DPIC_ARG_LONG.W))
  val vaddr=Input(UInt(DPIC_ARG_LONG.W))
  val data =Input(UInt(DPIC_ARG_LONG.W))
}

class CSRRegState extends DPICBundle with DiffParameter{
  val mstatus=Input(UInt(DPIC_ARG_LONG.W))
  val mtvec  =Input(UInt(DPIC_ARG_LONG.W))
  val mepc   =Input(UInt(DPIC_ARG_LONG.W))
  val mcause =Input(UInt(DPIC_ARG_LONG.W))
}

class GRegState extends DPICBundle with DiffParameter{
  val gpr=Input(Vec(32,UInt(DPIC_ARG_LONG.W)))
}