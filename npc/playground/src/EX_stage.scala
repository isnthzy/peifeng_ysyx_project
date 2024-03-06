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
    val to_preif =Output(new ex_to_preif_bus())

    val ar=Decoupled(new AxiAddressBundle())
    val aw=Decoupled(new AxiAddressBundle())
    val w=Decoupled(new AxiWriteDataBundle())
    val b=Flipped(Decoupled(new AxiWriteResponseBundle()))
  })
  dontTouch(EX.ar);
  dontTouch(EX.aw);
  dontTouch(EX.w);
  dontTouch(EX.b);
  val ld_wen=dontTouch(Wire(Bool()))
  val st_wen=dontTouch(Wire(Bool()))
  st_wen:=(EX.IO.bits.st_type=/=0.U)
  ld_wen:=(EX.IO.bits.ld_type=/=0.U)

  val ex_valid=dontTouch(RegInit(false.B))
  val ex_ready_go=dontTouch(Wire(Bool()))
  ex_ready_go:=Mux((~EX.ar.ready&&ld_wen)
                || (~(EX.aw.ready&&EX.w.ready)&&st_wen),false.B,true.B)
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
  EX.to_preif.Br_B.taken:=EX.IO.bits.b_taken&&ex_valid
  EX.to_preif.Br_B.target:=MuxLookup(EX.IO.bits.br_type,0.U)(Seq(
                          BR_XXX -> 0.U,
                          BR_LTU -> Alu.io.result,
                          BR_LT  -> Alu.io.result,
                          BR_EQ  -> Alu.io.result,
                          BR_GEU -> Alu.io.result,
                          BR_GE  -> Alu.io.result,
                          BR_NE  -> Alu.io.result,
  ))


  //如果是跳转，发起flush
  //如果是b跳转，ex级向if，preif，id级发起flush (损失三个周期，b跳转在ex级计算)
  val to_flush=Wire(Bool())
  to_flush:=(EX.to_preif.Br_B.taken
         || (EX.IO.bits.csr_cmd===CSR.MRET )
         || (EX.IO.bits.csr_cmd===CSR.ECALL))&&ex_valid 
  // EX.to_if.flush:=(EX.to_if.Br_B.taken
  //               || (EX.IO.bits.csr_cmd===CSR.MRET )
  //               || (EX.IO.bits.csr_cmd===CSR.ECALL))&&ex_valid 
  EX.to_id.flush:=to_flush
  EX.to_if.flush:=to_flush
  EX.to_preif.flush:=to_flush

  //对id发起阻塞
  EX.to_id.clog:=(EX.IO.bits.ld_type=/=0.U)&&ex_valid


  //前递
  EX.to_id.fw.addr:=Mux(ex_valid && EX.IO.bits.rf_wen , EX.to_ls.bits.rd , 0.U)
  EX.to_id.fw.data:=EX.to_ls.bits.result

  //EX级发起访存
//---------------------------AXI4 Lite---------------------------
  val WaitWriteIdle=dontTouch(Wire(Bool()))
  //如果write不是idle状态，拉高信号
  val BrespFire=dontTouch(Wire(Bool()))
  //b通道握手，拉高信号
//----------------------AXI4Lite AR Channel----------------------
  val ar_idle :: ar_wait_ready :: Nil = Enum(2)
  val arvalidReg=RegInit(false.B)
  val araddrReg=RegInit(0.U(ADDR_WIDTH.W))

  
  val ReadRequstState=RegInit(ar_idle)
  when(ReadRequstState===ar_idle){
    when(ld_wen&&ex_valid){
      when(WaitWriteIdle){
        when(BrespFire){
          ReadRequstState:=ar_wait_ready
          araddrReg:=Alu.io.result
          arvalidReg:=true.B    
        }
      }.otherwise{
        ReadRequstState:=ar_wait_ready
        araddrReg:=Alu.io.result
        arvalidReg:=true.B
      }
    }
  }.elsewhen(ReadRequstState===ar_wait_ready){
    when(EX.ar.ready){
      ReadRequstState:=ar_idle
      arvalidReg:=false.B
    }
  }

  EX.ar.valid:=arvalidReg
  EX.ar.bits.addr:=araddrReg
  EX.ar.bits.prot:=0.U

