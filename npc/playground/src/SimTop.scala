import chisel3._
import chisel3.util._
import config.Configs._

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
// PreIF begin
  dontTouch(PreIF_s.PreIF.to_if)
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
  // IF_stage.IF.ar<>Axi4Lite_Sram_If.io.ar
  // IF_stage.IF.r <>Axi4Lite_Sram_If.io.r
  // IF_stage.IF.aw<>Axi4Lite_Sram_If.io.aw
  // IF_stage.IF.w <>Axi4Lite_Sram_If.io.w 
  // IF_stage.IF.b <>Axi4Lite_Sram_If.io.b
// ID begin
  StageConnect(IF_stage.IF.to_id,ID_stage.ID.IO) //左边是out，右边是in
  ID_stage.ID.for_ex<>EX_stage.EX.to_id
  ID_stage.ID.for_ls<>LS_stage.LS.to_id
  ID_stage.ID.for_wb<>WB_stage.WB.to_id

// EX begin
  StageConnect(ID_stage.ID.to_ex,EX_stage.EX.IO)
  EX_stage.EX.ar<>Axi4Lite_Sram_Mem.io.ar
  EX_stage.EX.aw<>Axi4Lite_Sram_Mem.io.aw
  EX_stage.EX.w <>Axi4Lite_Sram_Mem.io.w 
  EX_stage.EX.b <>Axi4Lite_Sram_Mem.io.b
  
// LS begin
  StageConnect(EX_stage.EX.to_ls,LS_stage.LS.IO)
  LS_stage.LS.r<>Axi4Lite_Sram_Mem.io.r

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