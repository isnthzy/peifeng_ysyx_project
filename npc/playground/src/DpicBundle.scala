import chisel3._
import chisel3.util._  
import config.Configs._

class To_wb_dpic_bus extends Bundle{
  val id_inv_flag=Wire(Bool())

  val ex_func_flag=Wire(Bool())
  val ex_is_jal=Wire(Bool())
  val ex_is_ret=Wire(Bool())
  val ex_is_rd0=Wire(Bool())
}