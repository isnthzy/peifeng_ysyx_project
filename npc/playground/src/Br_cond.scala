import chisel3._
import chisel3.util._
import config.Configs._
import Control._

class Br_cond extends Module {
  val io=IO(new Bundle {
    val br_type=Input(UInt(4.W))
    val result=Input(UInt(32.W))
    val rdata1=Input(UInt(DATA_WIDTH.W))
    val rdata2=Input(UInt(DATA_WIDTH.W))
    val taken=Output(Bool())
    val target=Output(UInt(ADDR_WIDTH.W))
  })
  val rs1_eq_rs2   = io.rdata1 === io.rdata2
  val rs1_lt_rs2_s = io.rdata1.asSInt < io.rdata2.asSInt
  val rs1_lt_rs2_u = io.rdata1  <  io.rdata2

  io.taken :=(((io.br_type===BR_EQ) && rs1_eq_rs2)
            | ((io.br_type===BR_NE) && !rs1_eq_rs2)
            | ((io.br_type===BR_LT) && rs1_lt_rs2_s)
            | ((io.br_type===BR_LTU)&& rs1_lt_rs2_u)
            | ((io.br_type===BR_GE) && !rs1_lt_rs2_s)
            | ((io.br_type===BR_GEU)&& !rs1_lt_rs2_u))
  //重构后分支要放在jal，jr放在id级，b指令放在ex级


  io.target:=MuxLookup(io.br_type,0.U)(Seq(
    BR_XXX -> 0.U,
    BR_LTU -> io.result,
    BR_LT  -> io.result,
    BR_EQ  -> io.result,
    BR_GEU -> io.result,
    BR_GE  -> io.result,
    BR_NE  -> io.result,
  ))
  
}


class Br_j extends Module {
  val io=IO(new Bundle {
    val br_type=Input(UInt(4.W))
    val rdata1=Input(UInt(DATA_WIDTH.W))
    val rdata2=Input(UInt(DATA_WIDTH.W))
    val taken=Output(Bool())
    val target=Output(UInt(ADDR_WIDTH.W))
  })

  io.taken := ((io.br_type===BR_JAL)
            | (io.br_type===BR_JR))

  val result=io.rdata1+io.rdata2

  io.target:=MuxLookup(io.br_type,0.U)(Seq(
    BR_XXX -> 0.U,
    BR_JAL -> result,
    BR_JR  -> Cat(result(31,1),0.U(1.W))
  ))
}

class Br_option extends Module{
  val io=IO(new Bundle {
    val Jtype=Input(new br_bus())
    val Btype=Input(new br_bus())
    val out=Output(new br_bus())
  })
  io.out.taken:=io.Jtype.taken||io.Btype.taken
  io.out.target:=Mux(io.Jtype.taken,io.Jtype.target,io.Btype.target)
}