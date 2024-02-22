import chisel3._
import chisel3.util._
import config.Configs._

object CSR {
  val N = 0.U(5.W)
  val W = 1.U(5.W)
  val S = 2.U(5.W)
  val MRET  = 3.U(5.W)
  val ECALL = 4.U(5.W)
  val BREAK = 5.U(5.W)


  val MTVEC=0x305.U(12.W)
  val MSTATUS=0x300.U(12.W)
  val MEPC=0x341.U(12.W)
  val MCAUSE=0x342.U(12.W)
  val MTVAL=0x343.U(12.W)
}

class CsrFile extends Module{
  val io=IO(new Bundle{
    val csr_cmd=Input(UInt(5.W))
    val csr_raddr=Input(UInt(12.W))
    val csr_waddr=Input(UInt(12.W))
    val csr_wen=Input(Bool())
    val csr_wdata=Input(UInt(DATA_WIDTH.W))
    val mepc_in=Input(UInt(ADDR_WIDTH.W))
    val mcause_in=Input(UInt(ADDR_WIDTH.W))
    val ecpt_wen=Input(Bool())

    val out=Output(UInt(DATA_WIDTH.W))
    val global=Output(new csr_global())
  })
  // csr_addr，csr寄存器的地址，
  // in写入csr的值，out用于写入rd寄存器的值
  val mtvec  =RegInit(0.U(DATA_WIDTH.W))
  val mstatus=RegInit("h1800".U(DATA_WIDTH.W))
  val mepc   =RegInit(0.U(DATA_WIDTH.W))
  val mcause =RegInit(0.U(DATA_WIDTH.W))
  val mtval  =RegInit(0.U(DATA_WIDTH.W))
  
  val csr_assert_wen=WireDefault(false.B)
  io.global.mtvec:=WireDefault(0.U(ADDR_WIDTH.W))

  io.out:=MuxLookup(io.csr_raddr,0.U)(Seq(
    CSR.MTVEC->mtvec,
    CSR.MSTATUS->mstatus,
    CSR.MEPC->mepc,
    CSR.MCAUSE->mcause,
    CSR.MTVAL->mtval
  ))



  when(io.ecpt_wen){
    mepc:=io.mepc_in
    mcause:=io.mcause_in
    io.global.mtvec:=mtvec
  }
  when(io.csr_cmd===CSR.MRET){
    io.global.mtvec:=mepc
  }
  when(io.csr_wen){
    when(io.csr_waddr===CSR.MTVEC){ 
      mtvec:=io.csr_wdata }
    .elsewhen(io.csr_waddr===CSR.MSTATUS){ mstatus:=io.csr_wdata }
    .elsewhen(io.csr_waddr===CSR.MEPC)   { mepc:=io.csr_wdata    }
    .elsewhen(io.csr_waddr===CSR.MCAUSE) { mcause:=0xb.U  }
    .elsewhen(io.csr_waddr===CSR.MTVAL)  { mtval:=io.csr_wdata   }
    .otherwise{ csr_assert_wen:=true.B }
  }


  val csr_dpic=Module(new csr_debug_dpic())
  csr_dpic.io.clock:=clock
  csr_dpic.io.reset:=reset
  csr_dpic.io.csr_valid:=io.csr_cmd=/=CSR.N
  csr_dpic.io.assert_wen:=csr_assert_wen
}



class Csr_alu extends Module{
  val io=IO(new Bundle{
    val csr_cmd=Input(UInt(5.W))
    val in_csr=Input(UInt(DATA_WIDTH.W))
    val in_rdata1=Input(UInt(DATA_WIDTH.W))
    val wen=Output(Bool())
    val out=Output(UInt(DATA_WIDTH.W))
  })
  io.wen:= (io.csr_cmd===CSR.W
         || io.csr_cmd===CSR.S)
  io.out:=MuxLookup(io.csr_cmd,0.U)(Seq(
    CSR.W->io.in_rdata1,
    CSR.S->(io.in_csr|io.in_rdata1),
  ))
}


class csr_debug_dpic extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val csr_valid=Input(Bool())
    val assert_wen=Input(Bool())
  })
  setInline("csr_debug_dpic.v",
    """
      |import "DPI-C" function void Csr_assert();
      |module csr_debug_dpic(
      |    input        clock,
      |    input        reset,
      |    input        csr_valid,
      |    input        assert_wen
      |);
      | always @(posedge clock)begin
      |   if(~reset)begin
      |     if(csr_valid&&assert_wen)  Csr_assert();
      |   end
      |  end
      |endmodule
    """.stripMargin)
}  