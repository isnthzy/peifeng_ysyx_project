import chisel3._
import chisel3.util._
import config.Configs._

class SimTop extends Module {
  val io = IO(new Bundle {
    val debug_waddr=Output(UInt(5.W))
    val debug_wdata=Output(UInt(DATA_WIDTH.W))
    val debug_wen  =Output(Bool())
  })
  val IF_stage = Module(new IF_stage())
  val ID_stage = Module(new ID_stage())
  val EX_stage = Module(new EX_stage())
  val LS_stage = Module(new LS_stage())
  val WB_stage = Module(new WB_stage())
// IF begin
  IF_stage.IF.br_bus:=EX_stage.EX.br_bus
  IF_stage.IF.epc_bus:=WB_stage.WB.to_if
// ID begin
  StageConnect(IF_stage.IF.IO,ID_stage.ID.IO) //左边是out，右边是in
  ID_stage.ID.wb_bus:=WB_stage.WB.to_id
// EX begin
  StageConnect(ID_stage.ID.to_ex,EX_stage.EX.IO)
// lS begin
  StageConnect(EX_stage.EX.to_ls,LS_stage.LS.IO)
// WB begin
  StageConnect(LS_stage.LS.to_wb,WB_stage.WB.IO)


//debug
  io.debug_waddr:=WB_stage.WB.debug_waddr
  io.debug_wdata:=WB_stage.WB.debug_wdata
  io.debug_wen  :=WB_stage.WB.debug_wen
}

object StageConnect {
  def apply[T <: Data](out: DecoupledIO[T], in: DecoupledIO[T]) = {
    val arch = "single"
    // 为展示抽象的思想, 此处代码省略了若干细节
    if      (arch == "single"){ 
      in.valid:=true.B
      in.bits :=out.bits 
    }
    else if (arch == "multi"){ 
      in <> out
    }
    // else if (arch == "pipeline") { in <> RegEnable(in, in.fire) }
    // else if (arch == "ooo")      { in <> Queue(in, 16) }
  }
}