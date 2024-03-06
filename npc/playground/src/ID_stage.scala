import chisel3._
import chisel3.util._
import config.Configs._
import Control._

class ID_stage extends Module {
  val ID=IO(new Bundle {
    val IO = Flipped(Decoupled(new if_to_id_bus()))
    val to_ex =Decoupled(new id_to_ex_bus())

    val for_ex=Input(new ex_to_id_bus())
    val for_ls=Input(new ls_to_id_bus())
    val for_wb=Input(new wb_to_id_bus())
    val to_if=Output(new id_to_if_bus())
    val to_preif=Output(new id_to_preif_bus())
  })
  val id_clog=dontTouch(Wire(Bool()))
  val id_flush=dontTouch(Wire(Bool()))
  //id_clog需要结合decoder的地址计算是否发起阻塞，放在下边处理
  //应对lw指令的处理
  id_flush:=ID.for_ex.flush

  val id_valid=dontTouch(RegInit(false.B))
  val id_ready_go=dontTouch(Wire(Bool()))
  id_ready_go:=Mux(id_clog,false.B,true.B)
  ID.IO.ready := !id_valid || id_ready_go && ID.to_ex.ready
  when(ID.IO.ready){
    id_valid:=ID.IO.valid
  }
  ID.to_ex.valid:=Mux(id_flush, false.B , id_valid && id_ready_go)
  //flush是将下一级置为无效

  val dc=Module(new Decode())
  val ImmGen=Module(new ImmGen())
  val imm=dontTouch(Wire(UInt(32.W)))
  val rs1=dontTouch(Wire(UInt(5.W)))
  val rs2=dontTouch(Wire(UInt(5.W)))
  val funct3=dontTouch(Wire(UInt(3.W)))
  val rd=dontTouch(Wire(UInt(5.W)))
  val opcode=dontTouch(Wire(UInt(7.W)))
  val csr_addr=dontTouch(Wire(UInt(12.W)))
  
  dc.io.inst:=ID.IO.bits.inst

  ImmGen.io.inst:=ID.IO.bits.inst
  ImmGen.io.sel :=dc.io.imm_sel

  imm := ImmGen.io.out
  rs2 := ID.IO.bits.inst(24, 20)
  rs1 := ID.IO.bits.inst(19, 15)
  funct3 := ID.IO.bits.inst(14, 12)
  rd := ID.IO.bits.inst(11, 7)
  opcode := ID.IO.bits.inst(6, 0)
  csr_addr := ID.IO.bits.inst(31, 20)

  val Regfile=Module(new RegFile())
  Regfile.io.raddr1:=rs1
  Regfile.io.raddr2:=MuxLookup(dc.io.csr_cmd,rs2)(Seq(
    CSR.BREAK->10.U,
    CSR.ECALL->RISCV32E_ECALLREG
  ))
  //当ebreak时，算出reg(10)+0的结果并通知dpi-c，即reg(10)==return
  //当ecall时，算出reg(ECALL_REG)+0的结果并传递给WB的csr处理

  //前递处理
  val rdata1=dontTouch(Wire(UInt(DATA_WIDTH.W)))
  val rdata2=dontTouch(Wire(UInt(DATA_WIDTH.W)))
  val rs1_is_forward=dontTouch(Wire(Bool()))
  val rs2_is_forward=dontTouch(Wire(Bool()))
  id_clog:=(ID.for_ex.clog||ID.for_ls.clog)&&(rs1_is_forward||rs2_is_forward)

  rs1_is_forward:=((Regfile.io.raddr1=/=0.U) && 
                  ((Regfile.io.raddr1===ID.for_ex.fw.addr) ||
                  (Regfile.io.raddr1===ID.for_ls.fw.addr) || 
                  (ID.for_wb.rf.wen && (Regfile.io.raddr1===ID.for_wb.rf.waddr))))
  rs2_is_forward:=((Regfile.io.raddr2=/=0.U) &&
                  ((Regfile.io.raddr2===ID.for_ex.fw.addr) ||
                  (Regfile.io.raddr2===ID.for_ls.fw.addr) ||
                  (ID.for_wb.rf.wen && (Regfile.io.raddr2===ID.for_wb.rf.waddr))))
  rdata1:=Mux(rs1_is_forward,
            Mux(Regfile.io.raddr1===ID.for_ex.fw.addr,ID.for_ex.fw.data,
            Mux(Regfile.io.raddr1===ID.for_ls.fw.addr,ID.for_ls.fw.data,
                                                  ID.for_wb.rf.wdata)),
                                                  Regfile.io.rdata1)
  rdata2:=Mux(rs2_is_forward,
            Mux(Regfile.io.raddr2===ID.for_ex.fw.addr,ID.for_ex.fw.data,
            Mux(Regfile.io.raddr2===ID.for_ls.fw.addr,ID.for_ls.fw.data,
                                                  ID.for_wb.rf.wdata)),
                                                  Regfile.io.rdata2)


