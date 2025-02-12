package ErXCore

import chisel3._
import chisel3.util._
import chisel3.experimental.BundleLiterals._

object CSR_CODE {
  val MTVEC=0x305.U(12.W)
  val MSTATUS=0x300.U(12.W)
  val MEPC=0x341.U(12.W)
  val MCAUSE=0x342.U(12.W)
  val MTVAL=0x343.U(12.W)
  val MVENDORID=0xF11.U(12.W)
  val MARCHID=0xF12.U(12.W)
}

class CsrFile extends ErXCoreModule{
  val io=IO(new ErXCoreBundle{
    val read  = new ReadCsrIO
    val write = new WriteCsrIO
    val robIO = new CsrRobIO
  })
  // csr_addr，csr寄存器的地址
  // in写入csr的值，out用于写入rd寄存器的值
  val mstatus=RegInit((new CsrStatusBundle()).Lit(
    _.sd        ->0.U,
    _.wpri30_23 ->0.U,
    _.todo22_13 ->0.U,
    _.mpp       ->3.U,
    _.vs        ->0.U,
    _.spp       ->0.U,
    _.mpie      ->0.U,
    _.ube       ->0.U,
    _.spie      ->0.U,
    _.wpri_4    ->0.U,
    _.mie       ->0.U,
    _.wpri_2    ->0.U,
    _.sie       ->0.U,
    _.wpri_0    ->0.U,
  ))
  val mtvec  =RegInit(0.U.asTypeOf(new CsrXtvecBundle()))
  val mepc   =RegInit(0.U(XLEN.W))
  val mcause =RegInit(0.U.asTypeOf(new CsrCauseBundle()))
  val mvendorid=RegInit("h79737978".U(XLEN.W))
  val marchid=RegInit("h23060115".U(XLEN.W))

  io.read.data:=Mux1hMap(io.read.addr,Map(
    CSR_CODE.MSTATUS->mstatus.asUInt,
    CSR_CODE.MEPC   ->mepc,
    CSR_CODE.MCAUSE ->mcause.asUInt,
    CSR_CODE.MARCHID->marchid,
    CSR_CODE.MVENDORID->mvendorid,
  ))

  io.robIO.entry.mtvec  :=mtvec.asUInt
  io.robIO.entry.mepc   :=mepc

//mstatus
  val mstatusWrData=io.write.data.asTypeOf(new CsrStatusBundle())
  when(io.robIO.update.mretFlush){
    // mstatus.mie:=mstatus.mpie
    // mstatus.mpie:=1.U //方便期间，暂时与nemu同步
  }.elsewhen(io.write.wen&&io.write.addr===CSR_CODE.MSTATUS){
    mstatus:=mstatusWrData
  }
//mepc
  when(io.robIO.update.excpFlush){
    mepc:=io.robIO.update.excpResult.vaBadAddr
  }.elsewhen(io.write.wen&&io.write.addr===CSR_CODE.MEPC){
    mepc:=io.write.data
  }
//mcause
  val mcauseWrData=io.write.data.asTypeOf(new CsrCauseBundle())
  when(io.robIO.update.excpFlush){
    mcause.ecode:=io.robIO.update.excpResult.ecode
  }.elsewhen(io.write.wen&&io.write.addr===CSR_CODE.MCAUSE){
    mcause.interpt:=mcauseWrData.interpt
    mcause.ecode  :=mcauseWrData.ecode
  }
//mtvec
  val mtvecWrData=io.write.data.asTypeOf(new CsrXtvecBundle())
  when(io.write.wen&&io.write.addr===CSR_CODE.MTVEC){
    mtvec.base:=mtvecWrData.base
    mtvec.mode:=mtvecWrData.mode
  }
//
  import ErXCore.Difftest._
  val diffCSR = Wire(new DiffCsrRegBundle())
  diffCSR.mcause :=mcause.asUInt
  diffCSR.mepc   :=mepc.asUInt
  diffCSR.mstatus:=mstatus.asUInt
  diffCSR.mtvec  :=mtvec.asUInt
  ExcitingUtils.addSource(diffCSR,"DiffCSR",ExcitingUtils.Func)
}

class WriteCsrIO extends ErXCoreBundle{
  val wen = Input(Bool())
  val addr = Input(UInt(12.W))
  val data = Input(UInt(XLEN.W))
}

class ReadCsrIO extends ErXCoreBundle{
  val addr=Input(UInt(12.W))
  val data=Output(UInt(XLEN.W))
}

class CsrRobIO extends ErXCoreBundle{
  val update = Input(new CsrFlushUpdate())
  val entry  = Output(new CsrEntriesBundle())
}
class CsrFlushUpdate extends ErXCoreBundle{
  val excpFlush = Bool()
  val mretFlush = Bool()
  val excpResult = new ExcpResultBundle()
}

class CsrEntriesBundle extends ErXCoreBundle{
  val mepc=UInt(XLEN.W)
  val mtvec=UInt(XLEN.W)
}

class CsrCauseBundle extends ErXCoreBundle{
  val interpt=UInt(1.W)
  val ecode  =UInt((XLEN-1).W)
}

class CsrStatusBundle extends ErXCoreBundle{
  //32位
  val sd=UInt(1.W)
  val wpri30_23=UInt(8.W)
  val todo22_13=UInt(10.W)
  val mpp   =UInt(2.W)
  val vs    =UInt(2.W)
  val spp   =UInt(1.W)
  val mpie  =UInt(1.W)
  val ube   =UInt(1.W)
  val spie  =UInt(1.W)
  val wpri_4=UInt(1.W)
  val mie   =UInt(1.W)
  val wpri_2=UInt(1.W)
  val sie=UInt(1.W)
  val wpri_0=UInt(1.W)
}

class CsrXtvecBundle extends ErXCoreBundle{
  val base=UInt((XLEN-2).W)
  val mode=UInt(2.W)
}


object ECODE{ //简化写法，按照手册英文首字母简写
  val IAM =  0.U(6.W) //Instruction address misaligned
  val IAF =  1.U(6.W) //Instruction access fault
  val INE =  2.U(6.W) //Illegal instruction
  val BKP =  3.U(6.W) //Breakpoint
  val LAM =  4.U(6.W) //Load address misaligned
  val LAF =  5.U(6.W) //Load access fault
  val SAM =  6.U(6.W) //Store/AMO address misaligned
  val SAF =  7.U(6.W) //Store/AMO  access fault
  val ECU =  8.U(6.W) //Environment call from U-mode
  val ECS =  9.U(6.W) //Environment call from S-mode
  val ECM = 11.U(6.W) //Environment call from M-mode
  val IPF = 12.U(6.W) //Instruction page fault
  val LPF = 13.U(6.W) //Load page fault
  val SPF = 15.U(6.W) //Store/AMO page fault
}

class ExcpTypeBundle extends ErXCoreBundle{
  val spf = Bool()
  val lpf = Bool()
  val ipf = Bool()
  val ecm = Bool()
  val ecs = Bool()
  val ecu = Bool()
  val saf = Bool()
  val sam = Bool()
  val laf = Bool()
  val lam = Bool()
  val bkp = Bool()
  val ine = Bool()
  val iaf = Bool()
  val iam = Bool()
}//从下到上从0开始逐渐递增

class ExcpResultBundle extends ErXCoreBundle{
  val ecode = UInt(6.W)
  val vaError = Bool()
  val vaBadAddr = UInt(XLEN.W)
} 