package ErXCore
import chisel3._
import chisel3.util._

//NOTE:这个模块的作用是仅仅为了把信号类型转换成适用于soc的外接信号
//为了美观把他挂在xbar下面
class AxiCoreOut extends ErXCoreModule {
  val io=IO(new Bundle{
    val in  = Flipped(new Axi4Master())
    val out = new AxiTopBundle()
  })
  io.in.aw.ready:=io.out.awready
  io.out.awvalid:=io.in.aw.valid
  io.out.awaddr :=io.in.aw.bits.addr
  io.out.awid   :=io.in.aw.bits.id
  io.out.awlen  :=io.in.aw.bits.len
  io.out.awsize :=io.in.aw.bits.size
  io.out.awburst:=io.in.aw.bits.burst

  io.in.w.ready:=io.out.wready
  io.out.wvalid:=io.in.w.valid
  io.out.wdata :=io.in.w.bits.data
  io.out.wstrb :=io.in.w.bits.strb
  io.out.wlast :=io.in.w.bits.last

  io.out.bready:=io.in.b.ready
  io.in.b.valid:=io.out.bvalid
  io.in.b.bits.resp:=io.out.bresp
  io.in.b.bits.id  :=io.out.bid

  io.in.ar.ready:=io.out.arready
  io.out.arvalid:=io.in.ar.valid
  io.out.araddr :=io.in.ar.bits.addr
  io.out.arid   :=io.in.ar.bits.id
  io.out.arlen  :=io.in.ar.bits.len
  io.out.arsize :=io.in.ar.bits.size
  io.out.arburst:=io.in.ar.bits.burst

  io.out.rready:=io.in.r.ready
  io.in.r.valid:=io.out.rvalid
  io.in.r.bits.data:=io.out.rdata
  io.in.r.bits.resp:=io.out.rresp
  io.in.r.bits.id  :=io.out.rid
  io.in.r.bits.last:=io.out.rlast
}

class AxiTopBundle extends ErXCoreBundle {
  val awready=Input(Bool())
  val awvalid=Output(Bool())
  val awaddr =Output(UInt(XLEN.W))
  val awid   =Output(UInt(4.W))
  val awlen  =Output(UInt(8.W))
  val awsize =Output(UInt(3.W))
  val awburst=Output(UInt(2.W))

  val wready =Input(Bool())
  val wvalid =Output(Bool())
  val wdata  =Output(UInt(XLEN.W))
  val wstrb  =Output(UInt((XLEN/8).W))
  val wlast  =Output(Bool())

  val bready =Output(Bool())
  val bvalid =Input(Bool())
  val bresp  =Input(UInt(2.W))
  val bid    =Input(UInt(4.W))

  val arready=Input(Bool())
  val arvalid=Output(Bool())
  val araddr =Output(UInt(XLEN.W))
  val arid   =Output(UInt(4.W))
  val arlen  =Output(UInt(8.W))
  val arsize =Output(UInt(3.W))
  val arburst=Output(UInt(2.W))
  
  val rready =Output(Bool())
  val rvalid =Input(Bool())
  val rresp  =Input(UInt(2.W))
  val rdata  =Input(UInt(XLEN.W))
  val rlast  =Input(Bool())
  val rid    =Input(UInt(4.W))
}