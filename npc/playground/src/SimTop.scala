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
  val PF_stage = Module(new PreIF_stage())
  val IF_stage = Module(new IF_stage())
  val ID_stage = Module(new ID_stage())
  val EX_stage = Module(new EX_stage())
  val LS_stage = Module(new LS_stage())
  val WB_stage = Module(new WB_stage())
  
  val Axi4Lite_Sram_Mem = Module(new Axi4Lite_Sram_Mem())
  val Axi4Lite_Sram_If=Module(new Axi4Lite_Sram_If())
  val AXi4LiteBridge=Module(new Axi4Bridge())
  val AXi4LiteBridgeIF=Module(new Axi4Bridge())
//AxiBridge
  AXi4LiteBridge.io.ar<>Axi4Lite_Sram_Mem.io.ar
  AXi4LiteBridge.io.r <>Axi4Lite_Sram_Mem.io.r
  AXi4LiteBridge.io.aw<>Axi4Lite_Sram_Mem.io.aw
  AXi4LiteBridge.io.w <>Axi4Lite_Sram_Mem.io.w
  AXi4LiteBridge.io.b <>Axi4Lite_Sram_Mem.io.b

  AXi4LiteBridgeIF.io.ar<>Axi4Lite_Sram_If.io.ar
  AXi4LiteBridgeIF.io.r <>Axi4Lite_Sram_If.io.r
  AXi4LiteBridgeIF.io.aw<>Axi4Lite_Sram_If.io.aw
  AXi4LiteBridgeIF.io.w <>Axi4Lite_Sram_If.io.w
  AXi4LiteBridgeIF.io.b <>Axi4Lite_Sram_If.io.b
//AxiBridge

// PreIF begin
  PF_stage.PF.for_id<>ID_stage.ID.to_pf
  PF_stage.PF.for_ex<>EX_stage.EX.to_pf

  AXi4LiteBridgeIF.io.al<>PF_stage.PF.al
  AXi4LiteBridgeIF.io.s<>PF_stage.PF.s


// IF begin
  StageConnect(PF_stage.PF.to_if,IF_stage.IF.IO)
  IF_stage.IF.for_id<>ID_stage.ID.to_if
  IF_stage.IF.for_ex<>EX_stage.EX.to_if

  IF_stage.IF.dl<>AXi4LiteBridgeIF.io.dl

// ID begin
  StageConnect(IF_stage.IF.to_id,ID_stage.ID.IO) //左边是out，右边是in
  ID_stage.ID.for_ex<>EX_stage.EX.to_id
  ID_stage.ID.for_ls<>LS_stage.LS.to_id
  ID_stage.ID.for_wb<>WB_stage.WB.to_id

// EX begin
  StageConnect(ID_stage.ID.to_ex,EX_stage.EX.IO)
  AXi4LiteBridge.io.al<>EX_stage.EX.al
  AXi4LiteBridge.io.s <>EX_stage.EX.s

// LS begin
  StageConnect(EX_stage.EX.to_ls,LS_stage.LS.IO)
  LS_stage.LS.dl<>AXi4LiteBridge.io.dl

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