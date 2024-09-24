package FuncUnit

import chisel3._
import chisel3.util._
import CoreConfig.Configs._
import Bundles._
import Bundles.Difftest._
import Util.Mux1hMap
import chisel3.experimental.BundleLiterals._

object CSR {
  val MTVEC=0x305.U(12.W)
  val MSTATUS=0x300.U(12.W)
  val MEPC=0x341.U(12.W)
  val MCAUSE=0x342.U(12.W)
  val MTVAL=0x343.U(12.W)
  val MVENDORID=0xF11.U(12.W)
  val MARCHID=0xF12.U(12.W)
}

class CsrFile extends Module{
  val io=IO(new Bundle{
    val to_csr=Input(new Ls2CsrBundle())
    val from_csr=new PipeLine4CsrBundle()
    val csrEntries=Output(new CsrEntriesBundle())
    val diffCSR=Output(new DiffCsrRegBundle())
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
  val mepc   =RegInit(0.U(DATA_WIDTH.W))
  val mcause =RegInit(0.U.asTypeOf(new CsrCauseBundle()))
  val mvendorid=RegInit("h79737978".U(DATA_WIDTH.W))
  val marchid=RegInit("h15FDE93".U(DATA_WIDTH.W))

  io.from_csr.rdData:=Mux1hMap(io.from_csr.rdAddr,Map(
    CSR.MSTATUS->mstatus.asUInt,
    CSR.MEPC   ->mepc,
    CSR.MCAUSE ->mcause.asUInt,
    CSR.MARCHID->marchid,
    CSR.MVENDORID->mvendorid,
  ))

  io.csrEntries.mtvec  :=mtvec.asUInt
  io.csrEntries.mepc   :=mepc

//mstatus
  val mstatusWrData=io.to_csr.wrData.asTypeOf(new CsrStatusBundle())
  when(io.to_csr.mretFlush){
    // mstatus.mie:=mstatus.mpie
    // mstatus.mpie:=1.U //方便期间，暂时与nemu同步
  }.elsewhen(io.to_csr.wen&&io.to_csr.wrAddr===CSR.MSTATUS){
    mstatus:=mstatusWrData
  }
//mepc
  when(io.to_csr.excpFlush){
    mepc:=io.to_csr.excpResult.vaBadAddr
  }.elsewhen(io.to_csr.wen&&io.to_csr.wrAddr===CSR.MEPC){
    mepc:=io.to_csr.wrData
  }
//mcause
  val mcauseWrData=io.to_csr.wrData.asTypeOf(new CsrCauseBundle())
  when(io.to_csr.excpFlush){
    mcause.ecode:=io.to_csr.excpResult.ecode
  }.elsewhen(io.to_csr.wen&&io.to_csr.wrAddr===CSR.MCAUSE){
    mcause.interpt:=mcauseWrData.interpt
    mcause.ecode  :=mcauseWrData.ecode
  }
//mtvec
  val mtvecWrData=io.to_csr.wrData.asTypeOf(new CsrXtvecBundle())
  when(io.to_csr.wen&&io.to_csr.wrAddr===CSR.MTVEC){
    mtvec.base:=mtvecWrData.base
    mtvec.mode:=mtvecWrData.mode
  }
//
  io.diffCSR.mcause :=mcause.asUInt
  io.diffCSR.mepc   :=mepc.asUInt
  io.diffCSR.mstatus:=mstatus.asUInt
  io.diffCSR.mtvec  :=mtvec.asUInt
}


