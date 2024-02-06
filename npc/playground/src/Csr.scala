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
    val pc=Input(UInt(ADDR_WIDTH.W))
    val csr_addr=Input(UInt(12.W))
    val rs1_addr=Input(UInt(5.W))
    val in=Input(UInt(DATA_WIDTH.W))
    val out_wen=Output(Bool())
    val out=Output(UInt(DATA_WIDTH.W))
    val epc=Output(UInt(ADDR_WIDTH.W))
  })
  // csr_addr，csr寄存器的地址，
  // in写入csr的值，out用于写入rd寄存器的值
  io.out_wen:=(io.csr_cmd===CSR.W)||(io.csr_cmd===CSR.S)
  val csr_wen=(io.csr_cmd===CSR.W)||(io.csr_cmd===CSR.S)
  val mtvec  =RegInit(0.U(DATA_WIDTH.W))
  val mstatus=RegInit("h1800".U(DATA_WIDTH.W))
  val mepc   =RegInit(0.U(DATA_WIDTH.W))
  val mcause =RegInit(0.U(DATA_WIDTH.W))
  val mtval  =RegInit(0.U(DATA_WIDTH.W))
  val wdata  =dontTouch(Wire(UInt(DATA_WIDTH.W)))
  
  val csr_assert_wen=WireDefault(false.B)
  io.epc:=WireDefault(0.U(ADDR_WIDTH.W))

  io.out:=MuxLookup(io.csr_addr,0.U)(Seq(
    CSR.MTVEC->mtvec,
    CSR.MSTATUS->mstatus,
    CSR.MEPC->mepc,
    CSR.MCAUSE->mcause,
    CSR.MTVAL->mtval
  ))

  wdata:=MuxLookup(io.csr_cmd,0.U)(Seq(
    CSR.W->io.in,
    CSR.S->(io.out|io.in)
  ))


  when(io.csr_cmd=/=CSR.N){
    when(io.csr_cmd===CSR.ECALL){
      mepc:=io.pc
      mcause:=0xb.U(32.W)
      io.epc:=mtvec
    }
    when(io.csr_cmd===CSR.MRET){
      io.epc:=mepc
    }
    when(csr_wen){
      when(io.csr_addr===CSR.MTVEC){ 
        mtvec:=wdata }
      .elsewhen(io.csr_addr===CSR.MSTATUS){ mstatus:=wdata }
      .elsewhen(io.csr_addr===CSR.MEPC)   { mepc:=wdata    }
      .elsewhen(io.csr_addr===CSR.MCAUSE) { mcause:=wdata  }
      .elsewhen(io.csr_addr===CSR.MTVAL)  { mtval:=wdata   }
      .otherwise{ csr_assert_wen:=true.B }
    }
  }

  val csr_dpic=Module(new csr_debug_dpic())
  csr_dpic.io.clock:=clock
  csr_dpic.io.reset:=reset
  csr_dpic.io.csr_valid:=io.csr_cmd=/=CSR.N
  csr_dpic.io.assert_wen:=csr_assert_wen
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