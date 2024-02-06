import chisel3._
import chisel3.util._
import config.Configs._

// class Message extends Bundle {
//   val inst = Output(UInt(32.W))
//   val pc   = Output(UInt(32.W))
// }
// class IFU extends Module {
//   val io = IO(new Bundle {
//     val inst=Input(UInt(32.W))
//     val out = Decoupled(new Message)
//   })

//   io.out.bits.inst :=io.inst
//   io.out.bits.pc := 4.U
//   io.out.valid := true.B
// }
// class IDU extends Module {
//   val io = IO(new Bundle {
//     val in = Flipped(Decoupled(new Message))
//   })
//   io.in.ready:=true.B
//   // ...
// }
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
  ID_stage.ID.IO:=IF_stage.IF.IO
  ID_stage.ID.wb_bus:=WB_stage.WB.to_id
// EX begin
  EX_stage.EX.IO:=ID_stage.ID.to_ex
// lS begin
  LS_stage.LS.IO:=EX_stage.EX.to_ls
// WB begin
  WB_stage.WB.IO:=LS_stage.LS.to_wb

//debug
  io.debug_waddr:=WB_stage.WB.debug_waddr
  io.debug_wdata:=WB_stage.WB.debug_wdata
  io.debug_wen  :=WB_stage.WB.debug_wen
}

