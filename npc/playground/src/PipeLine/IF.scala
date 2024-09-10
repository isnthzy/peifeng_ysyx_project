package PipeLine
import chisel3._
import chisel3.util._
import Axi._
import Bundles._
import CoreConfig.Configs._

class IfStage extends Module {
  val fs=IO(new Bundle {
    val in=Flipped(Decoupled(new Pf2IfBusBundle()))
    val to_id=Decoupled(new If2IdBusBundle())

    val from_id=Input(new If4IdBusBundle())
    val from_ex=Input(new If4ExBusBundle())
    val from_ls=Input(new If4LsBusBundle())

    val dl=new AxiBridgeDataLoad()
  })
  val fsFlush=dontTouch(Wire(Bool()))
  val fsExcpEn=dontTouch(Wire(Bool()))
  fsFlush:=fs.from_id.flush||fs.from_ex.flush||fs.from_ls.flush
  val fsValid=dontTouch(Wire(Bool()))
  val fsValidR=RegInit(false.B)
  val fsReadyGo=dontTouch(Wire(Bool()))
  fs.in.ready:=fs.to_id.ready&& ~fsValidR || fsReadyGo
  when(fsFlush){
    fsValidR:=false.B
  }.elsewhen(fs.in.ready){
    fsValidR:=fs.in.valid
  }
  fsValid:=fsValidR&& ~fsFlush
  fsReadyGo:=fs.dl.rdata_ok || fsExcpEn
  fs.to_id.valid:= fsValid&&fsReadyGo //fsValid===fsValidR&& ~fsFlush

  val fsInstBuff=RegInit(0.U(DATA_WIDTH.W))
  val fsUseInstBuff=RegInit(false.B)
  val fsInst=Mux(fsExcpEn,INST_NOP,
        Mux(fsUseInstBuff,fsInstBuff,fs.dl.rdata))
  when(fs.to_id.fire){
    fsUseInstBuff:=false.B
  }.elsewhen(fs.dl.rdata_ok){
    fsInstBuff:=fs.dl.rdata
    fsUseInstBuff:=true.B
  }

//NOTE:Excp
  val fsExcpType=Wire(new IfExcpTypeBundle())
  fsExcpType.num:=fs.in.bits.excpType
  fsExcpType.iaf:=false.B
  fsExcpType.ipf:=false.B
  fsExcpEn:=fsExcpType.asUInt.orR
  fs.to_id.bits.excpEn:=fsExcpEn
  fs.to_id.bits.excpType:=fsExcpType

  fs.to_id.bits.pc:=fs.in.bits.pc
  fs.to_id.bits.inst:=fsInst
}