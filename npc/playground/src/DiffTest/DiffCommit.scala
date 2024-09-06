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
  DiffBridge.io.index:=diff.instr.index
  DiffBridge.io.instrValid:=diff.instr.valid
  DiffBridge.io.the_pc:=diff.instr.pc
  DiffBridge.io.instr:=diff.instr.instr
  DiffBridge.io.skip:=diff.instr.skip
  DiffBridge.io.wen:=diff.instr.wen
  DiffBridge.io.wdest:=diff.instr.wdest
  DiffBridge.io.wdata:=diff.instr.wdata
  DiffBridge.io.csrRstat:=diff.instr.csrRstat
  DiffBridge.io.csrData:=diff.instr.csrData

  DiffBridge.io.excp_valid:=diff.excp.excpValid
  DiffBridge.io.isMret:=diff.excp.isMret
  DiffBridge.io.intrptNo:=diff.excp.intrptNo
  DiffBridge.io.cause:=diff.excp.cause
  DiffBridge.io.exceptionPC:=diff.excp.exceptionPC
  DiffBridge.io.exceptionInst:=diff.excp.exceptionInst

  DiffBridge.io.storeIndex:=diff.store.index
  DiffBridge.io.storeValid:=diff.store.valid
  DiffBridge.io.storePaddr:=diff.store.paddr
  DiffBridge.io.storeVaddr:=diff.store.vaddr
  DiffBridge.io.storeData:=diff.store.data

  DiffBridge.io.loadIndex:=diff.load.index
  DiffBridge.io.loadValid:=diff.load.valid
  DiffBridge.io.loadPaddr:=diff.load.paddr
  DiffBridge.io.loadVaddr:=diff.load.vaddr
  DiffBridge.io.loadData :=diff.load.data

  DiffBridge.io.mstatus:=diff.csr.mstatus
  DiffBridge.io.mepc:=diff.csr.mepc
  DiffBridge.io.mcause:=diff.csr.mcause
  DiffBridge.io.mtvec:=diff.csr.mtvec

  DiffBridge.io.REG:=diff.reg(0)
  DiffBridge.io.REG_1:=diff.reg(1)
  DiffBridge.io.REG_2:=diff.reg(2)
  DiffBridge.io.REG_3:=diff.reg(3)
  DiffBridge.io.REG_4:=diff.reg(4)
  DiffBridge.io.REG_5:=diff.reg(5)
  DiffBridge.io.REG_6:=diff.reg(6)
  DiffBridge.io.REG_7:=diff.reg(7)
  DiffBridge.io.REG_8:=diff.reg(8)
  DiffBridge.io.REG_9:=diff.reg(9)
  DiffBridge.io.REG_10:=diff.reg(10)
  DiffBridge.io.REG_11:=diff.reg(11)
  DiffBridge.io.REG_12:=diff.reg(12)
  DiffBridge.io.REG_13:=diff.reg(13)
  DiffBridge.io.REG_14:=diff.reg(14)
  DiffBridge.io.REG_15:=diff.reg(15)
  DiffBridge.io.REG_16:=diff.reg(16)
  DiffBridge.io.REG_17:=diff.reg(17)
  DiffBridge.io.REG_18:=diff.reg(18)
  DiffBridge.io.REG_19:=diff.reg(19)
  DiffBridge.io.REG_20:=diff.reg(20)
  DiffBridge.io.REG_21:=diff.reg(21)
  DiffBridge.io.REG_22:=diff.reg(22)
  DiffBridge.io.REG_23:=diff.reg(23)
  DiffBridge.io.REG_24:=diff.reg(24)
  DiffBridge.io.REG_25:=diff.reg(25)
  DiffBridge.io.REG_26:=diff.reg(26)
  DiffBridge.io.REG_27:=diff.reg(27)
  DiffBridge.io.REG_28:=diff.reg(28)
  DiffBridge.io.REG_29:=diff.reg(29)
  DiffBridge.io.REG_30:=diff.reg(30)
  DiffBridge.io.REG_31:=diff.reg(31)
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

    val loadIndex = Input(UInt(8.W))
    val loadValid = Input(UInt(8.W))
    val loadPaddr = Input(UInt(64.W))
    val loadVaddr = Input(UInt(64.W))
    val loadData  = Input(UInt(64.W))

    val mstatus = Input(UInt(64.W))
    val mtvec = Input(UInt(64.W))
    val mepc = Input(UInt(64.W))
    val mcause = Input(UInt(64.W))

    val REG = Input(UInt(64.W))
    val REG_1 = Input(UInt(64.W))
    val REG_2 = Input(UInt(64.W))
    val REG_3 = Input(UInt(64.W))
    val REG_4 = Input(UInt(64.W))
    val REG_5 = Input(UInt(64.W))
    val REG_6 = Input(UInt(64.W))
    val REG_7 = Input(UInt(64.W))
    val REG_8 = Input(UInt(64.W))
    val REG_9 = Input(UInt(64.W))
    val REG_10 = Input(UInt(64.W))
    val REG_11 = Input(UInt(64.W))
    val REG_12 = Input(UInt(64.W))
    val REG_13 = Input(UInt(64.W))
    val REG_14 = Input(UInt(64.W))
    val REG_15 = Input(UInt(64.W))
    val REG_16 = Input(UInt(64.W))
    val REG_17 = Input(UInt(64.W))
    val REG_18 = Input(UInt(64.W))
    val REG_19 = Input(UInt(64.W))
    val REG_20 = Input(UInt(64.W))
    val REG_21 = Input(UInt(64.W))
    val REG_22 = Input(UInt(64.W))
    val REG_23 = Input(UInt(64.W))
    val REG_24 = Input(UInt(64.W))
    val REG_25 = Input(UInt(64.W))
    val REG_26 = Input(UInt(64.W))
    val REG_27 = Input(UInt(64.W))
    val REG_28 = Input(UInt(64.W))
    val REG_29 = Input(UInt(64.W))
    val REG_30 = Input(UInt(64.W))
    val REG_31 = Input(UInt(64.W))
  })
  addPath("playground/src/DiffTest/DiffBridge.v")

}