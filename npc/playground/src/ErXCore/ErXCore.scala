package ErXCore

import chisel3._
import chisel3.util._

trait HasErXCoreParameter {
  val XLEN = 32
  //
  val InstBuffSize = 8
  //
  val DecodeWidth = 2
  val PrfSize = 64
  val IssueWidth  = 3
  val ExecuteWidth = 3
  val CommitWidth  = 2

  val RobWidth = DecodeWidth
  val RobSize  = 8
  val RobEntries = RobWidth * RobSize
  val RobIdxWidth = log2Up(RobEntries)
  val RobAgeWidth = RobIdxWidth + 1 //Age massge use to issue select!!!
  val RetireWidth = 2
}

trait HasErXCacheConfig{
  def LINE_WIDTH = 128
  def LINE_WORD_NUM = (LINE_WIDTH / 32)

  def TAG_WIDTH = 26
  def INDEX_WIDTH  = 32 - TAG_WIDTH - OFFSET_WIDTH
  def OFFSET_WIDTH = log2Ceil(LINE_WORD_NUM) + 2
  
  def WAY_NUM_I = 2
  def USE_LRU = false
}

trait HasErXCoreConst extends HasErXCoreParameter {
  def INST_NOP = "h00000013".U(XLEN.W)
  def SOC_START_ADDR = "h30000000".U(XLEN.W)
  def NPC_START_ADDR = "h80000000".U(XLEN.W)
}

trait HasErXCoreLog { this: RawModule =>
  implicit val moduleName: String = this.name
}

abstract class ErXCoreModule extends Module with HasErXCoreParameter with HasErXCoreConst with HasErXCoreLog with HasErXCacheConfig
abstract class ErXCoreBundle extends Bundle with HasErXCoreParameter with HasErXCoreConst with HasErXCacheConfig
abstract class ErXCoreBlackBox extends BlackBox with HasErXCoreParameter with HasErXCoreConst with HasErXCacheConfig

class SimTop extends ErXCoreModule {
  override val desiredName = "ysyx_23060115"
  val io = IO(new Bundle {
    val interrupt=if(GenerateParams.getParam("SOC_MODE").asInstanceOf[Boolean]){
      Some(Input(Bool()))
    }else None
    val master=if(GenerateParams.getParam("SOC_MODE").asInstanceOf[Boolean]){ 
      Some(new AxiTopBundle()) 
    }else None
    val slave=if(GenerateParams.getParam("SOC_MODE").asInstanceOf[Boolean]){
      Some(Flipped(new AxiTopBundle())) 
    }else None
  })

  val Frontend  = Module(new Frontend)
  val Backend   = Module(new Backend)
  //Frontend <-> Backend
  Frontend.io.from_bck := Backend.io.fw_frt
  Backend.io.in <> Frontend.io.out

  //
  val Axi4Bridge= Module(new Axi4Bridge())
  val AxiArbiter= Module(new AxiArbiter(inNum = 2))
  AxiArbiter.io.in(1) <> Frontend.io.imem
  AxiArbiter.io.in(0) <> Backend.io.dmem
  Axi4Bridge.io.in <> AxiArbiter.io.out

  if(GenerateParams.getParam("SOC_MODE").asInstanceOf[Boolean]){
    val AxiCoreOut=Module(new AxiCoreOut())
    io.master.get<>AxiCoreOut.io.out

    io.slave.get.awready:=0.U
    io.slave.get.wready :=0.U
    io.slave.get.bvalid :=0.U
    io.slave.get.bid    :=0.U
    io.slave.get.bresp  :=0.U
    io.slave.get.arready:=0.U
    io.slave.get.rvalid :=0.U
    io.slave.get.rid    :=0.U
    io.slave.get.rdata  :=0.U
    io.slave.get.rresp  :=0.U
    io.slave.get.rlast  :=0.U
  // //AxiXBar
    val SimTimer = Module(new SimTimer())

    val AxiXbarA2X = Module(new AxiXbarA2X(
      List(
        (0x02010000L , 0xFDFF0000L, false), //第一行也为默认转发地址，当后面都未命中，转发到默认地址
        (0x02000000L , 0x10000L   , false),
      )
    ))
    Axi4Bridge.io.ar<>AxiXbarA2X.io.a.ar
    Axi4Bridge.io.r <>AxiXbarA2X.io.a.r
    Axi4Bridge.io.aw<>AxiXbarA2X.io.a.aw
    Axi4Bridge.io.w <>AxiXbarA2X.io.a.w
    Axi4Bridge.io.b <>AxiXbarA2X.io.a.b

    AxiXbarA2X.io.x(0)<>AxiCoreOut.io.in
    AxiXbarA2X.io.x(1)<>SimTimer.io
  }else{
    val AxiRam = Module(new Axi4FullSram())
    Axi4Bridge.io.ar<>AxiRam.io.ar
    Axi4Bridge.io.r <>AxiRam.io.r
    Axi4Bridge.io.aw<>AxiRam.io.aw
    Axi4Bridge.io.w <>AxiRam.io.w
    Axi4Bridge.io.b <>AxiRam.io.b
  }

}