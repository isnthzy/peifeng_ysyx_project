package ErXCore

import chisel3._
import chisel3.util._

class SimTop extends ErXCoreModule {
  
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
  override val desiredName = "ysyx_23060115"
  val Frontend  = Module(new Frontend)
  val Backend   = Module(new Backend)
  //Frontend <-> Backend
  Frontend.io.from_bck := Backend.io.fw_frt
  Backend.io.in <> Frontend.io.out

  //
  val Axi4Bridge = Module(new Axi4Bridge())
  val AxiArbiter = Module(new AxiArbiter(inNum = 2))
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