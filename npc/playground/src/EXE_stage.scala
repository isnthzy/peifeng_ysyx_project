import chisel3._
import chisel3.util._  
class EXE_stage extends Module{
  val io=IO(new Bundle {
    val d_ebus=Input(UInt(ID_TO_ES_BUS_WD.W))
    val result=Output(UInt(32.W))
  })
  val RegFile=Module(new RegFile())
  RegFile.io.raddr1:=Mux(IsaI.ebreak,10.U,io.d_ebus.src1)
  RegFile.io.raddr2:=Mux(IsaI.ebreak, 0.U,io.d_ebus.src2)
  RegFile.io.waddr:=d_ebus.rd
  RegFile.io.wen:=d_ebus.data_wen
  val rf_rdata1=RegFile.io.rdata1
  val rf_rdata2=RegFile.io.rdata2

  val alu =Module(new Alu())
  val alu_src1=Mux(io.d_ebus.src1_is_pc,REGpc,rf_rdata1)
  val alu_src2=Mux(io.d_ebus.src2_is_imm,io.d_ebus.Imm,
                Mux(io.d_ebus.src2_is_shamt_imm,io.d_ebus.Imm(5,0), //立即数(5,0)的位移量
                  Mux(io.d_ebus.src2_is_shamt_src,rf_rdata2(5,0),rf_rdata2))) //src2(5,0)的位移量
  
  alu.io.op  :=io.d_ebus.alu_op.asUInt
  alu.io.src1:=alu_src1
  alu.io.src2:=alu_src2
  alu.io.sign:=io.d_ebus.src_is_sign 

  io.result:=Mux(io.d_ebus.result_is_imm , io.d_ebus.Imm,
              Mux(io.d_ebus.result_is_snpc , io.d_ebus.snpc, alu.io.result)) //要往rd中写入snpc
  val jalr_tmp=alu.io.result+io.d_ebus.Imm
  jalr_taget:=Cat(jalr_tmp(31,1),0.U(1.W))
  RegFile.io.wdata:=io.result

  pmem_dpi.io.clock:=clock
  pmem_dpi.io.reset:=reset
  pmem_dpi.io.sram_valid:=sram_valid
  pmem_dpi.io.sram_wen:=sram_wen
  pmem_dpi.io.raddr:=alu.io.result
  sram_rdata:=pmem_dpi.io.rdata
  pmem_dpi.io.waddr:=alu.io.result
  pmem_dpi.io.wdata:=src2
  pmem_dpi.io.wmask:=wmask
  
  // val sram_rdata_resul
  val singal_dpi=Module(new singal_dpi())
  singal_dpi.io.clock:=clock
  singal_dpi.io.reset:=reset
  singal_dpi.io.pc:=REGpc
  singal_dpi.io.nextpc:=nextpc
  singal_dpi.io.inst:=io_inst
  singal_dpi.io.rd:=Inst.rd
  singal_dpi.io.is_jal:=IsaU.jal
  singal_dpi.io.func_flag  :=IsaU.jal | IsaI.jalr
  singal_dpi.io.ebreak_flag:=IsaI.ebreak
  singal_dpi.io.inv_flag   :=Inst_inv
  singal_dpi.io.ret_reg    :=alu.io.result
}
class pmem_dpi extends BlackBox with HasBlackBoxPath{
  val io=IO(new Bundle {
    val clock=Input(Clock())
    val reset=Input(Bool())
    val sram_valid=Input(Bool())
    val sram_wen=Input(Bool())
    val raddr=Input(UInt(32.W))
    val rdata=Output(UInt(32.W))
    val waddr=Input(UInt(32.W))
    val wdata=Input(UInt(32.W))
    val wmask=Input(UInt(5.W))
  })
  addPath("playground/src/v_resource/pmem.sv")
}


