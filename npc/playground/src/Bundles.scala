import chisel3._
import chisel3.util._  
import config.Configs._


class commit_csr_to_diff extends Bundle{
  //从ex级开始流到wb级通过dpic修改仿真环境的csr寄存器，进而进行diff
  //当前的设计是csr寄存器在ex级完成修改
  val wen=Bool()
  val waddr=UInt(12.W)
  val wdata=UInt(DATA_WIDTH.W)
  val exception=new exception_bus()
}

class csr_global extends Bundle{
  val mtvec=UInt(DATA_WIDTH.W)
  val mepc=UInt(DATA_WIDTH.W)
}

class exception_bus extends Bundle{
  val wen=Bool()
  val mcause_in=UInt(4.W)
  val pc_wb=UInt(ADDR_WIDTH.W)
}

class data_sram_ex_bus extends Bundle{
  val st_wen=Bool()
  val ld_wen=Bool()
  val addr=UInt(ADDR_WIDTH.W)
  val wmask=UInt(8.W)
  val wdata=UInt(DATA_WIDTH.W)
}

class data_sram_ls_bus extends Bundle{
  val rdata=UInt(DATA_WIDTH.W)
  val rdata_ok=Bool()
  val wdata_ok=Bool()
}


class forward_to_id_bus extends Bundle{
  val addr=UInt(5.W)
  val data=UInt(DATA_WIDTH.W)
  //传递到id级防止写后读（前递）
}

class ex_to_csr_bus extends Bundle{
  val ecpt=new exception_bus()
  val waddr=UInt(12.W)
  val wen=Bool()
  val wdata=UInt(DATA_WIDTH.W)
}

class wb_to_rf_bus extends Bundle{
  val waddr=UInt(5.W)
  val wdata=UInt(DATA_WIDTH.W)
  val wen=Bool()
}

class epc_to_if_bus extends Bundle{
  val taken=Bool()
  val target=UInt(ADDR_WIDTH.W)
}

class br_bus extends Bundle{
  val stall=Bool()
  val taken=Bool()
  val target=UInt(ADDR_WIDTH.W)
}

//-----------------dpi bundle-----------------
class for_id_dpi_bundle extends Bundle{
  val inv_flag=Bool()
}

class for_ex_dpi_bundle extends Bundle{
  val func_flag=Bool()
  val is_jal=Bool()
  val is_ret=Bool()
  val is_rd0=Bool()

  val ld_type=UInt(3.W)
  val st_type=UInt(3.W)
  val mem_addr=UInt(ADDR_WIDTH.W)
  val st_data=UInt(DATA_WIDTH.W)
}

class for_ls_dpi_bundle extends Bundle{
  val ld_data=UInt(DATA_WIDTH.W)
}

class To_wb_dpic_bus extends Bundle{
  val id=new for_id_dpi_bundle()
  val ex=new for_ex_dpi_bundle()
  val ls=new for_ls_dpi_bundle()
}
//-----------------dpi bundle-----------------

//--------------to preif---------------
class id_to_preif_bus extends Bundle{
  val Br_J=new br_bus()
  val flush=Bool()
}
class ex_to_preif_bus extends Bundle{
  val epc=new epc_to_if_bus()
  val Br_B=new br_bus()
  val flush=Bool()
}
//--------------to preif---------------

//----------------to if----------------
class id_to_if_bus extends Bundle{
  val flush=Bool()
}
class ex_to_if_bus extends Bundle{
  val flush=Bool()
}

//----------------to if----------------


//----------------to id----------------
class ex_to_id_bus extends Bundle{
  val fw=new forward_to_id_bus()
  val csr=new ex_to_csr_bus() //csrfile
  val clog=Bool()
  val flush=Bool()
}
class ls_to_id_bus extends Bundle{
  val fw=new forward_to_id_bus()
  val clog=Bool()
}
class wb_to_id_bus extends Bundle{
  val rf=new wb_to_rf_bus()
}
//----------------to id----------------

//----------------to ex----------------
class ls_to_ex_bus extends Bundle{
  val WaitloadOk=Bool()
}
//----------------to ex----------------
class preif_to_if_bus extends Bundle{
  val nextpc=UInt(ADDR_WIDTH.W)
  val pc  =UInt(ADDR_WIDTH.W)
}

class if_to_id_bus extends Bundle{
  val nextpc=UInt(ADDR_WIDTH.W)
  val pc  =UInt(ADDR_WIDTH.W)
  val inst=UInt(32.W)
}


class id_to_ex_bus extends Bundle{
  val dpic_bundle=new To_wb_dpic_bus()
  //传递到wb级进行交给dpic处理

  val pc_sel=Bool()
  val csr_addr=UInt(12.W)
  val csr_global=new csr_global()

  val b_taken=Bool()
  val st_type=UInt(3.W)
  val ld_type=UInt(3.W)
  val csr_cmd=UInt(5.W)
  val wb_sel =UInt(2.W)
  val br_type=UInt(4.W)
  val rf_wen =Bool()
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
  val csr_commit=new commit_csr_to_diff()
  val dpic_bundle=new To_wb_dpic_bus()
   //传递到wb级进行交给dpic处理

  val addr_low2bit=UInt(2.W)
  val ld_wen=Bool() //不需要st_type原因是st_type在ex级被处理
  val ld_type=UInt(3.W)
  val csr_cmd=UInt(5.W)
  val wb_sel =UInt(2.W)
  val rf_wen =Bool()
  val rd    =UInt(5.W)
  val result=UInt(DATA_WIDTH.W)
  val nextpc=UInt(ADDR_WIDTH.W)
  val pc  =UInt(ADDR_WIDTH.W)
  val inst=UInt(32.W)
}

class ls_to_wb_bus extends Bundle{
  val csr_commit=new commit_csr_to_diff()
  val dpic_bundle=new To_wb_dpic_bus()
   //传递到wb级进行交给dpic处理

  val csr_cmd=UInt(5.W)
  val rf_wen =Bool()
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