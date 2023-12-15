import chisel3._
import chisel3.util._
import config.Configs._

class SimTop extends Module {
  val io = IO(new Bundle {
    val result = Output(UInt(32.W))
  })

//定义变量
  val Imm         = dontTouch(Wire(UInt(32.W)))
  val pc          = dontTouch(Wire(UInt(32.W)))
  val nextpc      = dontTouch(Wire(UInt(32.W)))
  val is_not_jalr = dontTouch(Wire(Bool()))
  val is_jump     = dontTouch(Wire(Bool()))
  val jalr_taget  = dontTouch(Wire(UInt(32.W)))
  val sram_rdata  = dontTouch(Wire(UInt(32.W)))
  val inst        = dontTouch(Wire(UInt(32.W)))
// IFU begin
  val IF_stage = Module(new IF_stage())
  pc                      := IF_stage.io.pc
  nextpc                  := IF_stage.io.nextpc      
  IF_stage.io.Imm         := Imm
  IF_stage.io.jalr_taget  := jalr_taget
  IF_stage.io.is_not_jalr := is_not_jalr
  IF_stage.io.is_jump     := is_jump
// IDU begin
  val ID_stage = Module(new ID_stage())
  ID_stage.io.inst   := inst
  ID_stage.io.pc     := pc
  ID_stage.io.nextpc := nextpc
  ID_stage.io.result := io.result
  Imm                := ID_stage.io.Imm
  is_jump            := ID_stage.io.is_jump
  is_not_jalr        := ID_stage.io.is_not_jalr

  ID_stage.io.f_dbus := IF_stage.io.f_dbus
// EXU begin
  val EXE_stage = Module(new EXE_stage())
  EXE_stage.io.pc := pc
  jalr_taget      := EXE_stage.io.jalr_taget
  io.result       := EXE_stage.io.result

  EXE_stage.io.d_ebus := ID_stage.io.d_ebus

// WB begin

  val pmem_dpi=Module(new pmem_dpi())
  pmem_dpi.io.clock:=clock
  pmem_dpi.io.reset:=reset
  pmem_dpi.io.pc:=pc
  pmem_dpi.io.nextpc:=nextpc
  inst:=pmem_dpi.io.inst
  pmem_dpi.io.sram_valid:=EXE_stage.io.sram_valid
  pmem_dpi.io.sram_wen:=EXE_stage.io.sram_wen
  pmem_dpi.io.raddr:=EXE_stage.io.result
  sram_rdata:=pmem_dpi.io.rdata
  pmem_dpi.io.waddr:=EXE_stage.io.result
  pmem_dpi.io.wdata:=EXE_stage.io.sram_wdata
  pmem_dpi.io.wmask:=EXE_stage.io.sram_wmask
}
class pmem_dpi extends BlackBox with HasBlackBoxPath{
  val io=IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val pc=Input(UInt(ADDR_WIDTH.W))
    val nextpc=Input(UInt(ADDR_WIDTH.W))
    val inst=Output(UInt(32.W))
    val sram_valid=Input(Bool())
    val sram_wen=Input(Bool())
    val raddr=Input(UInt(32.W))
    val rdata=Output(UInt(32.W))
    val waddr=Input(UInt(32.W))
    val wdata=Input(UInt(32.W))
    val wmask=Input(UInt(4.W))
  })
  addPath("playground/src/v_resource/pmem.sv")
}
