import chisel3._
import chisel3.util._
import config.Configs._
import java.awt.MouseInfo

class SimTop extends Module {
  val io = IO(new Bundle {
    val debug_waddr=Output(UInt(5.W))
    val debug_wdata=Output(UInt(DATA_WIDTH.W))
    val debug_wen  =Output(Bool())
  })
  val PF_stage = Module(new PF_stage())
  val IF_stage = Module(new IF_stage())
  val ID_stage = Module(new ID_stage())
  val EX_stage = Module(new EX_stage())
  val LS_stage = Module(new LS_stage())
  val WB_stage = Module(new WB_stage())
  
  val Axi4Lite_Sram_Mem = Module(new Axi4Lite_Sram_Mem())
  val Axi4Lite_Sram_If=Module(new Axi4Lite_Sram_If())
  val Axi4LiteBridge=Module(new Axi4Bridge())
  val Axi4LiteBridgeIF=Module(new Axi4Bridge())
  val AxiArbiter=Module(new AxiArbiter())
//AxiBridge
  Axi4LiteBridge.io.ar<>Axi4Lite_Sram_Mem.io.ar
  Axi4LiteBridge.io.r <>Axi4Lite_Sram_Mem.io.r
  Axi4LiteBridge.io.aw<>Axi4Lite_Sram_Mem.io.aw
  Axi4LiteBridge.io.w <>Axi4Lite_Sram_Mem.io.w
  Axi4LiteBridge.io.b <>Axi4Lite_Sram_Mem.io.b

  // Axi4LiteBridgeIF.io.ar<>Axi4Lite_Sram_If.io.ar
  // Axi4LiteBridgeIF.io.r <>Axi4Lite_Sram_If.io.r
  // Axi4LiteBridgeIF.io.aw<>Axi4Lite_Sram_If.io.aw
  // Axi4LiteBridgeIF.io.w <>Axi4Lite_Sram_If.io.w
  // Axi4LiteBridgeIF.io.b <>Axi4Lite_Sram_If.io.b
//AxiBridge

//AxiArbiter
  AxiArbiter.io.fs.al<>PF_stage.PF.al
  AxiArbiter.io.fs.s <>PF_stage.PF.s
  AxiArbiter.io.fs.dl<>IF_stage.IF.dl

  AxiArbiter.io.ls.al<>EX_stage.EX.al
  AxiArbiter.io.ls.s <>EX_stage.EX.s
  AxiArbiter.io.ls.dl<>LS_stage.LS.dl

  Axi4LiteBridge.io.al<>AxiArbiter.io.out.al
  Axi4LiteBridge.io.s <>AxiArbiter.io.out.s
  Axi4LiteBridge.io.dl<>AxiArbiter.io.out.dl
//AxiArbiter

// PreIF begin
  PF_stage.PF.for_id<>ID_stage.ID.to_pf
  PF_stage.PF.for_ex<>EX_stage.EX.to_pf

  Axi4LiteBridgeIF.io.al<>PF_stage.PF.al
  Axi4LiteBridgeIF.io.s <>PF_stage.PF.s


// IF begin
  StageConnect(PF_stage.PF.to_if,IF_stage.IF.IO)
  IF_stage.IF.for_id<>ID_stage.ID.to_if
  IF_stage.IF.for_ex<>EX_stage.EX.to_if

  IF_stage.IF.dl<>Axi4LiteBridgeIF.io.dl

// ID begin
  StageConnect(IF_stage.IF.to_id,ID_stage.ID.IO) //左边是out，右边是in
  ID_stage.ID.for_ex<>EX_stage.EX.to_id
  ID_stage.ID.for_ls<>LS_stage.LS.to_id
  ID_stage.ID.for_wb<>WB_stage.WB.to_id

// EX begin
  StageConnect(ID_stage.ID.to_ex,EX_stage.EX.IO)
  Axi4LiteBridge.io.al<>EX_stage.EX.al
  Axi4LiteBridge.io.s <>EX_stage.EX.s

// LS begin
  StageConnect(EX_stage.EX.to_ls,LS_stage.LS.IO)
  LS_stage.LS.dl<>Axi4LiteBridge.io.dl

// WB begin
  StageConnect(LS_stage.LS.to_wb,WB_stage.WB.IO)


//debug
  io.debug_waddr:=WB_stage.WB.debug_waddr
  io.debug_wdata:=WB_stage.WB.debug_wdata
  io.debug_wen  :=WB_stage.WB.debug_wen
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