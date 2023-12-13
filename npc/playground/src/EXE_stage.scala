import chisel3._
import chisel3.util._  
class EXE_stage extends Module{
  val io=IO(new Bundle {
    val d_ebus=Input(new id_to_es_bus())
    val pc=Input(UInt(32.W))
    val result=Output(UInt(32.W))
    val jalr_taget=Output(UInt(32.W))
    val sram_valid=Output(Bool())
    val sram_wen=Output(Bool())
    val sram_wdata=Output(UInt(32.W))
    val sram_wmask=Output(UInt(5.W))
  })
  io.result:=0.U
  io.jalr_taget:=0.U
  val RegFile=Module(new RegFile())
  val sram_rdata=Wire(UInt(32.W))

  RegFile.io.raddr1:=Mux(io.d_ebus.is_ebreak,10.U,io.d_ebus.src1)
  RegFile.io.raddr2:=Mux(io.d_ebus.is_ebreak, 0.U,io.d_ebus.src2)
  RegFile.io.waddr:=io.d_ebus.rd
  RegFile.io.wen:=io.d_ebus.data_wen
  val rf_rdata1=RegFile.io.rdata1
  val rf_rdata2=RegFile.io.rdata2

  val alu =Module(new Alu())
  val alu_src1=Mux(io.d_ebus.src1_is_pc,io.pc,rf_rdata1)
  val alu_src2=Mux(io.d_ebus.src2_is_imm,io.d_ebus.imm,
                Mux(io.d_ebus.src2_is_shamt_imm,io.d_ebus.imm(5,0), //立即数(5,0)的位移量
                  Mux(io.d_ebus.src2_is_shamt_src,rf_rdata2(5,0),rf_rdata2))) //src2(5,0)的位移量
  
  alu.io.op  :=io.d_ebus.alu_op.asUInt
  alu.io.src1:=alu_src1
  alu.io.src2:=alu_src2
  alu.io.sign:=io.d_ebus.src_is_sign 

  io.result:=Mux(io.d_ebus.result_is_imm , io.d_ebus.imm,
              Mux(io.d_ebus.result_is_snpc , io.d_ebus.snpc, alu.io.result)) //要往rd中写入snpc
  val jalr_tmp=alu.io.result+io.d_ebus.imm
  io.jalr_taget:=Cat(jalr_tmp(31,1),0.U(1.W))
  RegFile.io.wdata:=io.result
  io.sram_wdata:=io.d_ebus.src2
  io.sram_wmask:=io.d_ebus.wmask
}



