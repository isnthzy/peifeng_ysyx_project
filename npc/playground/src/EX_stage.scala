import chisel3._
import chisel3.util._
import config.Configs._
import Control._
class EX_stage extends Module {
  val EX=IO(new Bundle {
    // val IO    =Input(new id_to_ex_bus())
    val IO    =Flipped(Decoupled(new id_to_ex_bus()))
    val to_ls =Decoupled(new ex_to_ls_bus())
    val br_bus=Output(new br_bus())
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
  val rs1_eq_rs2   = EX.IO.bits.rdata1 === EX.IO.bits.rdata2
  val rs1_lt_rs2_s = EX.IO.bits.rdata1.asSInt < EX.IO.bits.rdata2.asSInt
  val rs1_lt_rs2_u = EX.IO.bits.rdata1  <  EX.IO.bits.rdata2

  EX.br_bus.is_jump := ((EX.IO.bits.br_type===BR_JAL)
                      | (EX.IO.bits.br_type===BR_JR)
                      | ((EX.IO.bits.br_type===BR_EQ) && rs1_eq_rs2)
                      | ((EX.IO.bits.br_type===BR_NE) && !rs1_eq_rs2)
                      | ((EX.IO.bits.br_type===BR_LT) && rs1_lt_rs2_s)
                      | ((EX.IO.bits.br_type===BR_LTU)&& rs1_lt_rs2_u)
                      | ((EX.IO.bits.br_type===BR_GE) && !rs1_lt_rs2_s)
                      | ((EX.IO.bits.br_type===BR_GEU)&& !rs1_lt_rs2_u))
  EX.br_bus.dnpc:=MuxLookup(EX.IO.bits.br_type,0.U)(Seq(
    BR_XXX -> 0.U,
    BR_LTU -> Alu.io.result,
    BR_LT  -> Alu.io.result,
    BR_EQ  -> Alu.io.result,
    BR_GEU -> Alu.io.result,
    BR_GE  -> Alu.io.result,
    BR_NE  -> Alu.io.result,
    BR_JAL -> Alu.io.result,
    BR_JR  -> Cat(Alu.io.result(31,1),0.U(1.W))
  ))
  
  EX.to_ls.bits.pc_sel:=EX.IO.bits.pc_sel
  EX.to_ls.bits.csr_addr:=EX.IO.bits.csr_addr
  EX.to_ls.bits.csr_cmd:=EX.IO.bits.csr_cmd
  EX.to_ls.bits.rs1_addr:=EX.IO.bits.rs1_addr
  //csr
  EX.to_ls.bits.st_type:=EX.IO.bits.st_type
  EX.to_ls.bits.ld_type:=EX.IO.bits.ld_type
  EX.to_ls.bits.ebreak_flag:=EX.IO.bits.ebreak_flag
  EX.to_ls.bits.wb_sel:=EX.IO.bits.wb_sel
  EX.to_ls.bits.wen :=EX.IO.bits.wen
  EX.to_ls.bits.rd:=EX.IO.bits.rd
  EX.to_ls.bits.rdata2:=EX.IO.bits.rdata2
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

