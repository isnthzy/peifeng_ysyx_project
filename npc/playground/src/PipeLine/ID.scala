package PipeLine
import chisel3._
import chisel3.util._
import CoreConfig.Configs._
import Bundles._
import FuncUnit.Control._
import FuncUnit.{Decode,ImmGen,RegFile}
import Util.{Mux1hDefMap,SDEF}
import CoreConfig.GenerateParams

class IdStage extends Module {
  val id=IO(new Bundle {
    val in=Flipped(Decoupled(new If2IdBusBundle()))
    val to_ex=Decoupled(new Id2ExBusBundle())

    val fw_pf=Output(new Pf4IdBusBundle())
    val fw_if=Output(new If4IdBusBundle())

    val from_ex=Input(new Id4ExBusBundle())
    val from_ls=Input(new Id4LsBusBundle())
    val from_wb=Input(new Id4WbBusBundle())
    val from_csr=Flipped(new PipeLine4CsrBundle())

    val diffREG=Output((Vec(32, UInt(32.W))))
  })
  val idFlush=dontTouch(Wire(Bool()))
  val idExcpEn=dontTouch(Wire(Bool()))
  idFlush:=id.from_ex.flush||id.from_ls.flush
  val idValid=dontTouch(Wire(Bool()))
  val idValidR=RegInit(false.B)
  val idReadyGo=dontTouch(Wire(Bool()))
  val idStall=dontTouch(Wire(Bool()))
  id.in.ready:= ~idValidR || idReadyGo && id.to_ex.ready
  when(idFlush){
    idValidR:=false.B
  }.elsewhen(id.in.ready){
    idValidR:=id.in.valid
  }
  idValid:=idValidR&& ~idFlush
  idReadyGo:= ~idStall||idExcpEn
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
    SDEF(CSR_BREK)  -> 10.U,
    SDEF(CSR_ECAL)  -> RISCV32E_ECALLREG,
  ))
  id.diffREG:=Regfile.io.diffREG

  //当ebreak时，算出reg(10)+0的结果并通知dpi-c，即reg(10)==return
  //当ecall时，算出reg(ECALL_REG)+0的结果并传递给WB的csr处理

  //前递处理
  val rdata1=dontTouch(Wire(UInt(DATA_WIDTH.W)))
  val rdata2=dontTouch(Wire(UInt(DATA_WIDTH.W)))
  val rdata1Ready=dontTouch(Wire(Bool()))
  val rdata2Ready=dontTouch(Wire(Bool()))
  idStall:= ~(rdata1Ready&&rdata2Ready)
  when(Decode.io.aSel===SDEF(A_PC)){
    rdata1Ready:=true.B
    rdata1:=0.U
  }.otherwise{
    when(Regfile.io.raddr1===0.U){
      rdata1Ready:=true.B
      rdata1:=0.U
    }.elsewhen(Regfile.io.raddr1===id.from_ex.rf.waddr&&id.from_ex.rf.wen){
      rdata1Ready:= ~id.from_ex.dataUnReady
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
      rdata2Ready:= ~id.from_ex.dataUnReady
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

  val isJal =Decode.io.brType===SDEF(BR_JAL)
  val isJalr=Decode.io.brType===SDEF(BR_JALR)
  val brJumpTaken=(isJal||isJalr)&&idValidR

  val jal_target=id.in.bits.pc+imm
  val jalr_target=Cat((rdata1+imm)(31,1),0.U(1.W))
  val brJumpTarget=((Fill(ADDR_WIDTH,isJal) &jal_target)
                  | (Fill(ADDR_WIDTH,isJalr)&jalr_target))

  id.fw_pf.brJump.taken:=brJumpTaken
  id.fw_pf.brJump.target:=brJumpTarget
  id.fw_if.flush:=brJumpTaken

  val brCondTarget=jal_target

//NOTE:Excp
  val idExcpType=Wire(new IdExcpTypeBundle())
  idExcpType.num:=id.in.bits.excpType
  idExcpType.ine:=Decode.io.illigal
  idExcpType.bkp:=Decode.io.csrOp===SDEF(CSR_BREK)
  idExcpType.ecu:=false.B
  idExcpType.ecs:=false.B
  idExcpType.ecm:=Decode.io.csrOp===SDEF(CSR_ECAL)
  idExcpEn:=idExcpType.asUInt.orR
  id.to_ex.bits.excpEn:=idExcpEn
  id.to_ex.bits.excpType:=idExcpType
//------------------------------------------
  id.to_ex.bits.pc:=id.in.bits.pc
  id.to_ex.bits.inst:=id.in.bits.inst
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

  id.to_ex.bits.perfMode:=id.in.bits.perfMode
  if(GenerateParams.getParam("PERF").asInstanceOf[Boolean]){
    val totalCnt=RegInit(0.U(32.W))
    val aluCnt=RegInit(0.U(32.W))
    val brCnt=RegInit(0.U(32.W))
    val jpCnt=RegInit(0.U(32.W))
    val ldCnt=RegInit(0.U(32.W))
    val stCnt=RegInit(0.U(32.W))
    when(id.in.bits.perfMode){
      when(id.to_ex.fire){
        totalCnt:=totalCnt+1.U
        when(Decode.io.aluOp=/=SDEF(ALU_XXX)){
          aluCnt:=aluCnt+1.U
        }
        when(Decode.io.brType===SDEF(BR_JAL)
           | Decode.io.brType===SDEF(BR_JALR)){
          brCnt:=brCnt+1.U
        }
        when(Decode.io.brType=/=SDEF(LD_XXX)
           & Decode.io.brType=/=SDEF(BR_JAL)
           & Decode.io.brType=/=SDEF(BR_JALR)){
          jpCnt:=jpCnt+1.U
        }
        when(Decode.io.ldType=/=SDEF(LD_XXX)){
          ldCnt:=ldCnt+1.U
        }
        when(Decode.io.stType=/=SDEF(ST_XXX)){
          stCnt:=stCnt+1.U
        }
      }
      when(idExcpType.bkp.asBool && id.to_ex.fire){
        var aluRealCnt=aluCnt-brCnt-ldCnt-stCnt
        printf("============= perf =============\n")
        printf("Total inst cnt: %d\n",totalCnt)
        printf("ALU:%d, rate=%d%%\n",aluRealCnt,(aluRealCnt.asSInt*100.asSInt)/totalCnt.asSInt)
        printf("BR :%d, rate=%d%%\n",brCnt,(brCnt.asSInt*100.asSInt)/totalCnt.asSInt)
        printf("JP :%d, rate=%d%%\n",brCnt,(jpCnt.asSInt*100.asSInt)/totalCnt.asSInt)
        printf("LD :%d, rate=%d%%\n",ldCnt,(ldCnt.asSInt*100.asSInt)/totalCnt.asSInt)
        printf("ST :%d, rate=%d%%\n",stCnt,(stCnt.asSInt*100.asSInt)/totalCnt.asSInt)
     }
    }

  }
}