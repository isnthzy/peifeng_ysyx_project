import chisel3._
import chisel3.util._  
import config.Configs._

class SimTop extends Module {
  val io = IO(new Bundle {
    val result=Output(UInt(32.W))
    val wen=Output(Bool())
    val imm=Output(UInt(32.W))
  })

//定义变量 
  val pc    =dontTouch(Wire(UInt(32.W)))
  val nextpc=dontTouch(Wire(UInt(32.W)))
  val inst  =dontTouch(Wire(UInt(32.W)))
  val is_not_jalr=dontTouch(Wire(UInt(32.W)))
  val is_jump=dontTouch(Wire(UInt(32.W)))
  val id_to_es_bus=dontTouch(Wire(UInt(ID_TO_ES_BUS_WD.W)))

// IFU begin
  val IF_stage=Module(new IF_stage())
  pc:=IF_stage.io.pc
  nextpc:=IF_stage.io.nextpc
  inst:=IF_stage.io.inst
  IF_stage.io.is_not_jalr=is_not_jalr
  IF_stage.io.is_jump=is_jump
// IDU begin
  val ID_stage=Module(new ID_stage())
  ID_stage.io.inst:=inst
  is_jump:=ID_stage.io.is_jump
  is_not_jalr:=ID_stage.io.is_not_jalr
  id_to_es_bus:=ID_stage.io.i_ebus
// EXU begin
  val EXE_stage=Module(new EXE_stage())
  EXE_stage.io.i_ebus:=id_to_es_bus
// WB begin
}

class singal_dpi extends BlackBox with HasBlackBoxPath{
  val io=IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val pc=Input(UInt(32.W))
    val nextpc=Input(UInt(32.W))
    val inst=Input(UInt(32.W))
    val rd=Input(UInt(32.W))
    val is_jal=Input(Bool())
    val func_flag=Input(Bool())
    val ebreak_flag=Input(Bool())
    val inv_flag=Input(Bool()) //inv -> inst not vaild 无效的指令
    val ret_reg=Input(UInt(32.W))
  })
  addPath("playground/src/v_resource/dpi.sv")
}


