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
  val PreIF_s  = Module(new PreIF_s())
  val IF_stage = Module(new IF_stage())
  val ID_stage = Module(new ID_stage())
  val EX_stage = Module(new EX_stage())
  val LS_stage = Module(new LS_stage())
  val WB_stage = Module(new WB_stage())
  
  val Axi4Lite_Sram_Mem = Module(new Axi4Lite_Sram_Mem())
  val Axi4Lite_Sram_If=Module(new Axi4Lite_Sram_If())
  val AXi4LiteBridge=Module(new Axi4Bridge())

//AxiBridge
  AXi4LiteBridge.io.ar<>Axi4Lite_Sram_Mem.io.ar
  AXi4LiteBridge.io.r <>Axi4Lite_Sram_Mem.io.r
  AXi4LiteBridge.io.aw<>Axi4Lite_Sram_Mem.io.aw
  AXi4LiteBridge.io.w <>Axi4Lite_Sram_Mem.io.w
  AXi4LiteBridge.io.b <>Axi4Lite_Sram_Mem.io.b
//AxiBridge

// PreIF begin
  dontTouch(PreIF_s.PreIF.to_if.bits.pc)
  dontTouch(PreIF_s.PreIF.to_if.bits.nextpc)
  PreIF_s.PreIF.for_id<>ID_stage.ID.to_preif
  PreIF_s.PreIF.for_ex<>EX_stage.EX.to_preif
  PreIF_s.PreIF.ar<>Axi4Lite_Sram_If.io.ar
  PreIF_s.PreIF.aw<>Axi4Lite_Sram_If.io.aw
  PreIF_s.PreIF.w <>Axi4Lite_Sram_If.io.w
  PreIF_s.PreIF.b <>Axi4Lite_Sram_If.io.b
// IF begin
  StageConnect(PreIF_s.PreIF.to_if,IF_stage.IF.IO)
  IF_stage.IF.for_id<>ID_stage.ID.to_if
  IF_stage.IF.for_ex<>EX_stage.EX.to_if
  IF_stage.IF.r <>Axi4Lite_Sram_If.io.r

// ID begin
  StageConnect(IF_stage.IF.to_id,ID_stage.ID.IO) //左边是out，右边是in
  ID_stage.ID.for_ex<>EX_stage.EX.to_id
  ID_stage.ID.for_ls<>LS_stage.LS.to_id
  ID_stage.ID.for_wb<>WB_stage.WB.to_id

// EX begin
  StageConnect(ID_stage.ID.to_ex,EX_stage.EX.IO)
  AXi4LiteBridge.io.addr:=EX_stage.EX.mem_addr
  AXi4LiteBridge.io.write_en:=EX_stage.EX.write_en
  AXi4LiteBridge.io.wstrb:=EX_stage.EX.wstrb
  AXi4LiteBridge.io.wdata:=EX_stage.EX.wdata
  EX_stage.EX.waddr_ok:=AXi4LiteBridge.io.waddr_ok
  EX_stage.EX.wdata_ok:=AXi4LiteBridge.io.wdata_ok
  
  AXi4LiteBridge.io.read_en:=EX_stage.EX.read_en
  EX_stage.EX.raddr_ok:=AXi4LiteBridge.io.raddr_ok

// LS begin
  StageConnect(EX_stage.EX.to_ls,LS_stage.LS.IO)
  LS_stage.LS.rdata:=AXi4LiteBridge.io.rdata
  LS_stage.LS.rdata_ok:=AXi4LiteBridge.io.rdata_ok

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