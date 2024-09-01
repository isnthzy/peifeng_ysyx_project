package FuncUnit

import chisel3._
import chisel3.util._
import config.Configs._
import Bundles._
import Util.Mux1hMap

object CSR {
  val MTVEC=0x305.U(12.W)
  val MSTATUS=0x300.U(12.W)
  val MEPC=0x341.U(12.W)
  val MCAUSE=0x342.U(12.W)
  val MTVAL=0x343.U(12.W)
}

class CsrFile extends Module{
  val io=IO(new Bundle{
    val to_csr=Input(new Ls2CsrBundle())
    val from_csr=Flipped(new PipeLine4CsrBundle())
    val csrEntries=Output(new CsrEntriesBundle())
    // val diff_csr=Output()
  })
  // csr_addr，csr寄存器的地址
  // in写入csr的值，out用于写入rd寄存器的值
  // val mtvec  =RegInit(0.U(DATA_WIDTH.W))
  val mstatus=RegInit("h1800".U(DATA_WIDTH.W))
  val mepc   =RegInit(0.U(DATA_WIDTH.W))
  val mcause =RegInit(0.U(DATA_WIDTH.W))
  // val mtval  =RegInit(0.U(DATA_WIDTH.W))
  
  // val csr_assert_wen=WireDefault(false.B)
  // io.global.mtvec:=WireDefault(0.U(ADDR_WIDTH.W))
  // io.global.mepc:=WireDefault(0.U(ADDR_WIDTH.W))

  io.from_csr.rdData:=Mux1hMap(io.from_csr.rdAddr,Map(
    CSR.MSTATUS->mstatus,
    CSR.MEPC->mepc,
    CSR.MCAUSE->mcause,
  ))

//

  // when(io.csr_cmd===CSR.ECALL){
  //   io.global.mtvec:=mtvec
  // }

  // when(io.ecpt_wen){
  //   mepc:=io.mepc_in
  //   mcause:=io.mcause_in
  // }

  // when(io.csr_cmd===CSR.MRET){
  //   io.global.mepc:=mepc
  // }

  // when(io.csr_wen){
  //   when(io.csr_waddr===CSR.MTVEC){ 
  //     mtvec:=io.csr_wdata }
  //   .elsewhen(io.csr_waddr===CSR.MSTATUS){ mstatus:=io.csr_wdata }
  //   .elsewhen(io.csr_waddr===CSR.MEPC)   { mepc:=io.csr_wdata    }
  //   .elsewhen(io.csr_waddr===CSR.MCAUSE) { mcause:=0xb.U  }
  //   .elsewhen(io.csr_waddr===CSR.MTVAL)  { mtval:=io.csr_wdata   }
  //   .otherwise{ csr_assert_wen:=true.B }
  // }


}


