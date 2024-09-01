package PipeLine
import chisel3._
import chisel3.util._
import config.Configs._
import Bundles._
import FuncUnit.Control._
import FuncUnit.{Decode,ImmGen,RegFile}
import Util.{Mux1hDefMap,SDEF}

class IdStage extends Module {
  val id=IO(new Bundle {
    val in=Flipped(Decoupled(new If2IdBusBundle()))
    val to_ex=Decoupled(new Id2ExBusBundle())

    val fw_pf=Output(new Pf4IdBusBundle())
    val fw_if=Output(new If4IdBusBundle())

    val from_ex=Input(new Id4ExBusBundle())
    val from_ls=Input(new Id4LsBusBundle())
    val from_wb=Input(new Id4WbBusBundle())
    val from_csr=new PipeLine4CsrBundle()
  })
  val idFlush=dontTouch(Wire(Bool()))
  idFlush:=id.from_ex.flush||id.from_ls.flush
  val idValid=dontTouch(Wire(Bool()))
  val idValidR=RegInit(false.B)
  val idReadyGo=dontTouch(Wire(Bool()))
  id.in.ready:=id.to_ex.ready&& ~idValidR || idReadyGo
  when(idFlush){
    idValidR:=false.B
  }.elsewhen(id.in.ready){
    idValidR:=id.in.valid
  }
  idValid:=idValidR&& ~idFlush
  idReadyGo:=true.B
  id.to_ex.valid:= idValid&&idReadyGo
  val Decode=Module(new Decode())
  val ImmGen=Module(new ImmGen())
  val imm=dontTouch(Wire(UInt(32.W)))
  val rs1=dontTouch(Wire(UInt(5.W)))
  val rs2=dontTouch(Wire(UInt(5.W)))
  val funct3=dontTouch(Wire(UInt(3.W)))
  val rd=dontTouch(Wire(UInt(5.W)))
  val opcode=dontTouch(Wire(UInt(7.W)))
  val csrAddr=dontTouch(Wire(UInt(12.W)))
  
  Decode.io.inst:=id.in.bits.inst

  ImmGen.io.inst:=id.in.bits.inst
  ImmGen.io.sel :=Decode.io.immType

  imm := ImmGen.io.out
  rs2 := id.in.bits.inst(24, 20)
  rs1 := id.in.bits.inst(19, 15)
  funct3 := id.in.bits.inst(14, 12)
  rd := id.in.bits.inst(11, 7)
  opcode := id.in.bits.inst(6, 0)
  csrAddr := id.in.bits.inst(31, 20)

  id.from_csr.rdAddr:=csrAddr
  val Regfile=Module(new RegFile())
  Regfile.io.raddr1:=rs1
  Regfile.io.raddr2:=MuxLookup(Decode.io.csrOp,rs2)(Seq(
    SDEF(CSR_BREK)  ->10.U,
    SDEF(CSR_ECAL) ->RISCV32E_ECALLREG
  ))
  //当ebreak时，算出reg(10)+0的结果并通知dpi-c，即reg(10)==return
  //当ecall时，算出reg(ECALL_REG)+0的结果并传递给WB的csr处理

  //前递处理
  val rdata1=dontTouch(Wire(UInt(DATA_WIDTH.W)))
  val rdata2=dontTouch(Wire(UInt(DATA_WIDTH.W)))
  val rdata1Ready=dontTouch(Wire(Bool()))
  val rdata2Ready=dontTouch(Wire(Bool()))
  when(Decode.io.aSel===SDEF(A_PC)){
    rdata1Ready:=true.B
    rdata1:=0.U
  }.otherwise{
    when(Regfile.io.raddr1===0.U){
      rdata1Ready:=true.B
      rdata1:=0.U
    }.elsewhen(Regfile.io.raddr1===id.from_ex.rf.waddr&&id.from_ex.rf.wen){
      rdata1Ready:=true.B
      rdata1:=id.from_ex.rf.wdata
    }.elsewhen(Regfile.io.raddr1===id.from_ls.rf.waddr&&id.from_ls.rf.wen){
      rdata1Ready:= ~id.from_ls.dataUnReady
      rdata1:=id.from_ls.rf.wdata
    }.elsewhen(Regfile.io.raddr1===id.from_wb.rf.waddr&&id.from_wb.rf.wen){
      rdata1Ready:=true.B
      rdata1:=id.from_wb.rf.wdata
    }.otherwise{
      rdata1Ready:=true.B
      rdata1:=Regfile.io.rdata1
    }
  }

