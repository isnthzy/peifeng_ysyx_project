import chisel3._
import chisel3.util._
import config.Configs._
import Control._
class EX_stage extends Module {
  val EX=IO(new Bundle {
    val IO    =Input(new id_to_ex_bus())
    val to_ls =Output(new ex_to_ls_bus())
    val br_bus=Output(new br_bus())
  })
  
  val Alu=Module(new Alu())
  Alu.io.op:=EX.IO.alu_op
  Alu.io.src1:=EX.IO.src1
  Alu.io.src2:=EX.IO.src2
  
  //分支跳转
  val rs1_eq_rs2   = EX.IO.src1 === EX.IO.src2
  val rs1_lt_rs2_s = EX.IO.src1.asSInt < EX.IO.src2.asSInt
  val rs1_lt_rs2_u = EX.IO.src1  <  EX.IO.src2

  EX.br_bus.is_jump := ((EX.IO.br_type===BR_JAL)
                      | (EX.IO.br_type===BR_JR)
                      | (EX.IO.br_type===BR_EQ) && rs1_eq_rs2
                      | (EX.IO.br_type===BR_NE) && !rs1_eq_rs2
                      | (EX.IO.br_type===BR_LT) && rs1_lt_rs2_s
                      | (EX.IO.br_type===BR_LTU)&& rs1_lt_rs2_u
                      | (EX.IO.br_type===BR_GE) && !rs1_lt_rs2_s
                      | (EX.IO.br_type===BR_GEU)&& !rs1_lt_rs2_u)
  EX.br_bus.dnpc:=MuxLookup(EX.IO.br_type,0.U)(Seq(
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

  EX.to_ls.wb_sel:=EX.IO.wb_sel
  EX.to_ls.wen :=EX.IO.wen
  EX.to_ls.waddr:=EX.IO.waddr
  EX.to_ls.result:=Alu.io.result
  EX.to_ls.pc  :=EX.IO.pc
  EX.to_ls.inst:=EX.IO.inst
}
