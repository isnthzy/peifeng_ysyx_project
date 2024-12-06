package PipeLine

import chisel3._
import chisel3.util._
import CoreConfig.Configs._
import Bundles._
import Axi.AxiBridgeDataLoad
import Util.{Mux1hMap,Mux1hDefMap,Sext,Zext}
import FuncUnit.Control._
import CoreConfig.GenerateParams

class LsStage extends Module {
  val ls=IO(new Bundle {
    val in=Flipped(Decoupled(new Ex2LsBusBundle()))
    val to_wb=Decoupled(new Ls2WbBusBundle())

    val fw_pf=Output(new Pf4LsBusBundle())
    val fw_if=Output(new If4LsBusBundle())
    val fw_id=Output(new Id4LsBusBundle())
    val fw_ex=Output(new Ex4LsBusBundle())

    val to_csr=Output(new Ls2CsrBundle())
    val dl=new AxiBridgeDataLoad()
  })
  val lsExcpEn=dontTouch(Wire(Bool()))
  val lsValid=dontTouch(Wire(Bool()))
  val lsValidR=RegInit(false.B)
  val lsReadyGo=dontTouch(Wire(Bool()))
  val lsStall=dontTouch(Wire(Bool()))
  ls.in.ready:= ~lsValidR || lsReadyGo && ls.to_wb.ready
  when(ls.in.ready){
    lsValidR:=ls.in.valid
  }
  lsValid:=lsValidR
  lsReadyGo:= ~lsStall || lsExcpEn
  ls.to_wb.valid:= lsValid&&lsReadyGo

  lsStall:=ls.in.bits.loadEn&& ~ls.dl.rdata_ok && lsValid

  val loadByteData=Mux1hMap(ls.in.bits.addrLow2Bit,Map(
    "b00".U -> ls.dl.rdata(7 , 0),
    "b01".U -> ls.dl.rdata(15, 8),
    "b10".U -> ls.dl.rdata(23,16),
    "b11".U -> ls.dl.rdata(31,24)
  ))
  val loadHalfData=Mux1hMap(ls.in.bits.addrLow2Bit,Map(
    "b00".U -> ls.dl.rdata(15, 0), 
    "b01".U -> ls.dl.rdata(15, 0),
    "b10".U -> ls.dl.rdata(31,16), 
    "b11".U -> ls.dl.rdata(31,16)
  ))
  val loadDataResult=Mux1hDefMap(ls.in.bits.ldType,Map(
    LD_LW -> ls.dl.rdata,
    LD_LH -> Sext(loadHalfData,32),
    LD_LB -> Sext(loadByteData,32),
    LD_LHU-> Zext(loadHalfData,32),
    LD_LBU-> Zext(loadByteData,32),
  ))

  val ls_result=Mux1hDefMap(ls.in.bits.wbSel,Map(
    WB_ALU -> ls.in.bits.result,
    WB_MEM -> loadDataResult,
  ))


  ls.fw_id.dataUnReady:=ls.in.bits.rfWen&& ~lsReadyGo
  ls.fw_id.rf.wen:=ls.in.bits.rfWen&&lsValidR
  ls.fw_id.rf.waddr:=ls.in.bits.rd
  ls.fw_id.rf.wdata:=ls_result

//Excp:
  val LsExcpType=Wire(new LsExcpTypeBundle())
  LsExcpType.laf:=false.B
  LsExcpType.saf:=false.B
  LsExcpType.lpf:=false.B
  LsExcpType.spf:=false.B
  val excpType=Wire(new ExcpTypeBundle())
  excpType.iam:=ls.in.bits.excpType.num.num.num.iam
  excpType.iaf:=ls.in.bits.excpType.num.num.iaf
  excpType.ine:=ls.in.bits.excpType.num.ine
  excpType.bkp:=ls.in.bits.excpType.num.bkp
  excpType.lam:=ls.in.bits.excpType.lam
  excpType.laf:=LsExcpType.laf
  excpType.sam:=ls.in.bits.excpType.sam
  excpType.saf:=LsExcpType.saf
  excpType.ecu:=ls.in.bits.excpType.num.ecu
  excpType.ecs:=ls.in.bits.excpType.num.ecs
  excpType.ecm:=ls.in.bits.excpType.num.ecm
  excpType.ipf:=ls.in.bits.excpType.num.num.ipf
  excpType.lpf:=LsExcpType.lpf
  excpType.spf:=LsExcpType.spf

