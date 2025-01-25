package DiffTest
import chisel3._
import chisel3.util._
import CoreConfig.Configs._
import Bundles.Difftest._

class DiffCommit extends Module{
  val diff=IO(new Bundle {
    val instr=Input(new DiffInstrBundle())
    val excp =Input(new DiffExcpBundle())
    val store=Input(new DiffStoreBundle())
    val load =Input(new DiffLoadBundle())
    val csr  =Input(new DiffCsrRegBundle())
    val reg  =Input(Vec(32,UInt(DATA_WIDTH.W)))
  })
  val DiffBridge=Module(new DiffBridge())
  DiffBridge.io.clock:=clock
  DiffBridge.io.index:=RegNext(diff.instr.index,0.U)
  DiffBridge.io.instrValid:=RegNext(diff.instr.valid,0.U)
  DiffBridge.io.the_pc:=RegNext(diff.instr.pc,0.U)
  DiffBridge.io.instr:=RegNext(diff.instr.instr,0.U)
  DiffBridge.io.skip:=RegNext(diff.instr.skip,0.U)
  DiffBridge.io.wen:=RegNext(diff.instr.wen,0.U)
  DiffBridge.io.wdest:=RegNext(diff.instr.wdest,0.U)
  DiffBridge.io.wdata:=RegNext(diff.instr.wdata,0.U)
  DiffBridge.io.csrRstat:=RegNext(diff.instr.csrRstat,0.U)
  DiffBridge.io.csrData:=RegNext(diff.instr.csrData,0.U)

  DiffBridge.io.excp_valid:=RegNext(diff.excp.excpValid,0.U)
  DiffBridge.io.isMret:=RegNext(diff.excp.isMret,0.U)
  DiffBridge.io.intrptNo:=RegNext(diff.excp.intrptNo,0.U)
  DiffBridge.io.cause:=RegNext(diff.excp.cause,0.U)
  DiffBridge.io.exceptionPC:=RegNext(diff.excp.exceptionPC,0.U)
  DiffBridge.io.exceptionInst:=RegNext(diff.excp.exceptionInst,0.U)

  DiffBridge.io.storeIndex:=RegNext(diff.store.index,0.U)
  DiffBridge.io.storeValid:=RegNext(diff.store.valid,0.U)
  DiffBridge.io.storePaddr:=RegNext(diff.store.paddr,0.U)
  DiffBridge.io.storeVaddr:=RegNext(diff.store.vaddr,0.U)
  DiffBridge.io.storeData:= RegNext(diff.store.data,0.U)
  DiffBridge.io.storeLen := RegNext(diff.store.len,0.U)

  DiffBridge.io.loadIndex:=RegNext(diff.load.index,0.U)
  DiffBridge.io.loadValid:=RegNext(diff.load.valid,0.U)
  DiffBridge.io.loadPaddr:=RegNext(diff.load.paddr,0.U)
  DiffBridge.io.loadVaddr:=RegNext(diff.load.vaddr,0.U)
  DiffBridge.io.loadData :=RegNext(diff.load.data ,0.U)
  DiffBridge.io.loadLen  :=RegNext(diff.load.len,0.U)

  DiffBridge.io.mstatus:=RegNext(diff.csr.mstatus,0.U)
  DiffBridge.io.mepc   :=RegNext(diff.csr.mepc   ,0.U)
  DiffBridge.io.mcause :=RegNext(diff.csr.mcause ,0.U)
  DiffBridge.io.mtvec  :=RegNext(diff.csr.mtvec  ,0.U)

  DiffBridge.io.REG:=diff.reg
}

class DiffBridge extends BlackBox with HasBlackBoxPath{
  val io = IO(new Bundle {
    val clock = Input(Clock())

    val index = Input(UInt(8.W))
    val instrValid = Input(Bool())
    val the_pc = Input(UInt(64.W))
    val instr = Input(UInt(32.W))
    val skip = Input(Bool())
    val wen = Input(Bool())
    val wdest = Input(UInt(8.W))
    val wdata = Input(UInt(64.W))
    val csrRstat = Input(Bool())
    val csrData = Input(UInt(64.W))

    val excp_valid = Input(Bool())
    val isMret = Input(Bool())
    val intrptNo = Input(UInt(32.W))
    val cause = Input(UInt(32.W))
    val exceptionPC = Input(UInt(64.W))
    val exceptionInst = Input(UInt(32.W))

    val storeIndex = Input(UInt(8.W))
    val storeValid = Input(UInt(8.W))
    val storePaddr = Input(UInt(64.W))
    val storeVaddr = Input(UInt(64.W))
    val storeData = Input(UInt(64.W))
    val storeLen  = Input(UInt(8.W))

    val loadIndex = Input(UInt(8.W))
    val loadValid = Input(UInt(8.W))
    val loadPaddr = Input(UInt(64.W))
    val loadVaddr = Input(UInt(64.W))
    val loadData  = Input(UInt(64.W))
    val loadLen   = Input(UInt(8.W))

    val mstatus = Input(UInt(64.W))
    val mtvec = Input(UInt(64.W))
    val mepc = Input(UInt(64.W))
    val mcause = Input(UInt(64.W))

    val REG=Input(Vec(32, UInt(64.W)))
  })
  addPath("playground/src/DiffTest/DiffBridge.v")

}