  //在id级实例化CSR，通过ex前递写回
  val csr_out_data=dontTouch(Wire(UInt(DATA_WIDTH.W)))
  val Csrfile=Module(new CsrFile())
  Csrfile.io.csr_cmd:=dc.io.csr_cmd
  csr_out_data:=Mux(csr_addr===ID.for_ex.csr.waddr,ID.for_ex.csr.wdata,Csrfile.io.out)
  //简化的csr实现，先用前递代替，实际上应该是清空
  Csrfile.io.csr_raddr:=csr_addr
  Csrfile.io.csr_wen:=ID.for_ex.csr.wen
  Csrfile.io.csr_waddr:=ID.for_ex.csr.waddr
  Csrfile.io.csr_wdata:=ID.for_ex.csr.wdata
  Csrfile.io.ecpt_wen :=ID.for_ex.csr.ecpt.wen
  Csrfile.io.mepc_in  :=ID.for_ex.csr.ecpt.pc_wb
  Csrfile.io.mcause_in:=ID.for_ex.csr.ecpt.mcause_in

  ID.to_ex.bits.csr_addr:=csr_addr
  ID.to_ex.bits.csr_cmd:=dc.io.csr_cmd
  ID.to_ex.bits.pc_sel:=dc.io.pc_sel
  ID.to_ex.bits.csr_global<>Csrfile.io.global


  val src1=MuxLookup(dc.io.A_sel,0.U)(Seq(
    A_RS1 -> rdata1,
    A_PC  -> ID.IO.bits.pc
  ))
  val src2=MuxLookup(dc.io.B_sel,0.U)(Seq(
    B_RS2 -> rdata2,
    B_IMM -> imm,
    B_CSR -> csr_out_data
  ))
  Regfile.io.waddr:=ID.for_wb.rf.waddr
  Regfile.io.wdata:=ID.for_wb.rf.wdata
  Regfile.io.wen  :=ID.for_wb.rf.wen

  //j分支跳转
  val J_cond=Module(new Br_j())
  val B_cond=Module(new Br_b())
  J_cond.io.br_type:=dc.io.br_type
  J_cond.io.src1:=src1
  J_cond.io.src2:=src2
  ID.to_preif.Br_J.taken:=J_cond.io.taken&&id_valid
  ID.to_preif.Br_J.target:=J_cond.io.target
  
  B_cond.io.br_type:=dc.io.br_type
  B_cond.io.rdata1:=rdata1
  B_cond.io.rdata2:=rdata2


  ID.to_if.flush:=J_cond.io.taken &&id_valid
  ID.to_preif.flush:=J_cond.io.taken &&id_valid
  //如果是j跳转，id级向if级和preif级发起flush     (损失两个周期)
  //如果是b跳转，ex级向if，preif，id级发起flush (损失三个周期，b跳转在ex级计算)
  //b跳转分为两个阶段，在id级计算是否跳转，在ex级得到跳转地址发起跳转
  ID.to_ex.bits.b_taken:=B_cond.io.taken&&id_valid

//------------------------------------------

  ID.to_ex.bits.st_type:=dc.io.st_type
  ID.to_ex.bits.ld_type:=dc.io.ld_type
  ID.to_ex.bits.csr_cmd:=dc.io.csr_cmd
  ID.to_ex.bits.wb_sel :=dc.io.wb_sel
  ID.to_ex.bits.br_type:=dc.io.br_type
  ID.to_ex.bits.rf_wen :=dc.io.wb_en&&id_valid
  ID.to_ex.bits.rd     :=rd
  ID.to_ex.bits.alu_op:=dc.io.alu_op
  ID.to_ex.bits.src1:=src1
  ID.to_ex.bits.src2:=src2
  ID.to_ex.bits.rdata1:=rdata1
  ID.to_ex.bits.rdata2:=rdata2
  ID.to_ex.bits.inst:=ID.IO.bits.inst
  ID.to_ex.bits.pc  :=ID.IO.bits.pc
  ID.to_ex.bits.nextpc:=ID.IO.bits.nextpc

  /*---------------------传递信号到wb级再由wb级处理dpi信号----------------------*/
  ID.to_ex.bits.dpic_bundle.id.inv_flag:=(dc.io.illegal&&ID.IO.bits.nextpc=/="h80000000".U)
  ID.to_ex.bits.dpic_bundle.ex:=0.U.asTypeOf(new for_ex_dpi_bundle)
  ID.to_ex.bits.dpic_bundle.ls:=0.U.asTypeOf(new for_ls_dpi_bundle)
}