  val memBadAddr=ls.in.bits.memBadAddr //为访存地址，
  val excpNum=excpType.asUInt
  val excpResult=Wire(new ExcpResultBundle())
  excpResult:=MuxCase(0.U,Seq(
    excpNum(0)  -> Cat(ECODE.IAM,lsValid ,ls.in.bits.pc    ),
    excpNum(1)  -> Cat(ECODE.IAF,lsValid ,ls.in.bits.pc    ),
    excpNum(2)  -> Cat(ECODE.INE,0.U(1.W),0.U(ADDR_WIDTH.W)),
    excpNum(3)  -> Cat(ECODE.BKP,0.U(1.W),0.U(ADDR_WIDTH.W)),
    excpNum(4)  -> Cat(ECODE.LAM,lsValid ,memBadAddr       ),
    excpNum(5)  -> Cat(ECODE.LAF,0.U(1.W),0.U(ADDR_WIDTH.W)),
    excpNum(6)  -> Cat(ECODE.SAM,lsValid ,memBadAddr       ),
    excpNum(7)  -> Cat(ECODE.SAF,0.U(1.W),0.U(ADDR_WIDTH.W)),
    excpNum(8)  -> Cat(ECODE.ECU,lsValid ,ls.in.bits.pc    ),
    excpNum(9)  -> Cat(ECODE.ECS,lsValid ,ls.in.bits.pc    ),
    excpNum(10) -> Cat(ECODE.ECM,lsValid ,ls.in.bits.pc    ),
    excpNum(11) -> Cat(ECODE.IPF,lsValid ,memBadAddr       ),
    excpNum(12) -> Cat(ECODE.LPF,lsValid ,memBadAddr       ),
    excpNum(13) -> Cat(ECODE.SPF,lsValid ,memBadAddr       ),
  )).asTypeOf(new ExcpResultBundle())
  lsExcpEn:=excpNum.asUInt.orR&&lsValid
  ls.to_csr.wen:=ls.in.bits.csrWen&&lsValid
  ls.to_csr.wrAddr:=ls.in.bits.csrWrAddr
  ls.to_csr.wrData:=ls.in.bits.csrWrData
  ls.to_csr.excpFlush :=excpNum.asUInt.orR&&lsValid
  ls.to_csr.mretFlush :=ls.in.bits.isMret&&lsValid
  ls.to_csr.excpResult:=excpResult

  val refetchFlush=(ls.in.bits.csrWen||ls.in.bits.isFencei)&&lsValid
  val toPipelineFlush=Wire(new PipelineFlushsBundle())
  toPipelineFlush.refetch:=refetchFlush
  toPipelineFlush.excp   :=excpNum.asUInt.orR&&lsValid
  toPipelineFlush.xret   :=ls.in.bits.isMret&&lsValid
  ls.fw_pf.flush:=toPipelineFlush
  ls.fw_pf.refetchPC:=ls.in.bits.pc+4.U
  ls.fw_if.flush:=toPipelineFlush.asUInt.orR
  ls.fw_id.flush:=toPipelineFlush.asUInt.orR
  ls.fw_ex.flush:=toPipelineFlush.asUInt.orR
//diff
  ls.to_wb.bits.diffExcp.excpValid:=toPipelineFlush.excp
  ls.to_wb.bits.diffExcp.isMret   :=ls.in.bits.isMret
  ls.to_wb.bits.diffExcp.intrptNo :=0.U
  ls.to_wb.bits.diffExcp.cause    :=excpResult.ecode
  ls.to_wb.bits.diffExcp.exceptionPC:=ls.in.bits.pc
  ls.to_wb.bits.diffExcp.exceptionInst:=ls.in.bits.inst
  ls.to_wb.bits.diffLoad:=ls.in.bits.diffLoad
  ls.to_wb.bits.diffStore:=ls.in.bits.diffStore
  ls.to_wb.bits.diffLoad.data:=ls.dl.rdata
//NOTE:
  ls.to_wb.bits.isDeviceSkip:=ls.in.bits.isDeviceSkip
  ls.to_wb.bits.excpEn:=excpNum.asUInt.orR
  ls.to_wb.bits.pc:=ls.in.bits.pc
  ls.to_wb.bits.inst:=ls.in.bits.inst
  ls.to_wb.bits.rd:=ls.in.bits.rd
  ls.to_wb.bits.result:=ls_result
  ls.to_wb.bits.rfWen:=ls.in.bits.rfWen

  ls.to_wb.bits.perfMode:=ls.in.bits.perfMode
  if(GenerateParams.getParam("PERF").asInstanceOf[Boolean]){
    val LSUDataRespClockCnt=RegInit(0.U(64.W))
    val LSUInstCnt=RegInit(0.U(64.W))
    when(ls.in.bits.perfMode){
      when(ls.in.bits.diffLoad.valid(0)||ls.in.bits.diffStore.valid(0)){
        LSUDataRespClockCnt:=LSUDataRespClockCnt+1.U
        when(ls.to_wb.fire){
          LSUInstCnt:=LSUInstCnt+1.U
        }
      }
      when(excpType.bkp.asBool && ls.to_wb.fire){
        var CyclePerLSUAddrResp=(LSUDataRespClockCnt.asSInt  * 100.asSInt) / LSUInstCnt.asSInt
        printf("Cycle per lsu  (data resp)(%%): %d%%\n",CyclePerLSUAddrResp);
      }
    }
  }
}