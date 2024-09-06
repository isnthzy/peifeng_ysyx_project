import chisel3._
import chisel3.util._
import config.Configs._
import PipeLine.{PfStage,IfStage,IdStage,ExStage,LsStage,WbStage}
import Axi.{Axi4Bridge,AxiArbiter}
import DiffTest.DiffCommit
import FuncUnit.CsrFile
import IP.Axi4LiteSram
import config.GenCtrl

class SimTop extends Module {
  val io = IO(new Bundle {
    val debug_pc   =Output(UInt(ADDR_WIDTH.W))
    val debug_waddr=Output(UInt(5.W))
    val debug_wdata=Output(UInt(DATA_WIDTH.W))
    val debug_wen  =Output(Bool())
  })
  val PreFetch  = Module(new PfStage())
  val InstFetch = Module(new IfStage())
  val InstDecode= Module(new IdStage())
  val Execute   = Module(new ExStage())
  val LoadStore = Module(new LsStage())
  val WriteBack = Module(new WbStage())
  val CsrFile   = Module(new CsrFile())
  
  val Axi4LiteSram = Module(new Axi4LiteSram())
  val Axi4LiteBridge=Module(new Axi4Bridge())
  val AxiArbiter=Module(new AxiArbiter())
//AxiBridge
  Axi4LiteBridge.io.ar<>Axi4LiteSram.io.ar
  Axi4LiteBridge.io.r <>Axi4LiteSram.io.r
  Axi4LiteBridge.io.aw<>Axi4LiteSram.io.aw
  Axi4LiteBridge.io.w <>Axi4LiteSram.io.w
  Axi4LiteBridge.io.b <>Axi4LiteSram.io.b

//AxiBridge

//AxiArbiter
  AxiArbiter.io.fs.al<>PreFetch.pf.al
  AxiArbiter.io.fs.s <>PreFetch.pf.s
  AxiArbiter.io.fs.dl<>InstFetch.fs.dl

  AxiArbiter.io.ls.al<>Execute.ex.al
  AxiArbiter.io.ls.s <>Execute.ex.s
  AxiArbiter.io.ls.dl<>LoadStore.ls.dl

  Axi4LiteBridge.io.al<>AxiArbiter.io.out.al
  Axi4LiteBridge.io.s <>AxiArbiter.io.out.s
  Axi4LiteBridge.io.dl<>AxiArbiter.io.out.dl
//AxiArbiter

// PreIF begin
  PreFetch.pf.from_id<>InstDecode.id.fw_pf
  PreFetch.pf.from_ex<>Execute.ex.fw_pf

// if begin
  StageConnect(PreFetch.pf.to_if,InstFetch.fs.in)
  InstFetch.fs.from_id<>InstDecode.id.fw_if
  InstFetch.fs.from_ex<>Execute.ex.fw_if

// id begin
  StageConnect(InstFetch.fs.to_id,InstDecode.id.in) //左边是out，右边是in
  InstDecode.id.from_ex<>Execute.ex.fw_id
  InstDecode.id.from_ls<>LoadStore.ls.fw_id
  InstDecode.id.from_wb<>WriteBack.wb.fw_id

// ex begin
  StageConnect(InstDecode.id.to_ex,Execute.ex.in)

// ls begin
  StageConnect(Execute.ex.to_ls,LoadStore.ls.in)

// wb begin
  StageConnect(LoadStore.ls.to_wb,WriteBack.wb.in)

  if(GenCtrl.VERILATOR_SIM){
    val DiffCommit= Module(new DiffCommit())
    DiffCommit.diff.instr:=WriteBack.wb.diffInstrCommit
    DiffCommit.diff.load :=WriteBack.wb.diffLoadCommit
    DiffCommit.diff.store:=WriteBack.wb.diffStoreCommit
    DiffCommit.diff.excp :=WriteBack.wb.diffExcpCommit
    DiffCommit.diff.csr  :=CsrFile.io.diffCSR
    DiffCommit.diff.reg  :=InstDecode.id.diffREG
  }

  io.debug_pc   :=WriteBack.wb.diffInstrCommit.pc 
  io.debug_waddr:=WriteBack.wb.diffInstrCommit.waddr
  io.debug_wdata:=WriteBack.wb.diffInstrCommit.wdata
  io.debug_wen  :=WriteBack.wb.diffInstrCommit.wen
}

object StageConnect {
  def apply[T <: Data](out: DecoupledIO[T], in: DecoupledIO[T]) = {
    val arch = "pipeline"
    if (arch == "pipeline") { 
      out.ready:=in.ready
      in.valid:=out.valid
      in.bits <> RegEnable(out.bits, out.fire) 
    }
  }
}