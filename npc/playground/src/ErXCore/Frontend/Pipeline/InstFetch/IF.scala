package ErXCore
import chisel3._
import chisel3.util._
// import CoreConfig.Configs._
// import CoreConfig.GenerateParams
// import Cache.Core2AxiRespondIO

class IfStage extends ErXCoreModule {
  val io=IO(new Bundle {
    val in = Flipped(Decoupled(new Pf2IfBusBundle()))
    val to_id = Decoupled(new InstIO())
    val from_bck = Input(new Bundle {
      val flush = Input(Bool())
    })

    val dl = new Core2AxiRespondIO()

    val perfMode = Output(Bool()) //飞线到if级...
    val programExit = Input(Bool())
  })
  val fsFlush=dontTouch(Wire(Bool()))
  val fsStall=dontTouch(Wire(Bool()))
  val fsExcpEn=dontTouch(Wire(Bool()))
  fsFlush:=io.from_bck.flush
  val fsValid=dontTouch(Wire(Bool()))
  val fsValidR=RegInit(false.B)
  val fsReadyGo=dontTouch(Wire(Bool()))
  val holdValid=RegInit(false.B)

  io.in.ready:= ~fsValidR || fsReadyGo && io.to_id.ready
  when(fsFlush){
    fsValidR:=false.B
  }.elsewhen(io.in.ready){
    fsValidR:=io.in.valid
  }
  fsValid:=fsValidR&& ~fsFlush
  fsReadyGo:= ~fsStall || fsExcpEn
  io.to_id.valid:=fsValid&&fsReadyGo //fsValid===fsValidR&& ~fsFlush
  val inst_discard=RegInit(false.B)
  when(fsFlush&& ~io.in.ready&& ~fsReadyGo){
    inst_discard:=true.B
  }.elsewhen(io.dl.dataOk){
    inst_discard:=false.B
  }
  fsStall:= ~(io.dl.dataOk||holdValid) || inst_discard

  val fsInstBuff=RegInit(0.U(XLEN.W))
  val fsUseInstBuff=RegInit(false.B)
  val fsInst=Mux(fsExcpEn,INST_NOP,
              Mux(fsUseInstBuff&& ~io.dl.dataOk,fsInstBuff,io.dl.data))
  when(io.to_id.fire){
    fsUseInstBuff:=false.B
    holdValid:=false.B
  }.elsewhen(io.dl.dataOk& ~fsFlush){
    fsInstBuff:=io.dl.data
    fsUseInstBuff:= ~io.to_id.ready
    holdValid:= ~io.to_id.ready
  }

//NOTE:Excp
  val fsExcpType=Wire(new IfExcpTypeBundle())
  fsExcpType.num:=io.in.bits.excpType
  fsExcpType.iaf:=false.B
  fsExcpType.ipf:=false.B
  fsExcpEn:=fsExcpType.asUInt.orR
  // io.to_id.bits.excpEn:=fsExcpEn
  // io.to_id.bits.excpType:=fsExcpType

  val perfMode=RegInit(false.B)
  io.to_id.bits.pc:=io.in.bits.pc
  io.to_id.bits.inst:=fsInst
  // io.to_id.bits.perfMode:=perfMode

  io.perfMode:=perfMode
  // if(GenerateParams.getParam("PERF").asInstanceOf[Boolean]){
  //   val OpenCalculateIPC=Module(new OpenCalculateIPC())
    
  //   OpenCalculateIPC.io.clock:=clock
  //   OpenCalculateIPC.io.valid:=false.B
  //   //NOTE:这样做的目的是当我们使用soc时略去bootloader阶段，等到进入程序后通知npc开始计算ipc
  //   //使用CSRRS读取学号寄存器作为了判断是否进入程序的条件
  //   when((fsInst===BitPat("b11110001001000000010?????1110011"))&&io.to_id.fire){
  //     perfMode:=true.B
  //     OpenCalculateIPC.io.valid:=true.B
  //   }
  // }
  // if(GenerateParams.getParam("PERF").asInstanceOf[Boolean]){
  //   val FetchDataClockCnt=RegInit(0.U(64.W))
  //   val InstCnt=RegInit(0.U(64.W))
  //   when(perfMode){
  //     FetchDataClockCnt:=FetchDataClockCnt+1.U
  //     when(io.to_id.fire){
  //       InstCnt:=InstCnt+1.U
  //     }
  //     when(io.programExit){
  //       var CyclePerFetchDataResp=(FetchDataClockCnt.asSInt  * 100.asSInt) / InstCnt.asSInt
  //       printf("Cycle per fetch(data resp)(%%): %d%%\n",CyclePerFetchDataResp);
  //     }
  //   }
  // }

}

class OpenCalculateIPC extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val valid = Input(Bool())
  })
  setInline("OpenCalculateIPC.v",
    """import "DPI-C" function void open_npc_calculate_ipc();
      |module OpenCalculateIPC(
      |    input              clock,
      |    input              valid
      |);
      |always@(posedge clock) begin
      |  if(valid) begin
      |    open_npc_calculate_ipc();
      |  end
      |end
      |endmodule
    """.stripMargin)
}