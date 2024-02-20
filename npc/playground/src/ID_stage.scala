import chisel3._
import chisel3.util._
import config.Configs._
import Control._

class ID_stage extends Module {
  val ID=IO(new Bundle {
    // val IO    =Input(new if_to_id_bus())
    val IO = Flipped(Decoupled(new if_to_id_bus()))
    val to_ex =Decoupled(new id_to_ex_bus())
    val ex_fw=Input(new forward_to_id_bus())
    val ls_fw=Input(new forward_to_id_bus())
    val wb_bus=Input(new wb_to_id_bus())
    val flush=Input(Bool())
    val flush_out=Output(Bool())
    val j_cond=Output(new br_bus())
    val for_ex_clog=Input(Bool())
    val for_ls_clog=Input(Bool())
  })
  val id_clog=dontTouch(Wire(Bool()))
  id_clog:=ID.for_ex_clog || ID.for_ls_clog

  val id_valid=dontTouch(RegInit(false.B))
  val id_ready_go=dontTouch(Wire(Bool()))
  id_ready_go:=Mux(id_clog,false.B,true.B)
  ID.IO.ready := !id_valid || id_ready_go && ID.to_ex.ready
  when(ID.IO.ready){
    id_valid:=ID.IO.valid
  }
  ID.to_ex.valid:=Mux(ID.flush, false.B ,id_valid && id_ready_go)
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
  rs1_is_forward:=((Regfile.io.raddr1=/=0.U) && 
                  (id_clog===false.B) &&  //从lw传来的数据
                  ((Regfile.io.raddr1===ID.ex_fw.addr) ||
                  (Regfile.io.raddr1===ID.ls_fw.addr) || 
                  (ID.wb_bus.wen && (Regfile.io.raddr1===ID.wb_bus.waddr))))
  rs2_is_forward:=((Regfile.io.raddr2=/=0.U) &&
                  (id_clog===false.B) &&
                  ((Regfile.io.raddr2===ID.ex_fw.addr) ||
                  (Regfile.io.raddr2===ID.ls_fw.addr) ||
                  (ID.wb_bus.wen && (Regfile.io.raddr2===ID.wb_bus.waddr))))
  rdata1:=Mux(rs1_is_forward,
            Mux(Regfile.io.raddr1===ID.ex_fw.addr,ID.ex_fw.data,
            Mux(Regfile.io.raddr1===ID.ls_fw.addr,ID.ls_fw.data,
                                                  ID.wb_bus.wdata)),
                                                  Regfile.io.rdata1)
  rdata2:=Mux(rs2_is_forward,
            Mux(Regfile.io.raddr2===ID.ex_fw.addr,ID.ex_fw.data,
            Mux(Regfile.io.raddr2===ID.ls_fw.addr,ID.ls_fw.data,
                                                  ID.wb_bus.wdata)),
                                                  Regfile.io.rdata2)


  val src1=MuxLookup(dc.io.A_sel,0.U)(Seq(
    A_RS1 -> rdata1,
    A_PC  -> ID.IO.bits.pc
  ))
  val src2=MuxLookup(dc.io.B_sel,0.U)(Seq(
    B_RS2 -> rdata2,
    B_IMM -> imm
  ))
  Regfile.io.waddr:=ID.wb_bus.waddr
  Regfile.io.wdata:=ID.wb_bus.wdata
  Regfile.io.wen  :=ID.wb_bus.wen

  //j分支跳转
  val J_cond=Module(new Br_j())
  J_cond.io.br_type:=dc.io.br_type
  J_cond.io.rdata1:=src1
  J_cond.io.rdata2:=src2
  ID.j_cond.taken:=J_cond.io.taken&&id_valid&& ~ID.flush
  ID.j_cond.target:=J_cond.io.target
  ID.flush_out:=J_cond.io.taken&&id_valid && ~ID.flush
  

  ID.to_ex.bits.pc_sel:=dc.io.pc_sel
  ID.to_ex.bits.csr_addr:=csr_addr
  ID.to_ex.bits.csr_cmd:=dc.io.csr_cmd
  ID.to_ex.bits.rs1_addr:=rs1


  //csr
  ID.to_ex.bits.st_type:=dc.io.st_type
  ID.to_ex.bits.ld_type:=dc.io.ld_type
  ID.to_ex.bits.ebreak_flag:=(dc.io.csr_cmd===CSR.BREAK)
  ID.to_ex.bits.wb_sel :=dc.io.wb_sel
  ID.to_ex.bits.br_type:=dc.io.br_type
  ID.to_ex.bits.wen :=dc.io.wb_en&&id_valid
  ID.to_ex.bits.rd  :=rd
  ID.to_ex.bits.alu_op:=dc.io.alu_op
  ID.to_ex.bits.src1:=src1
  ID.to_ex.bits.src2:=src2
  ID.to_ex.bits.rdata1:=Regfile.io.rdata1
  ID.to_ex.bits.rdata2:=Regfile.io.rdata2
  ID.to_ex.bits.inst:=ID.IO.bits.inst
  ID.to_ex.bits.pc  :=ID.IO.bits.pc
  ID.to_ex.bits.nextpc:=ID.IO.bits.nextpc

  /*---------------------传递信号到wb级再由wb级处理dpi信号----------------------*/
  ID.to_ex.bits.dpic_bundle.id_inv_flag:=(dc.io.illegal&&ID.IO.bits.nextpc=/="h80000000".U)
  ID.to_ex.bits.dpic_bundle.ex_func_flag:=false.B
  ID.to_ex.bits.dpic_bundle.ex_is_jal:=false.B
  ID.to_ex.bits.dpic_bundle.ex_is_ret:=false.B
  ID.to_ex.bits.dpic_bundle.ex_is_rd0:=false.B
}

