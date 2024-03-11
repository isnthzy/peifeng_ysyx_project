import chisel3._
import chisel3.util._
import config.Configs._

class Axi4Bridge extends Module {
  val io=IO(new Bundle {
    val ar=Decoupled(new AxiAddressBundle())
    val r=Flipped(Decoupled(new AxiReadDataBundle()))

    val aw=Decoupled(new AxiAddressBundle())
    val w=Decoupled(new AxiWriteDataBundle())
    val b=Flipped(Decoupled(new AxiWriteResponseBundle()))

    val addr=Input(UInt(ADDR_WIDTH.W))
    val write_en=Input(Bool())
    val wstrb=Input(UInt(4.W))
    val wdata=Input(UInt(DATA_WIDTH.W))
    val waddr_ok=Output(Bool())
    val wdata_ok=Output(Bool())

    val read_en=Input(Bool())
    val rdata=Output(UInt(DATA_WIDTH.W))
    val raddr_ok=Output(Bool())
    val rdata_ok=Output(Bool())
  })

//---------------------------AXI4 Lite---------------------------
  val WaitWriteIdle=dontTouch(Wire(Bool()))
  //如果write不是idle状态，拉高信号
  val BrespFire=dontTouch(Wire(Bool()))
  //b通道握手，拉高信号

  val WaitReadIdle=dontTouch(Wire(Bool()))
  val RrespFire=dontTouch(Wire(Bool()))
  val ram_rdata=dontTouch(WireDefault(0.U(DATA_WIDTH.W)))
//----------------------AXI4Lite AR Channel----------------------
  val ar_idle :: ar_wait_ready :: ar_wait_rresp ::Nil = Enum(3)
  val arvalidReg=RegInit(false.B)
  val araddrReg=RegInit(0.U(ADDR_WIDTH.W))
  val rvalidReg=RegInit(false.B)

  val ReadRequstState=RegInit(ar_idle)
  when(ReadRequstState===ar_idle){
    when(io.read_en){
      when(WaitWriteIdle){
        when(BrespFire){
          ReadRequstState:=ar_wait_ready
          araddrReg:=io.addr
          arvalidReg:=true.B    
        }
        //如果此时是等待写回复并且已经回复的状态就可以发起ar
      }.otherwise{
        ReadRequstState:=ar_wait_ready
        araddrReg:=io.addr
        arvalidReg:=true.B
      }
    }
  }.elsewhen(ReadRequstState===ar_wait_ready){
    when(io.ar.fire){
      ReadRequstState:=ar_wait_rresp
      arvalidReg:=false.B
      rvalidReg:=true.B 
    }
  }.elsewhen(ReadRequstState===ar_wait_rresp){
    when(io.r.fire){
      ReadRequstState:=ar_idle
      rvalidReg:=false.B

      ram_rdata:=io.r.bits.data
      //不用reg减少一周期
    }
  }
  WaitReadIdle:=(ReadRequstState=/=ar_idle)
  RrespFire:=io.r.fire
  io.ar.valid:=arvalidReg
  io.ar.bits.addr:=araddrReg
  io.ar.bits.prot:=0.U
  io.r.ready:=rvalidReg

  io.raddr_ok:= io.ar.fire
  io.rdata_ok:= WaitReadIdle&&RrespFire
  io.rdata:= ram_rdata
  /*
    如果状态位为
    1.等待read事务空闲，此时读相应，已读出来数据
    2.read事务此时空闲
    拉高rdata_ok
  */
//----------------------AXI4Lite AR Channel----------------------

//-------------------AXI4Lite  W WR B  Channel-------------------
  val wr_idle :: wr_wait_ready :: wr_wait_bresp :: Nil = Enum(3)
  val WriteRequstState=RegInit(wr_idle)
  val awvalidReg=RegInit(false.B)
  val awaddrReg=RegInit(0.U(ADDR_WIDTH.W))
  val wvalidReg=RegInit(false.B)
  val wdataReg=RegInit(0.U(DATA_WIDTH.W))
  val wstrbReg=RegInit(0.U(4.W))
  val breadyReg=RegInit(false.B)

  when(WriteRequstState===wr_idle){
    //当ls级为lw等待rready时,ex级为sw，此时r,aw,w都在等ready，在此处进行一个小仲裁，先让lw握手，
    //等待下一个节点给aw和w握手
    when(io.write_en){
      when(WaitReadIdle){
        when(RrespFire){
          WriteRequstState:=wr_wait_ready
          awvalidReg:=true.B
          awaddrReg:=io.addr
          
          wvalidReg:=true.B
          wdataReg:=io.wdata
          wstrbReg:=io.wstrb
        }
      }.otherwise{
        WriteRequstState:=wr_wait_ready
        awvalidReg:=true.B
        awaddrReg:=io.addr
        
        wvalidReg:=true.B
        wdataReg:=io.wdata
        wstrbReg:=io.wstrb
      }
    }
  }.elsewhen(WriteRequstState===wr_wait_ready){
    when(io.aw.fire&&io.w.fire){
      WriteRequstState:=wr_wait_bresp
      awvalidReg:=false.B
      wvalidReg:=false.B
      breadyReg:=true.B
    }
  }.elsewhen(WriteRequstState===wr_wait_bresp){
    when(io.b.fire){
      WriteRequstState:=wr_idle
      breadyReg:=false.B
    }
  }
  WaitWriteIdle:=(WriteRequstState=/=wr_idle)
  BrespFire:=io.b.fire
  io.aw.bits.prot:=0.U
  io.aw.valid:=awvalidReg
  io.aw.bits.addr:=awaddrReg
  io.w.valid:=wvalidReg
  io.w.bits.data:=wdataReg
  io.w.bits.strb:=wstrbReg
  io.b.ready:=breadyReg

  io.waddr_ok:= io.aw.fire&&io.w.fire
  io.wdata_ok:= WaitWriteIdle&&BrespFire
//---------------------------AXI4 Lite---------------------------
}