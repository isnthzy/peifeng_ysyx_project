import chisel3._
import chisel3.util._  
import config.Configs._

class SimTop extends Module {
  val io = IO(new Bundle {
    val result=Output(UInt(32.W))
  })

//定义变量 
  val Imm   =dontTouch(Wire(UInt(32.W)))
  val pc    =dontTouch(Wire(UInt(32.W)))
  val nextpc=dontTouch(Wire(UInt(32.W)))
  val inst  =dontTouch(Wire(UInt(32.W)))
  val is_not_jalr=dontTouch(Wire(Bool()))
  val is_jump=dontTouch(Wire(Bool()))
  val jalr_taget=dontTouch(Wire(UInt(32.W)))
  val d_ebus=Wire(new id_to_es_bus())
// IFU begin
  val IF_stage=Module(new IF_stage())
  pc:=IF_stage.io.pc
  nextpc:=IF_stage.io.nextpc
  inst:=IF_stage.io.inst
  IF_stage.io.Imm:=Imm
  IF_stage.io.jalr_taget:=jalr_taget
  IF_stage.io.is_not_jalr:=is_not_jalr
  IF_stage.io.is_jump:=is_jump
// IDU begin
  val ID_stage=Module(new ID_stage())
  ID_stage.io.inst:=0.U
  ID_stage.io.pc:=0.U
  ID_stage.io.nextpc:=0.U
  ID_stage.io.result:=io.result
  Imm:=ID_stage.io.Imm
  is_jump:=ID_stage.io.is_jump
  is_not_jalr:=ID_stage.io.is_not_jalr

  ID_stage.io.f_dbus:=IF_stage.io.f_dbus
  d_ebus:=ID_stage.io.d_ebus
// EXU begin
  val EXE_stage=Module(new EXE_stage())
  EXE_stage.io.pc:=pc
  jalr_taget:=EXE_stage.io.jalr_taget
  io.result:=EXE_stage.io.result

  // EXE_stage.io.d_ebus:=ID_stage.io.d_ebus
  
// WB begin
}


