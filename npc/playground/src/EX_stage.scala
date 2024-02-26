import chisel3._
import chisel3.util._
import config.Configs._
import Control._
class EX_stage extends Module {
  val EX=IO(new Bundle {
    val IO    =Flipped(Decoupled(new id_to_ex_bus()))
    val to_ls =Decoupled(new ex_to_ls_bus())

    val to_id =Output(new ex_to_id_bus())
    val to_if =Output(new ex_to_if_bus())

    val ar=Flipped(Decoupled(new AxiAddressBundle()))
    val aw=Flipped(Decoupled(new AxiAddressBundle()))
    val w=Flipped(Decoupled(new AxiWriteDataBundle()))
    val b=Decoupled(new AxiWriteResponseBundle())
  })
  
  val ex_valid=dontTouch(RegInit(false.B))
  val ex_ready_go=dontTouch(Wire(Bool()))
  ex_ready_go:=Mux(EX.ar.ready||(EX.aw.ready&&EX.w.ready),true.B,false.B)
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
  
  EX.to_if.Br_B.taken:=EX.IO.bits.b_taken&&ex_valid
  EX.to_if.Br_B.target:=MuxLookup(EX.IO.bits.br_type,0.U)(Seq(
                          BR_XXX -> 0.U,
                          BR_LTU -> Alu.io.result,
                          BR_LT  -> Alu.io.result,
                          BR_EQ  -> Alu.io.result,
                          BR_GEU -> Alu.io.result,
                          BR_GE  -> Alu.io.result,
                          BR_NE  -> Alu.io.result,
  ))


  //如果是跳转，发起flush
  EX.to_if.flush:=(EX.to_if.Br_B.taken
                || (EX.IO.bits.csr_cmd===CSR.MRET )
                || (EX.IO.bits.csr_cmd===CSR.ECALL))&&ex_valid 
  
  //对id发起阻塞
  EX.to_id.clog:=(EX.IO.bits.ld_type=/=0.U)&&ex_valid


  //前递
  EX.to_id.fw.addr:=Mux(ex_valid && EX.IO.bits.rf_wen , EX.to_ls.bits.rd , 0.U)
  EX.to_id.fw.data:=EX.to_ls.bits.result

  //EX级发起访存-----------AXI分界线-------------------
  

  val ld_wen=dontTouch(Wire(Bool()))
  val st_wen=dontTouch(Wire(Bool()))
  ld_wen:=(EX.IO.bits.st_type=/=0.U)&&ex_valid
  st_wen:=(EX.IO.bits.ld_type=/=0.U)&&ex_valid

  val DoAddrReadReg=RegInit(false.B)
  val DoAddrWriteReg=RegInit(false.B)
  val DoWdataReg=RegInit(false.B)
  when(ld_wen && ex_valid){
    DoAddrReadReg:=true.B
  }

  when(EX.ar.fire && ex_valid){
    DoAddrReadReg:=false.B
  }

  when(st_wen && ex_valid){
    DoAddrWriteReg:=true.B
    DoWdataReg:=true.B
  }

  when(EX.aw.fire && ex_valid){
    DoAddrWriteReg:=false.B
  }

  when(EX.w.fire && ex_valid){
    DoWdataReg:=false.B
  }

  EX.ar.bits.addr:=Alu.io.result
  // EX.ar.valid:=DoAddrReadReg&&ex_valid
  EX.ar.bits.prot:=0.U

  EX.w.bits.data:=EX.IO.bits.rdata2
  EX.w.bits.strb:=EX.IO.bits.st_type

  // EX.data_sram.st_wen:=ld_wen
  // EX.data_sram.ld_wen:=st_wen
  // EX.data_sram.addr:=Alu.io.result
  // EX.data_sram.wmask:=EX.IO.bits.st_type
  // EX.data_sram.wdata:=EX.IO.bits.rdata2

  //----------------------------------------------------

  //csr
  val Csr_alu=Module(new Csr_alu())
  Csr_alu.io.csr_cmd:=EX.IO.bits.csr_cmd
  Csr_alu.io.in_csr:=Alu.io.result
  Csr_alu.io.in_rdata1:=EX.IO.bits.rdata1
  EX.to_id.csr.waddr:=EX.IO.bits.csr_addr
  EX.to_id.csr.wen:=Csr_alu.io.wen&&ex_valid
  EX.to_id.csr.wdata:=Csr_alu.io.out
  EX.to_id.csr.ecpt.wen:=(EX.IO.bits.csr_cmd===CSR.ECALL)&&ex_valid
  EX.to_id.csr.ecpt.mcause_in:=11.U
  EX.to_id.csr.ecpt.pc_wb:=EX.IO.bits.pc
  
  EX.to_if.epc.taken:=(EX.IO.bits.pc_sel===PC_EPC)&&ex_valid
  EX.to_if.epc.target:=Mux((EX.IO.bits.csr_cmd===CSR.MRET ),EX.IO.bits.csr_global.mepc,
                        Mux((EX.IO.bits.csr_cmd===CSR.ECALL),EX.IO.bits.csr_global.mtvec,0.U))
  


  EX.to_ls.bits.st_wen:=st_wen
  EX.to_ls.bits.ld_wen:=ld_wen
  EX.to_ls.bits.ld_type:=EX.IO.bits.ld_type
  EX.to_ls.bits.csr_cmd:=EX.IO.bits.csr_cmd
  EX.to_ls.bits.wb_sel:=EX.IO.bits.wb_sel
  EX.to_ls.bits.rf_wen:=EX.IO.bits.rf_wen
  EX.to_ls.bits.rd:=EX.IO.bits.rd
  EX.to_ls.bits.result:=Alu.io.result
  EX.to_ls.bits.pc  :=EX.IO.bits.pc
  EX.to_ls.bits.inst:=EX.IO.bits.inst
  EX.to_ls.bits.nextpc:=EX.IO.bits.nextpc

  /*---------------------传递信号到wb级再由wb级处理dpi信号----------------------*/
  EX.to_ls.bits.csr_commit.waddr:=EX.to_id.csr.waddr
  EX.to_ls.bits.csr_commit.wen  :=EX.to_id.csr.wen
  EX.to_ls.bits.csr_commit.wdata:=EX.to_id.csr.wdata
  EX.to_ls.bits.csr_commit.exception:=EX.to_id.csr.ecpt


  EX.to_ls.bits.dpic_bundle.id_inv_flag:=EX.IO.bits.dpic_bundle.id_inv_flag
  EX.to_ls.bits.dpic_bundle.ex_func_flag:=(EX.IO.bits.br_type===BR_JAL)|(EX.IO.bits.br_type===BR_JR)
  EX.to_ls.bits.dpic_bundle.ex_is_jal:=EX.IO.bits.br_type===BR_JAL
  EX.to_ls.bits.dpic_bundle.ex_is_ret:=EX.IO.bits.inst===0x00008067.U
  EX.to_ls.bits.dpic_bundle.ex_is_rd0:=EX.IO.bits.rd===0.U

}