  when(Decode.io.bSel===SDEF(B_IMM)){
    rdata2Ready:=true.B
    rdata2:=0.U
    //TODO:CSR的依然要处理前递
  }.otherwise{
    when(Regfile.io.raddr2===0.U){
      rdata2Ready:=true.B
      rdata2:=0.U
    }.elsewhen(Regfile.io.raddr2===id.from_ex.rf.waddr&&id.from_ex.rf.wen){
      rdata2Ready:=true.B
      rdata2:=id.from_ex.rf.wdata
    }.elsewhen(Regfile.io.raddr2===id.from_ls.rf.waddr&&id.from_ls.rf.wen){
      rdata2Ready:= ~id.from_ls.dataUnReady
      rdata2:=id.from_ls.rf.wdata
    }.elsewhen(Regfile.io.raddr2===id.from_wb.rf.waddr&&id.from_wb.rf.wen){
      rdata2Ready:=true.B
      rdata2:=id.from_wb.rf.wdata
    }.otherwise{
      rdata2Ready:=true.B
      rdata2:=Regfile.io.rdata2
    }
  }


  // //在id级实例化CSR，通过ex前递写回
  // val csr_out_data=dontTouch(Wire(UInt(DATA_WIDTH.W)))
  // val Csrfile=Module(new CsrFile())
  // Csrfile.io.csr_cmd:=dc.io.csr_cmd
  // csr_out_data:=Mux(csr_addr===id.from_ex.csr.waddr,id.from_ex.csr.wdata,Csrfile.io.out)
  // //简化的csr实现，先用前递代替，实际上应该是清空
  // Csrfile.io.csr_raddr:=csr_addr
  // Csrfile.io.csr_wen:=id.from_ex.csr.wen
  // Csrfile.io.csr_waddr:=id.from_ex.csr.waddr
  // Csrfile.io.csr_wdata:=id.from_ex.csr.wdata
  // Csrfile.io.ecpt_wen :=id.from_ex.csr.ecpt.wen
  // Csrfile.io.mepc_in  :=id.from_ex.csr.ecpt.pc_wb
  // Csrfile.io.mcause_in:=id.from_ex.csr.ecpt.mcause_in

  // id.to_ex.bits.csr_addr:=csr_addr
  // id.to_ex.bits.csr_cmd:=dc.io.csr_cmd
  // id.to_ex.bits.pc_sel:=dc.io.pc_sel
  // id.to_ex.bits.csr_global<>Csrfile.io.global
  val csr_rdata=Wire(UInt(DATA_WIDTH.W))

  val src1=Mux1hDefMap(Decode.io.aSel,Map(
    A_RS1 -> rdata1,
    A_PC  -> id.in.bits.pc
  ))
  val src2=Mux1hDefMap(Decode.io.bSel,Map(
    B_RS2 -> rdata2,
    B_IMM -> imm,
    B_CSR -> id.from_csr.rdData,
  ))
  Regfile.io.waddr:=id.from_wb.rf.waddr
  Regfile.io.wdata:=id.from_wb.rf.wdata
  Regfile.io.wen  :=id.from_wb.rf.wen

  val isJal=Decode.io.brType===SDEF(BR_JAL)
  val isJalr=Decode.io.brType===SDEF(BR_JALR)
  val brJumpTaken=isJal||isJalr

  val jal_target=id.in.bits.pc+imm
  val jalr_target=Cat((rdata1+imm)(31,1),0.U(1.W))
  val brJumpTarget=((Fill(ADDR_WIDTH,isJal) &jal_target)
                  | (Fill(ADDR_WIDTH,isJalr)&jal_target))

  id.fw_pf.brJump.taken:=brJumpTaken
  id.fw_pf.brJump.target:=brJumpTarget
  id.fw_if.flush:=brJumpTaken

  val brCondTarget=jal_target
//------------------------------------------
  id.to_ex.bits.pc:=id.in.bits.pc
  id.to_ex.bits.rd:=rd
  id.to_ex.bits.src1:=src1  
  id.to_ex.bits.src2:=src2
  id.to_ex.bits.imm:=imm
  id.to_ex.bits.aluOp:=Decode.io.aluOp
  id.to_ex.bits.csrWrAddr:=csrAddr
  id.to_ex.bits.csrOp:=Decode.io.csrOp
  id.to_ex.bits.brType:=Decode.io.brType
  id.to_ex.bits.stType:=Decode.io.stType
  id.to_ex.bits.ldType:=Decode.io.ldType
  id.to_ex.bits.wbSel:=Decode.io.wbSel
  id.to_ex.bits.rfWen:=Decode.io.rfWen
}