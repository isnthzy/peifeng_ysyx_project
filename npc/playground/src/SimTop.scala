import chisel3._
import chisel3.util._
import CoreConfig.Configs._
import CoreConfig.DeviceConfig
import PipeLine.{PfStage,IfStage,IdStage,ExStage,LsStage,WbStage}
import Axi.{Axi4Bridge,AxiArbiter,AxiXbarA2X,Axi4Master,AxiTopBundle,AxiCoreOut}
import DiffTest.DiffCommit
import FuncUnit.CsrFile
import IP.Axi4LiteSram
import CoreConfig.GenCtrl
import DiffTest.dpic._
import Device.{SimTimer}
import CoreConfig.ISAConfig

class SimTop extends Module with DeviceConfig{
  override val desiredName = "ysyx_23060115"
  val io = IO(new Bundle {
    val interrupt=if(ISAConfig.SOC_MODE) Some(Input(Bool())) else None
    val master=if(ISAConfig.SOC_MODE) Some(new AxiTopBundle()) else None
    val slave=if(ISAConfig.SOC_MODE) Some(Flipped(new AxiTopBundle())) else None
  })
  val PreFetch  = Module(new PfStage())
  val InstFetch = Module(new IfStage())
  val InstDecode= Module(new IdStage())
  val Execute   = Module(new ExStage())
  val LoadStore = Module(new LsStage())
  val WriteBack = Module(new WbStage())
  val CsrFile   = Module(new CsrFile())

//
  val Axi4Bridge=Module(new Axi4Bridge())
  val AxiArbiter=Module(new AxiArbiter())
  val AxiCoreOut=Module(new AxiCoreOut())
//
if(ISAConfig.SOC_MODE){
  io.master.get<>AxiCoreOut.io.out

  io.slave.get.awready:=0.U
  io.slave.get.wready :=0.U
  io.slave.get.bvalid :=0.U
  io.slave.get.bid    :=0.U
  io.slave.get.bresp  :=0.U
  io.slave.get.arready:=0.U
  io.slave.get.rvalid :=0.U
  io.slave.get.rid    :=0.U
  io.slave.get.rdata  :=0.U
  io.slave.get.rresp  :=0.U
  io.slave.get.rlast  :=0.U
}
//AxiArbiter
  AxiArbiter.io.fs.al<>PreFetch.pf.al
  AxiArbiter.io.fs.s <>PreFetch.pf.s
  AxiArbiter.io.fs.dl<>InstFetch.fs.dl

  AxiArbiter.io.ls.al<>Execute.ex.al
  AxiArbiter.io.ls.s <>Execute.ex.s
  AxiArbiter.io.ls.dl<>LoadStore.ls.dl

  Axi4Bridge.io.al<>AxiArbiter.io.out.al
  Axi4Bridge.io.s <>AxiArbiter.io.out.s
  Axi4Bridge.io.dl<>AxiArbiter.io.out.dl
//AxiArbiter

// //AxiXBar
if(ISAConfig.SOC_MODE){
  val SimTimer = Module(new SimTimer())

  val AxiXbarA2X = Module(new AxiXbarA2X(
    List(
      (0x02010000L , 0xFDFF0000L, false), //第一行也为默认转发地址，当后面都未命中，转发到默认地址
      (0x02000000L , 0x10000L   , false),
    )
  ))
  Axi4Bridge.io.ar<>AxiXbarA2X.io.a.ar
  Axi4Bridge.io.r <>AxiXbarA2X.io.a.r
  Axi4Bridge.io.aw<>AxiXbarA2X.io.a.aw
  Axi4Bridge.io.w <>AxiXbarA2X.io.a.w
  Axi4Bridge.io.b <>AxiXbarA2X.io.a.b

  AxiXbarA2X.io.x(0)<>AxiCoreOut.io.in
  AxiXbarA2X.io.x(1)<>SimTimer.io
}else{
  val AxiRam = Module(new Axi4LiteSram())
  Axi4Bridge.io.ar<>AxiRam.io.ar
  Axi4Bridge.io.r <>AxiRam.io.r
  Axi4Bridge.io.aw<>AxiRam.io.aw
  Axi4Bridge.io.w <>AxiRam.io.w
  Axi4Bridge.io.b <>AxiRam.io.b

}
//
  //NOTE:为了perf加的丑陋的飞线

  var programExit=(LoadStore.ls.to_wb.bits.diffExcp.excpValid
                 &&LoadStore.ls.to_wb.bits.diffExcp.cause===0x3.U
                 &&LoadStore.ls.to_wb.fire)
  if(GenCtrl.PERF){
    PreFetch.pf.perfMode:=InstFetch.fs.perfMode
    PreFetch.pf.programExit:=RegNext(programExit)
    InstFetch.fs.programExit:=RegNext(programExit)
  }else{
    PreFetch.pf.perfMode:=false.B
    PreFetch.pf.programExit:=false.B
    InstFetch.fs.programExit:=false.B
  }



// PreIF begin
  PreFetch.pf.from_id:=InstDecode.id.fw_pf
  PreFetch.pf.from_ex:=Execute.ex.fw_pf
  PreFetch.pf.from_ls:=LoadStore.ls.fw_pf
  PreFetch.pf.csrEntries:=CsrFile.io.csrEntries

// if begin
  StageConnect(PreFetch.pf.to_if,InstFetch.fs.in)
  InstFetch.fs.from_id:=InstDecode.id.fw_if
  InstFetch.fs.from_ex:=Execute.ex.fw_if
  InstFetch.fs.from_ls:=LoadStore.ls.fw_if

// id begin
  StageConnect(InstFetch.fs.to_id,InstDecode.id.in) //左边是out，右边是in
  InstDecode.id.from_ex:=Execute.ex.fw_id
  InstDecode.id.from_ls:=LoadStore.ls.fw_id
  InstDecode.id.from_wb:=WriteBack.wb.fw_id
  InstDecode.id.from_csr<>CsrFile.io.from_csr

// ex begin
  StageConnect(InstDecode.id.to_ex,Execute.ex.in)
  Execute.ex.from_ls:=LoadStore.ls.fw_ex
// ls begin
  StageConnect(Execute.ex.to_ls,LoadStore.ls.in)
  LoadStore.ls.to_csr<>CsrFile.io.to_csr
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

