package PipeLine

import chisel3._
import chisel3.util._
import CoreConfig.Configs._
import Bundles._
import Device.DeviceSkip
import FuncUnit.{Alu}
import FuncUnit.Control._
import Util.{Mux1hDefMap,SDEF}
import Axi.{AxiBridgeAddrLoad,AxiBridgeStore}
import CoreConfig.GenCtrl

class ExStage extends Module {
  val ex=IO(new Bundle {
    val in=Flipped(Decoupled(new Id2ExBusBundle()))
    val to_ls=Decoupled(new Ex2LsBusBundle())

    val fw_pf=Output(new Pf4ExBusBundle())
    val fw_if=Output(new If4ExBusBundle())
    val fw_id=Output(new Id4ExBusBundle())

    val from_ls=Input(new Ex4LsBusBundle)

    val al=new AxiBridgeAddrLoad()
    val s =new AxiBridgeStore()
  })
  val exFlush=dontTouch(Wire(Bool()))
  val exExcpEn=dontTouch(Wire(Bool()))
  exFlush:=ex.from_ls.flush
  val exValid=dontTouch(Wire(Bool()))
  val exValidR=RegInit(false.B)
  val exReadyGo=dontTouch(Wire(Bool()))
  val exStall=dontTouch(Wire(Bool()))
  ex.in.ready:= ~exValidR || exReadyGo && ex.to_ls.ready

  val exDebug0=dontTouch(RegInit(false.B))
  val exDebug1=dontTouch(RegInit(false.B))
  when(exFlush){
    exValidR:=false.B
  }.elsewhen(ex.in.ready){
    exDebug0:=ex.in.ready
    exDebug1:=ex.in.valid
    exValidR:=ex.in.valid
  }
  exValid:=exValidR&& ~exFlush
  exReadyGo:= ~exStall || exExcpEn
  ex.to_ls.valid:= exValid&&exReadyGo

  val csrWrData=Mux1hDefMap(ex.in.bits.csrOp,Map(
    CSR_RW ->  ex.in.bits.src1,
    CSR_RS -> (ex.in.bits.src1 | ex.in.bits.src2),
  ))
  val store_skip=Wire(Bool())

  val csrWen=(ex.in.bits.csrOp===SDEF(CSR_RW)
            ||ex.in.bits.csrOp===SDEF(CSR_RS))
  val isMret=ex.in.bits.csrOp===SDEF(CSR_MRET)
  val storeEn=ex.in.bits.stType=/=SDEF(ST_XXX)&& ~store_skip
  val loadEn =ex.in.bits.ldType=/=SDEF(LD_XXX)

  val Alu=Module(new Alu())
  Alu.io.src1:=Mux(ex.in.bits.brType===SDEF(BR_JALR),ex.in.bits.pc,ex.in.bits.src1)
  Alu.io.src2:=ex.in.bits.src2
  Alu.io.op  :=ex.in.bits.aluOp
  val exResult=Alu.io.result

  ex.fw_id.rf.wen:=ex.in.bits.rfWen&&exValidR
  ex.fw_id.rf.waddr:=ex.in.bits.rd
  ex.fw_id.rf.wdata:=Alu.io.result
  ex.fw_id.dataUnReady:=loadEn&&exValidR

  val brCondTaken=((ex.in.bits.brType===SDEF(BR_EQ) &&  Alu.io.result(0))
                || (ex.in.bits.brType===SDEF(BR_NE) && ~Alu.io.result(0))
                || (ex.in.bits.brType===SDEF(BR_LT) &&  Alu.io.result(0))
                || (ex.in.bits.brType===SDEF(BR_LTU)&&  Alu.io.result(0))
                || (ex.in.bits.brType===SDEF(BR_GE) && ~Alu.io.result(0))
                || (ex.in.bits.brType===SDEF(BR_GEU)&& ~Alu.io.result(0)))&&exValidR
                
  val brCondTarget=ex.in.bits.pc+ex.in.bits.imm
  ex.fw_pf.brCond.taken:=brCondTaken
  ex.fw_pf.brCond.target:=brCondTarget
  ex.fw_if.flush:=brCondTaken
  ex.fw_id.flush:=brCondTaken


