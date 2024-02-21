import chisel3._
import chisel3.util._  
import config.Configs._

class data_sram_bus_ex extends Bundle{
  val st_wen=Bool()
  val ld_wen=Bool()
  val addr=UInt(ADDR_WIDTH.W)
  val wmask=UInt(8.W)
  val wdata=UInt(DATA_WIDTH.W)
}

class data_sram_bus_ls extends Bundle{
  val rdata=UInt(DATA_WIDTH.W)
  val rdata_ok=Bool()
  val wdata_ok=Bool()
}


class forward_to_id_bus extends Bundle{
  val addr=UInt(5.W)
  val data=UInt(DATA_WIDTH.W)
  //传递到id级防止写后读（前递）
}


class wb_to_id_bus extends Bundle{
  val waddr=UInt(5.W)
  val wdata=UInt(DATA_WIDTH.W)
  val wen=Bool()
}

class wb_to_if_bus extends Bundle{
  val csr_epc=UInt(ADDR_WIDTH.W)
  val epc_wen=Bool()
}

class br_bus extends Bundle{
  val taken=Bool()
  val target=UInt(ADDR_WIDTH.W)
}

class if_to_id_bus extends Bundle{
  val nextpc=UInt(ADDR_WIDTH.W)
  val pc  =UInt(ADDR_WIDTH.W)
  val inst=UInt(32.W)
}

class To_wb_dpic_bus extends Bundle{
  val id_inv_flag=Bool()

  val ex_func_flag=Bool()
  val ex_is_jal=Bool()
  val ex_is_ret=Bool()
  val ex_is_rd0=Bool()
}

class id_to_ex_bus extends Bundle{
  val dpic_bundle=new To_wb_dpic_bus()
  //传递到wb级进行交给dpic处理

  val pc_sel=Bool()
  val csr_addr=UInt(12.W)
  val csr_cmd=UInt(5.W)
  val rs1_addr=UInt(5.W)
  //csr
  val b_taken=Bool()
  val st_type=UInt(8.W)
  val ld_type=UInt(3.W)
  val ebreak_flag=Bool()
  val wb_sel =UInt(2.W)
  val br_type=UInt(4.W)
  val wen   =Bool()
  val rd    =UInt(5.W)
  val alu_op=UInt(4.W)
  val src1=UInt(DATA_WIDTH.W)
  val src2=UInt(DATA_WIDTH.W)
  val rdata1=UInt(DATA_WIDTH.W)
  val rdata2=UInt(DATA_WIDTH.W)
  val nextpc=UInt(ADDR_WIDTH.W)
  val pc  =UInt(ADDR_WIDTH.W)
  val inst=UInt(32.W)
}

class ex_to_ls_bus extends Bundle{
  val dpic_bundle=new To_wb_dpic_bus()
   //传递到wb级进行交给dpic处理

  val pc_sel=Bool()
  val csr_addr=UInt(12.W)
  val csr_cmd=UInt(5.W)
  val rs1_addr=UInt(5.W)
  //csr
  val st_wen=Bool()
  val ld_wen=Bool() //不需要st_type原因是st_type在ex级被处理
  val ld_type=UInt(3.W)
  val ebreak_flag=Bool()
  val wb_sel =UInt(2.W)
  val wen   =Bool()
  val rd    =UInt(5.W)
  val result=UInt(DATA_WIDTH.W)
  val nextpc=UInt(ADDR_WIDTH.W)
  val pc  =UInt(ADDR_WIDTH.W)
  val inst=UInt(32.W)
}

class ls_to_wb_bus extends Bundle{
  val dpic_bundle=new To_wb_dpic_bus()
   //传递到wb级进行交给dpic处理

  val pc_sel=Bool()
  val csr_addr=UInt(12.W)
  val csr_cmd=UInt(5.W)
  val rs1_addr=UInt(5.W)
  //csr
  val ebreak_flag=Bool()
  val wen   =Bool()
  val rd    =UInt(5.W)
  val result=UInt(DATA_WIDTH.W)
  val nextpc=UInt(ADDR_WIDTH.W)
  val pc  =UInt(ADDR_WIDTH.W)
  val inst=UInt(32.W)
}



object Sext{ //有符号位宽扩展
  def apply(num:UInt,e_width:Int) = {
    val num_width=num.getWidth
    if(num_width<e_width){
      Cat(Fill(e_width-num_width,num(num_width-1)),num(num_width-1,0))
    }else{
      num(num_width-1,0)
    }
  }
}

object Zext{ //无符号位宽扩展
  def apply(num:UInt,e_width:Int) = {
    val num_width=num.getWidth
    if(num_width<e_width){
      Cat(Fill(e_width-num_width,0.U),num(num_width-1,0))
    }else{
      num(num_width-1,0)
    }
  }
}