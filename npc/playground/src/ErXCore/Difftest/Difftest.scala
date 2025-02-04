package ErXCore.Difftest

import chisel3._
import chisel3.util._
//重写，or随便糊一个之前的，随便糊一个之前的吧，
trait DiffBundleConst{
  def REGSIZE = 32
  def XLEN = 32
}
abstract class DiffBundle extends Bundle with DiffBundleConst
abstract class DiffModule extends Module with DiffBundleConst

class DiffCommit extends DiffModule{
  val diff=IO(new DiffBundle {
    val instr=Input(new DiffInstrBundle())
    val instr1=Input(new DiffInstrBundle())
    val excp =Input(new DiffExcpBundle())
    val store=Input(new DiffStoreBundle())
    val load =Input(new DiffLoadBundle())
    val store1=Input(new DiffStoreBundle())
    val load1 =Input(new DiffLoadBundle())
    val csr  =Input(new DiffCsrRegBundle())
    val reg  =Input(Vec(REGSIZE,UInt(XLEN.W)))
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

  DiffBridge.io.index:=RegNext(diff.instr1.index,0.U)
  DiffBridge.io.instrValid:=RegNext(diff.instr1.valid,0.U)
  DiffBridge.io.the_pc:=RegNext(diff.instr1.pc,0.U)
  DiffBridge.io.instr:=RegNext(diff.instr1.instr,0.U)
  DiffBridge.io.skip:=RegNext(diff.instr1.skip,0.U)
  DiffBridge.io.wen:=RegNext(diff.instr1.wen,0.U)
  DiffBridge.io.wdest:=RegNext(diff.instr1.wdest,0.U)
  DiffBridge.io.wdata:=RegNext(diff.instr1.wdata,0.U)
  DiffBridge.io.csrRstat:=RegNext(diff.instr1.csrRstat,0.U)
  DiffBridge.io.csrData:=RegNext(diff.instr1.csrData,0.U)

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

  DiffBridge.io.storeIndex:=RegNext(diff.store1.index,0.U)
  DiffBridge.io.storeValid:=RegNext(diff.store1.valid,0.U)
  DiffBridge.io.storePaddr:=RegNext(diff.store1.paddr,0.U)
  DiffBridge.io.storeVaddr:=RegNext(diff.store1.vaddr,0.U)
  DiffBridge.io.storeData:= RegNext(diff.store1.data,0.U)
  DiffBridge.io.storeLen := RegNext(diff.store1.len,0.U)

  DiffBridge.io.loadIndex:=RegNext(diff.load1.index,0.U)
  DiffBridge.io.loadValid:=RegNext(diff.load1.valid,0.U)
  DiffBridge.io.loadPaddr:=RegNext(diff.load1.paddr,0.U)
  DiffBridge.io.loadVaddr:=RegNext(diff.load1.vaddr,0.U)
  DiffBridge.io.loadData :=RegNext(diff.load1.data ,0.U)
  DiffBridge.io.loadLen  :=RegNext(diff.load1.len,0.U)

  DiffBridge.io.mstatus:=RegNext(diff.csr.mstatus,0.U)
  DiffBridge.io.mepc   :=RegNext(diff.csr.mepc   ,0.U)
  DiffBridge.io.mcause :=RegNext(diff.csr.mcause ,0.U)
  DiffBridge.io.mtvec  :=RegNext(diff.csr.mtvec  ,0.U)

  DiffBridge.io.REG:=diff.reg
}

class DiffBridge extends BlackBox with HasBlackBoxPath{
  val io = IO(new DiffBundle {
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

    val index1 = Input(UInt(8.W))
    val instrValid1 = Input(Bool())
    val the_pc1 = Input(UInt(64.W))
    val instr1 = Input(UInt(32.W))
    val skip1 = Input(Bool())
    val wen1 = Input(Bool())
    val wdest1 = Input(UInt(8.W))
    val wdata1 = Input(UInt(64.W))
    val csrRstat1 = Input(Bool())
    val csrData1 = Input(UInt(64.W))


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

    val storeIndex1 = Input(UInt(8.W))
    val storeValid1 = Input(UInt(8.W))
    val storePaddr1 = Input(UInt(64.W))
    val storeVaddr1 = Input(UInt(64.W))
    val storeData1 = Input(UInt(64.W))
    val storeLen1  = Input(UInt(8.W))

    val loadIndex1 = Input(UInt(8.W))
    val loadValid1 = Input(UInt(8.W))
    val loadPaddr1 = Input(UInt(64.W))
    val loadVaddr1 = Input(UInt(64.W))
    val loadData1  = Input(UInt(64.W))
    val loadLen1   = Input(UInt(8.W))    

    val mstatus = Input(UInt(64.W))
    val mtvec = Input(UInt(64.W))
    val mepc = Input(UInt(64.W))
    val mcause = Input(UInt(64.W))

    val REG=Input(Vec(32, UInt(64.W)))
  })
  addPath("playground/src/ErXCore/Difftest/DiffBridge.v")

}


class DiffInstrBundle extends DiffBundle{
  val index=UInt(8.W)
  val valid=Bool()
  val pc=UInt(XLEN.W)
  val instr=UInt(32.W)
  val skip=Bool()
  val wen=Bool()
  val wdest=UInt(8.W)
  val wdata=UInt(XLEN.W)
  val csrRstat=Bool()
  val csrData=UInt(XLEN.W)
}

class DiffExcpBundle extends DiffBundle{
  val excpValid=Bool()
  val isMret=Bool()
  val intrptNo=Bool()
  val cause =UInt(32.W)
  val exceptionPC=UInt(XLEN.W)
  val exceptionInst=UInt(32.W)
}

class DiffStoreBundle extends DiffBundle{
  val index=UInt(8.W)
  val valid=UInt(8.W)
  val paddr=UInt(XLEN.W) //给未对齐的地址
  val vaddr=UInt(XLEN.W) //给未对齐的地址
  val data =UInt(XLEN.W)
  val len  =UInt(8.W)
}

class DiffLoadBundle extends DiffBundle{
  val index=UInt(8.W)
  val valid=UInt(8.W)
  val paddr=UInt(XLEN.W) //给未对齐的地址
  val vaddr=UInt(XLEN.W) //给未对齐的地址
  val data =UInt(XLEN.W)
  val len  =UInt(8.W)
}

class DiffCsrRegBundle extends DiffBundle{
  val mstatus=UInt(XLEN.W)
  val mtvec  =UInt(XLEN.W)
  val mepc   =UInt(XLEN.W)
  val mcause =UInt(XLEN.W)
}