  val memMisalignedAddr=ex.in.bits.src1+ex.in.bits.imm
  val memStoreSrc=ex.in.bits.src2
  val memByteSize=(ex.in.bits.ldType===SDEF(LD_LB)||ex.in.bits.ldType===SDEF(LD_LBU)
                 ||ex.in.bits.stType===SDEF(ST_SB))
  val memHalfSize=(ex.in.bits.ldType===SDEF(LD_LH)||ex.in.bits.ldType===SDEF(LD_LHU)
                 ||ex.in.bits.stType===SDEF(ST_SH))
  val memSize=Cat(memHalfSize,memByteSize)
  val memSbSel=Cat(
    memMisalignedAddr(1,0)===3.U,
    memMisalignedAddr(1,0)===2.U,
    memMisalignedAddr(1,0)===1.U,
    memMisalignedAddr(1,0)===0.U
  )
  val memShSel=Cat(
    memMisalignedAddr(1,0)===2.U,
    memMisalignedAddr(1,0)===2.U,
    memMisalignedAddr(1,0)===0.U,
    memMisalignedAddr(1,0)===0.U
  )
  val memSbCont=Cat(
    Fill(8,memSbSel(3))&memStoreSrc(7,0),
    Fill(8,memSbSel(2))&memStoreSrc(7,0),
    Fill(8,memSbSel(1))&memStoreSrc(7,0),
    Fill(8,memSbSel(0))&memStoreSrc(7,0)
  )
  val memShCont=Cat(
    Fill(16,memShSel(3))&memStoreSrc(15,0),
    Fill(16,memShSel(0))&memStoreSrc(15,0)
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
  val memAddr=Cat(memMisalignedAddr(31,2),0.U(2.W))
//NOTE:
  ex.s.wen:=storeEn&&exValid && ~exExcpEn
  ex.s.waddr:=memMisalignedAddr
  ex.s.wstrb:=memWstrb
  ex.s.wdata:=memWdata
  ex.s.wsize:=memDataSize
  ex.al.ren :=loadEn&&exValid && ~exExcpEn
  ex.al.raddr:=memMisalignedAddr
  ex.al.rsize:=memDataSize
  store_skip:=memAddr==="hfffffffc".U
  exStall:=(storeEn&& ~ex.s.wdata_ok 
         || loadEn && ~ex.al.raddr_ok)&&exValid
//NOTE:Excp
  val AddrMisaligned=(memSize(1)&&memAddr(0)
                  || !memSize   &&memAddr(1,0).asUInt.orR)
  val exExcpType=Wire(new ExExcpTypeBundle())
  exExcpType.num:=ex.in.bits.excpType
  exExcpType.lam:=AddrMisaligned&&loadEn&&exValid
  exExcpType.sam:=AddrMisaligned&&storeEn&&exValid
  exExcpEn:=exExcpType.asUInt.orR
  ex.to_ls.bits.excpEn:=exExcpEn
  ex.to_ls.bits.excpType:=exExcpType
//
  val DeviceSkip=Module(new DeviceSkip())
  DeviceSkip.io.isLoadStore:=(loadEn)&&exValidR
  DeviceSkip.io.addr:=memAddr
  val isDeviceSkip=DeviceSkip.io.skip
//
  ex.to_ls.bits.diffStore.valid:=storeEn
  ex.to_ls.bits.diffStore.index:=0.U
  ex.to_ls.bits.diffStore.paddr:=memMisalignedAddr
  ex.to_ls.bits.diffStore.vaddr:=memMisalignedAddr
  ex.to_ls.bits.diffStore.data:=ex.in.bits.src2 //为对齐矫正的data
  ex.to_ls.bits.diffStore.len :=Cat(0.U(5.W),ex.in.bits.stType===SDEF(ST_SW),
                                             ex.in.bits.stType===SDEF(ST_SH),
                                             ex.in.bits.stType===SDEF(ST_SB))
  
  ex.to_ls.bits.diffLoad.valid:=loadEn
  ex.to_ls.bits.diffLoad.index:=0.U
  ex.to_ls.bits.diffLoad.paddr:=memMisalignedAddr
  ex.to_ls.bits.diffLoad.vaddr:=memMisalignedAddr
  ex.to_ls.bits.diffLoad.data :=DontCare
  ex.to_ls.bits.diffLoad.len  :=Cat(0.U(5.W),ex.in.bits.ldType===SDEF(LD_LW),
                                             ex.in.bits.ldType===SDEF(LD_LH)||ex.in.bits.ldType===SDEF(LD_LHU),
                                             ex.in.bits.ldType===SDEF(LD_LB)||ex.in.bits.ldType===SDEF(LD_LBU))
//NOTE:
  ex.to_ls.bits.isDeviceSkip:=isDeviceSkip
  ex.to_ls.bits.memBadAddr:=memMisalignedAddr
  ex.to_ls.bits.isMret :=isMret
  ex.to_ls.bits.csrWen :=csrWen
  ex.to_ls.bits.csrWrAddr:=ex.in.bits.csrWrAddr
  ex.to_ls.bits.csrWrData:=csrWrData
  ex.to_ls.bits.pc:=ex.in.bits.pc
  ex.to_ls.bits.inst:=ex.in.bits.inst
  ex.to_ls.bits.rd:=ex.in.bits.rd
  ex.to_ls.bits.result:=exResult
  ex.to_ls.bits.addrLow2Bit:=memMisalignedAddr(1,0)
  ex.to_ls.bits.storeEn:=storeEn
  ex.to_ls.bits.loadEn :=loadEn
  ex.to_ls.bits.ldType:=ex.in.bits.ldType
  ex.to_ls.bits.wbSel :=ex.in.bits.wbSel
  ex.to_ls.bits.rfWen :=ex.in.bits.rfWen

  ex.to_ls.bits.perfMode:=ex.in.bits.perfMode
  if(GenCtrl.PERF){
    val LSUAddrRespClockCnt=RegInit(0.U(32.W))
    val LSUInstCnt=RegInit(0.U(32.W))
    when(ex.in.bits.perfMode){
      when(storeEn||loadEn){
        LSUAddrRespClockCnt:=LSUAddrRespClockCnt+1.U
        when(ex.to_ls.fire){
          LSUInstCnt:=LSUInstCnt+1.U
        }
      }
      when(exExcpType.num.bkp.asBool && ex.to_ls.fire){
        printf("lsu int cnt: %d\n",LSUInstCnt);
        var CyclePerLSUAddrResp=(LSUInstCnt.asSInt * 100.asSInt) / LSUAddrRespClockCnt.asSInt
        printf("Cycle per lsu(addr resp)(%%): %d%%\n",CyclePerLSUAddrResp);
      }
    }
  }
}