//----------------------AXI4Lite AR Channel----------------------

//-------------------AXI4Lite  W WR B  Channel-------------------
  val wr_idle :: wr_wait_ready :: wr_wait_bresp :: Nil = Enum(3)
  val WriteRequstState=RegInit(wr_idle)
  val awvalidReg=RegInit(false.B)
  val awaddrReg=RegInit(0.U(ADDR_WIDTH.W))
  val wvalidReg=RegInit(false.B)
  val wdataReg=RegInit(0.U(DATA_WIDTH.W))
  val wstrbReg=RegInit(0.U(4.W))
  val breadyReg=RegInit(false.B)

  when(WriteRequstState===wr_idle){
    when(st_wen&&ex_valid){
      WriteRequstState:=wr_wait_ready
      awvalidReg:=true.B
      awaddrReg:=Alu.io.result
      
      wvalidReg:=true.B
      wdataReg:=EX.IO.bits.rdata2
      wstrbReg:=EX.IO.bits.st_type
    }
  }.elsewhen(WriteRequstState===wr_wait_ready){
    when(EX.aw.ready&&EX.w.ready){
      WriteRequstState:=wr_wait_bresp
      awvalidReg:=false.B
      wvalidReg:=false.B
      breadyReg:=true.B
    }
  }.elsewhen(WriteRequstState===wr_wait_bresp){
    when(breadyReg&&EX.b.valid){
      WriteRequstState:=wr_idle
      breadyReg:=false.B
    }
  }
  WaitWriteIdle:=(WriteRequstState=/=wr_idle)
  BrespFire:=EX.b.fire
  EX.aw.bits.prot:=0.U
  EX.aw.valid:=awvalidReg
  EX.aw.bits.addr:=awaddrReg
  EX.w.valid:=wvalidReg
  EX.w.bits.data:=wdataReg
  EX.w.bits.strb:=wstrbReg
  EX.b.ready:=breadyReg
//---------------------------AXI4 Lite---------------------------

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
  
  EX.to_preif.epc.taken:=(EX.IO.bits.pc_sel===PC_EPC)&&ex_valid
  EX.to_preif.epc.target:=Mux((EX.IO.bits.csr_cmd===CSR.MRET ),EX.IO.bits.csr_global.mepc,
                        Mux((EX.IO.bits.csr_cmd===CSR.ECALL),EX.IO.bits.csr_global.mtvec,0.U))
  


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

  EX.to_ls.bits.dpic_bundle.id<>EX.IO.bits.dpic_bundle.id
  EX.to_ls.bits.dpic_bundle.ex.func_flag:=(EX.IO.bits.br_type===BR_JAL)|(EX.IO.bits.br_type===BR_JR)
  EX.to_ls.bits.dpic_bundle.ex.is_jal:=EX.IO.bits.br_type===BR_JAL
  EX.to_ls.bits.dpic_bundle.ex.is_ret:=EX.IO.bits.inst===0x00008067.U
  EX.to_ls.bits.dpic_bundle.ex.is_rd0:=EX.IO.bits.rd===0.U

  EX.to_ls.bits.dpic_bundle.ex.ld_type:=EX.IO.bits.ld_type
  EX.to_ls.bits.dpic_bundle.ex.st_type:=EX.IO.bits.st_type
  EX.to_ls.bits.dpic_bundle.ex.mem_addr:=Mux(st_wen||ld_wen,Alu.io.result,0.U)
  EX.to_ls.bits.dpic_bundle.ex.st_data:=Mux(st_wen,EX.IO.bits.rdata2,0.U)

  EX.to_ls.bits.dpic_bundle.ls:=0.U.asTypeOf(new for_ls_dpi_bundle)
}

