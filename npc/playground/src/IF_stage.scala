import chisel3._
import chisel3.util._  
import config.Configs._

class IF_stage extends Module{
  val io=IO(new Bundle {
    val jalr_taget=Input(UInt(32.W))
    val is_not_jalr=Input(Bool())
    val is_jump=Input(Bool())
    val Imm=Input(UInt(32.W))
    val pc=Output(UInt(ADDR_WIDTH.W))
    val nextpc=Output(UInt(ADDR_WIDTH.W))
    val inst=Output(UInt(32.W))
    val f_dbus=Output(new if_to_id_bus())
  })
  val REGpc=RegInit(START_ADDR)
  val fetch_inst=Module(new fetch_inst())
  val snpc=dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  val dnpc=dontTouch(Wire(UInt(ADDR_WIDTH.W)))
  fetch_inst.io.clock:=clock
  fetch_inst.io.reset:=reset
  fetch_inst.io.pc:=REGpc
  fetch_inst.io.nextpc:=io.nextpc
  io.inst:=fetch_inst.io.inst
            
  snpc:=REGpc+4.U
  dnpc:=Mux(io.is_not_jalr,REGpc+io.Imm,io.jalr_taget) //不是jalr就是jal和IsaB
  REGpc:=Mux(io.is_jump,dnpc,snpc)
  
  io.f_dbus.snpc:=snpc
  io.nextpc:=dnpc
  io.pc:=REGpc
}


class fetch_inst extends BlackBox with HasBlackBoxPath{
  val io=IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val pc=Input(UInt(32.W))
    val nextpc=Input(UInt(32.W))
    val inst=Output(UInt(32.W))
  })
  addPath("playground/src/v_resource/fetch_inst.sv")
}
