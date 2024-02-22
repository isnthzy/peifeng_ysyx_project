import chisel3._
import chisel3.util._
import config.Configs._
import Control._
class EX_stage extends Module {
  val EX=IO(new Bundle {
    // val IO    =Input(new id_to_ex_bus())
    val IO    =Flipped(Decoupled(new id_to_ex_bus()))
    val to_ls =Decoupled(new ex_to_ls_bus())
    val bypass_id=Output(new forward_to_id_bus())
    val clog_id  =Output(Bool())
    val flush_out=Output(Bool())
    val br_bus=Output(new br_bus())
    val to_if=Output(new ex_to_if_bus())
    val to_csr=Output(new ex_to_csr_bus())
    val data_sram=Output(new data_sram_bus_ex())
  })
  
  val ex_valid=dontTouch(RegInit(false.B))
  val ex_ready_go=dontTouch(Wire(Bool()))
  ex_ready_go:=true.B
  EX.IO.ready := !ex_valid || ex_ready_go && EX.to_ls.ready
  when(EX.IO.ready){
    ex_valid:=EX.IO.valid
  }
  EX.to_ls.valid:=ex_valid && ex_ready_go


  val Alu=Module(new Alu())
  Alu.io.op:=EX.IO.bits.alu_op
  Alu.io.src1:=EX.IO.bits.src1
  Alu.io.src2:=EX.IO.bits.src2
  
  //分支跳转
  
  EX.br_bus.taken:=EX.IO.bits.b_taken&&ex_valid
  EX.br_bus.target:=MuxLookup(EX.IO.bits.br_type,0.U)(Seq(
                      BR_XXX -> 0.U,
                      BR_LTU -> Alu.io.result,
                      BR_LT  -> Alu.io.result,
                      BR_EQ  -> Alu.io.result,
                      BR_GEU -> Alu.io.result,
                      BR_GE  -> Alu.io.result,
                      BR_NE  -> Alu.io.result,
                    ))


  //如果是跳转，发起flush
  EX.flush_out:=(EX.br_bus.taken
              || EX.IO.bits.ecpt_ecall
              || EX.IO.bits.is_mret)&&ex_valid 
  
  //对id发起阻塞
  EX.clog_id:=(EX.IO.bits.ld_type=/=0.U)&&ex_valid


  //前递
  EX.bypass_id.addr:=Mux(ex_valid && EX.to_ls.bits.wen , EX.to_ls.bits.rd , 0.U)
  EX.bypass_id.data:=EX.to_ls.bits.result

  //EX级发起访存
  val ld_wen=dontTouch(Wire(Bool()))
  val st_wen=dontTouch(Wire(Bool()))
  ld_wen:=(EX.IO.bits.st_type=/=0.U)&&ex_valid
  st_wen:=(EX.IO.bits.ld_type=/=0.U)&&ex_valid

  EX.data_sram.st_wen:=ld_wen
  EX.data_sram.ld_wen:=st_wen
  EX.data_sram.addr:=Alu.io.result
  EX.data_sram.wmask:=EX.IO.bits.st_type
  EX.data_sram.wdata:=EX.IO.bits.rdata2


  //csr
  val Csr_alu=Module(new Csr_alu())
  Csr_alu.io.csr_cmd:=EX.IO.bits.csr_cmd
  Csr_alu.io.in_csr:=Alu.io.result
  Csr_alu.io.in_rdata1:=EX.IO.bits.rdata1
  EX.to_csr.csr_waddr:=EX.IO.bits.csr_addr
  EX.to_csr.csr_wen:=Csr_alu.io.wen&&ex_valid
  EX.to_csr.csr_wdata:=Csr_alu.io.out
  EX.to_csr.ecpt.ecpt_wen:=EX.IO.bits.ecpt_ecall
  EX.to_csr.ecpt.exception_no:=11.U
  EX.to_csr.ecpt.mepc:=EX.IO.bits.pc
  
  EX.to_if.csr_epc:=Mux(EX.IO.bits.is_mret,EX.IO.bits.csr_global.mepc,
                      Mux(EX.IO.bits.ecpt_ecall,EX.IO.bits.csr_global.mtvec,0.U))
  EX.to_if.epc_wen:=(EX.IO.bits.pc_sel===PC_EPC)
  // EX.to_ls.bits.pc_sel:=EX.IO.bits.pc_sel
  // EX.to_ls.bits.csr_addr:=EX.IO.bits.csr_addr
  // EX.to_ls.bits.csr_cmd:=EX.IO.bits.csr_cmd
  // EX.to_ls.bits.rs1_addr:=EX.IO.bits.rs1_addr
  


  EX.to_ls.bits.st_wen:=st_wen
  EX.to_ls.bits.ld_wen:=ld_wen
  EX.to_ls.bits.ld_type:=EX.IO.bits.ld_type
  EX.to_ls.bits.ebreak_flag:=EX.IO.bits.ebreak_flag
  EX.to_ls.bits.wb_sel:=EX.IO.bits.wb_sel
  EX.to_ls.bits.wen :=EX.IO.bits.wen
  EX.to_ls.bits.rd:=EX.IO.bits.rd
  EX.to_ls.bits.result:=Alu.io.result
  EX.to_ls.bits.pc  :=EX.IO.bits.pc
  EX.to_ls.bits.inst:=EX.IO.bits.inst
  EX.to_ls.bits.nextpc:=EX.IO.bits.nextpc

  /*---------------------传递信号到wb级再由wb级处理dpi信号----------------------*/
  EX.to_ls.bits.dpic_bundle.id_inv_flag:=EX.IO.bits.dpic_bundle.id_inv_flag
  EX.to_ls.bits.dpic_bundle.ex_func_flag:=(EX.IO.bits.br_type===BR_JAL)|(EX.IO.bits.br_type===BR_JR)
  EX.to_ls.bits.dpic_bundle.ex_is_jal:=EX.IO.bits.br_type===BR_JAL
  EX.to_ls.bits.dpic_bundle.ex_is_ret:=EX.IO.bits.inst===0x00008067.U
  EX.to_ls.bits.dpic_bundle.ex_is_rd0:=EX.IO.bits.rd===0.U

}

