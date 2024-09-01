package PipeLine

import chisel3._
import chisel3.util._
import config.Configs._
import Bundles._
import FuncUnit.{Alu}
import FuncUnit.Control._
import Util.{Mux1hDefMap,SDEF}
import Axi.{AxiBridgeAddrLoad,AxiBridgeStore}

class ExStage extends Module {
  val ex=IO(new Bundle {
    val in=Flipped(Decoupled(new Id2ExBusBundle()))
    val to_ls=Decoupled(new Ex2LsBusBundle())

    val fw_pf=Output(new Pf4ExBusBundle())
    val fw_if=Output(new If4ExBusBundle())
    val fw_id=Output(new Id4ExBusBundle())

    val from_ls=Input(new Ex4LsBusBundle)

    val al=Output(new AxiBridgeAddrLoad())
    val s =Output(new AxiBridgeStore())
  })
  val exFlush=dontTouch(Wire(Bool()))
  exFlush:=ex.from_ls.flush
  val exValid=dontTouch(Wire(Bool()))
  val exValidR=RegInit(false.B)
  val exReadyGo=dontTouch(Wire(Bool()))
  ex.in.ready:=ex.to_ls.ready&& ~exValidR || exReadyGo
  when(exFlush){
    exValidR:=false.B
  }.elsewhen(ex.in.ready){
    exValidR:=ex.in.valid
  }
  exValid:=exValidR&& ~exFlush
  exReadyGo:=true.B
  ex.to_ls.valid:= exValid&&exReadyGo

  val csrWrData=Mux1hDefMap(ex.in.bits.csrOp,Map(
    CSR_RW ->  ex.in.bits.src1,
    CSR_RS -> (ex.in.bits.src1 | ex.in.bits.src2),
  ))
  val csrWen=(ex.in.bits.csrOp===SDEF(CSR_RW)
            ||ex.in.bits.csrOp===SDEF(CSR_RS))

  val Alu=Module(new Alu())
  Alu.io.src1:=ex.in.bits.src1
  Alu.io.src2:=ex.in.bits.src2
  Alu.io.op  :=ex.in.bits.aluOp
  val exResult=Alu.io.result

  ex.fw_id.rf.wen:=ex.in.bits.rfWen
  ex.fw_id.rf.waddr:=ex.in.bits.rd
  ex.fw_id.rf.wdata:=Alu.io.result

  val brCondTaken=((ex.in.bits.brType===SDEF(BR_EQ) &&  Alu.io.result(0))
                || (ex.in.bits.brType===SDEF(BR_NE) && ~Alu.io.result(0))
                || (ex.in.bits.brType===SDEF(BR_LT) &&  Alu.io.result(0))
                || (ex.in.bits.brType===SDEF(BR_LTU)&&  Alu.io.result(0))
                || (ex.in.bits.brType===SDEF(BR_GE) && ~Alu.io.result(0))
                || (ex.in.bits.brType===SDEF(BR_GEU)&& ~Alu.io.result(0)))
                
  val brCondTarget=ex.in.bits.pc+ex.in.bits.imm
  ex.fw_pf.brCond.taken:=brCondTaken
  ex.fw_pf.brCond.target:=brCondTarget
  ex.fw_if.flush:=brCondTaken
  ex.fw_id.flush:=brCondTaken


  val memAddr=ex.in.bits.src1+ex.in.bits.imm
  val memStoreSrc=ex.in.bits.src2
  val memByteSize=(ex.in.bits.ldType===SDEF(LD_LB)||ex.in.bits.ldType===SDEF(LD_LBU)
                 ||ex.in.bits.stType===SDEF(ST_SB))
  val memHalfSize=(ex.in.bits.ldType===SDEF(LD_LH)||ex.in.bits.ldType===SDEF(LD_LHU)
                 ||ex.in.bits.stType===SDEF(ST_SH))
  val memSize=Cat(memHalfSize,memByteSize)
  val memSbSel=Cat(
    memAddr(1,0)===3.U,
    memAddr(1,0)===2.U,
    memAddr(1,0)===1.U,
    memAddr(1,0)===0.U
  )
  val memShSel=Cat(
    memAddr(1,0)===2.U,
    memAddr(1,0)===2.U,
    memAddr(1,0)===0.U,
    memAddr(1,0)===0.U
  )
  val memSbCont=Cat(
    Fill(8,memSbSel(3))&memStoreSrc(7,0),
    Fill(8,memSbSel(2))&memStoreSrc(7,0),
    Fill(8,memSbSel(1))&memStoreSrc(7,0),
    Fill(8,memSbSel(0))&memStoreSrc(7,0)
  )
  val memShCont=Cat(
    Fill(8,memShSel(3))&memStoreSrc(7,0),
    Fill(8,memShSel(2))&memStoreSrc(7,0),
    Fill(8,memShSel(1))&memStoreSrc(7,0),
    Fill(8,memShSel(0))&memStoreSrc(7,0)
  )
  val memDataSize=(
    Fill(3,memSize(0))&0.U(3.W)
   |Fill(3,memSize(1))&1.U(3.W)
   |Fill(3, !memSize) &2.U(3.W)
  )
  val memWstrb=(
    Fill(4,memSize(0))&memSbSel
   |Fill(4,memSize(1))&memShSel
   |Fill(4, !memSize) &15.U(4.W)
  )
  val memWdata=(
    Fill(ADDR_WIDTH,memSize(0))&memSbCont
   |Fill(ADDR_WIDTH,memSize(1))&memShCont
   |Fill(ADDR_WIDTH, !memSize )&memStoreSrc
  )
  val storeEn=ex.in.bits.stType=/=SDEF(ST_XXX)
  val loadEn=ex.in.bits.ldType=/=SDEF(LD_XXX)
//NOTE:
  ex.s.wen:=storeEn&&exValid
  ex.s.waddr:=memAddr
  ex.s.wstrb:=memWstrb
  ex.s.wdata:=memWdata
  ex.al.ren:=loadEn&&exValid
  ex.al.raddr:=memAddr

//NOTE:
  ex.to_ls.bits.csrWen :=csrWen
  ex.to_ls.bits.csrWrAddr:=ex.in.bits.csrWrAddr
  ex.to_ls.bits.csrWrData:=csrWrData
  ex.to_ls.bits.pc:=ex.in.bits.pc
  ex.to_ls.bits.rd:=ex.in.bits.rd
  ex.to_ls.bits.result:=exResult
  ex.to_ls.bits.addrLow2Bit:=memAddr(1,0)
  ex.to_ls.bits.storeEn:=storeEn
  ex.to_ls.bits.loadEn :=loadEn
  ex.to_ls.bits.ldType:=ex.in.bits.ldType
  ex.to_ls.bits.wbSel :=ex.in.bits.wbSel
  ex.to_ls.bits.rfWen :=ex.in.bits.rfWen

}