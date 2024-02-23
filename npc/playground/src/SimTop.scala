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
  val Sram = Module(new Sram())



// IF begin
  IF_stage.IF.for_id<>ID_stage.ID.to_if
  IF_stage.IF.for_ex<>EX_stage.EX.to_if

// ID begin
  StageConnect(IF_stage.IF.to_id,ID_stage.ID.IO) //左边是out，右边是in
  ID_stage.ID.for_ex<>EX_stage.EX.to_id
  ID_stage.ID.for_ls<>LS_stage.LS.to_id
  ID_stage.ID.for_wb<>WB_stage.WB.to_id

// EX begin
  StageConnect(ID_stage.ID.to_ex,EX_stage.EX.IO)
  Sram.io.in:=EX_stage.EX.data_sram

// LS begin
  StageConnect(EX_stage.EX.to_ls,LS_stage.LS.IO)
  LS_stage.LS.data_sram:=Sram.io.out

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