import chisel3._
import chisel3.util._
import CoreConfig.Configs._
import CoreConfig.DeviceConfig
import PipeLine.{PfStage,IfStage,IdStage,ExStage,LsStage,WbStage}
import Axi.{Axi4Bridge,AxiArbiter,AxiXbarA2X}
import DiffTest.DiffCommit
import FuncUnit.CsrFile
import IP.Axi4LiteSram
import CoreConfig.GenCtrl
import DiffTest.dpic._
import Device.{SimTimer}

class SimTop extends Module with DeviceConfig{
  override val desiredName = "ysyx_23060115"
  val io = IO(new Bundle {
    val interrupt=Input(Bool())
    val master=new AxiTopBundle()
    val slave=Flipped(new AxiTopBundle())
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
//
  Axi4Bridge.io.aw.ready:=io.master.awready
  io.master.awvalid:=Axi4Bridge.io.aw.valid
  io.master.awaddr :=Axi4Bridge.io.aw.bits.addr
  io.master.awid   :=Axi4Bridge.io.aw.bits.id
  io.master.awlen  :=Axi4Bridge.io.aw.bits.len
  io.master.awsize :=Axi4Bridge.io.aw.bits.size
  io.master.awburst:=Axi4Bridge.io.aw.bits.burst

  Axi4Bridge.io.w.ready:=io.master.wready
  io.master.wvalid:=Axi4Bridge.io.w.valid
  io.master.wdata :=Axi4Bridge.io.w.bits.data
  io.master.wstrb :=Axi4Bridge.io.w.bits.strb
  io.master.wlast :=Axi4Bridge.io.w.bits.last

  io.master.bready:=Axi4Bridge.io.b.ready
  Axi4Bridge.io.b.valid:=io.master.bvalid
  Axi4Bridge.io.b.bits.resp:=io.master.bresp
  Axi4Bridge.io.b.bits.id  :=io.master.bid

  Axi4Bridge.io.ar.ready:=io.master.arready
  io.master.arvalid:=Axi4Bridge.io.ar.valid
  io.master.araddr :=Axi4Bridge.io.ar.bits.addr
  io.master.arid   :=Axi4Bridge.io.ar.bits.id
  io.master.arlen  :=Axi4Bridge.io.ar.bits.len
  io.master.arsize :=Axi4Bridge.io.ar.bits.size
  io.master.arburst:=Axi4Bridge.io.ar.bits.burst

  io.master.rready:=Axi4Bridge.io.r.ready
  Axi4Bridge.io.r.valid:=io.master.rvalid
  Axi4Bridge.io.r.bits.data:=io.master.rdata
  Axi4Bridge.io.r.bits.resp:=io.master.rresp
  Axi4Bridge.io.r.bits.id  :=io.master.rid
  Axi4Bridge.io.r.bits.last:=io.master.rlast
//Axi4Bridge


  io.slave.awready:=0.U
  io.slave.wready :=0.U
  io.slave.bvalid :=0.U
  io.slave.bid    :=0.U
  io.slave.bresp  :=0.U
  io.slave.arready:=0.U
  io.slave.rvalid :=0.U
  io.slave.rid    :=0.U
  io.slave.rdata  :=0.U
  io.slave.rresp  :=0.U
  io.slave.rlast  :=0.U
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
//   val Axi4LiteSram = Module(new Axi4LiteSram())
//   val SimUart  = Module(new SimUart())
//   val SimTimer = Module(new SimTimer())

//   val AxiXbarA2X = Module(new AxiXbarA2X(
//     List(
//       (0x80000000L , 0x8000000L    , false),
//       (0xa00003f8L , 0x0L          , false),
//       (0xa0000048L , 0x0L          , false),
//     )
//   ))
//   Axi4Bridge.io.ar<>AxiXbarA2X.io.a.ar
//   Axi4Bridge.io.r <>AxiXbarA2X.io.a.r
//   Axi4Bridge.io.aw<>AxiXbarA2X.io.a.aw
//   Axi4Bridge.io.w <>AxiXbarA2X.io.a.w
//   Axi4Bridge.io.b <>AxiXbarA2X.io.a.b

//   AxiXbarA2X.io.x(0)<>Axi4LiteSram.io
//   AxiXbarA2X.io.x(1)<>SimUart.io
//   AxiXbarA2X.io.x(2)<>SimTimer.io
//

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

class AxiTopBundle extends Bundle {
  val awready=Input(Bool())
  val awvalid=Output(Bool())
  val awaddr =Output(UInt(ADDR_WIDTH.W))
  val awid   =Output(UInt(4.W))
  val awlen  =Output(UInt(8.W))
  val awsize =Output(UInt(3.W))
  val awburst=Output(UInt(2.W))

  val wready =Input(Bool())
  val wvalid =Output(Bool())
  val wdata  =Output(UInt(DATA_WIDTH.W))
  val wstrb  =Output(UInt((DATA_WIDTH/8).W))
  val wlast  =Output(Bool())

  val bready =Output(Bool())
  val bvalid =Input(Bool())
  val bresp  =Input(UInt(2.W))
  val bid    =Input(UInt(4.W))

  val arready=Input(Bool())
  val arvalid=Output(Bool())
  val araddr =Output(UInt(ADDR_WIDTH.W))
  val arid   =Output(UInt(4.W))
  val arlen  =Output(UInt(8.W))
  val arsize =Output(UInt(3.W))
  val arburst=Output(UInt(2.W))
  
  val rready =Output(Bool())
  val rvalid =Input(Bool())
  val rresp  =Input(UInt(2.W))
  val rdata  =Input(UInt(DATA_WIDTH.W))
  val rlast  =Input(Bool())
  val rid    =Input(UInt(4.W))
}