package Axi

import chisel3._
import chisel3.util._
import CoreConfig.Configs._

class Axi4Bridge extends Module {
  val io=IO(new Bundle {
    val ar=Decoupled(new AxiAddressBundle())
    val r=Flipped(Decoupled(new AxiReadDataBundle()))

    val aw=Decoupled(new AxiAddressBundle())
    val w=Decoupled(new AxiWriteDataBundle())
    val b=Flipped(Decoupled(new AxiWriteResponseBundle()))

    val al=Flipped(new AxiBridgeAddrLoad)
    val dl=Flipped(new AxiBridgeDataLoad)
    val s=Flipped(new AxiBridgeStore)

  })

//---------------------------AXI4 Lite---------------------------
  val WaitWriteIdle=dontTouch(Wire(Bool()))
  //如果write不是idle状态，拉高信号
  val BrespFire=dontTouch(Wire(Bool()))
  //b通道握手，拉高信号
  val LoadStoreFire=io.al.ren&&io.s.wen

  val WaitReadIdle=dontTouch(Wire(Bool()))
  val RrespFire=dontTouch(Wire(Bool()))
  val ram_rdata=dontTouch(WireDefault(0.U(DATA_WIDTH.W)))
//---------------------AXI4Lite AR R Channel---------------------
  val ar_idle :: ar_wait_ready :: ar_wait_rresp ::Nil = Enum(3)
  val arvalidReg=RegInit(false.B)
  val araddrReg=RegInit(0.U(ADDR_WIDTH.W))
  val rreadyReg=RegInit(false.B)

  val ReadRequstState=RegInit(ar_idle)
  when(ReadRequstState===ar_idle){
    when(io.al.ren){
      when(~LoadStoreFire){ //仲裁:当取指(load)和store同时发生时,阻塞取指(load),先让store通过
        when(WaitWriteIdle){
          when(BrespFire){
            ReadRequstState:=ar_wait_ready
            araddrReg:=io.al.raddr
            arvalidReg:=true.B    
          }
          //如果此时是等待写回复并且已经回复的状态就可以发起ar
        }.otherwise{
          ReadRequstState:=ar_wait_ready
          araddrReg:=io.al.raddr
          arvalidReg:=true.B
        }
      }
    }
  }.elsewhen(ReadRequstState===ar_wait_ready){
    when(io.ar.fire){
      ReadRequstState:=ar_wait_rresp
      arvalidReg:=false.B
      rreadyReg:=true.B 
    }
  }.elsewhen(ReadRequstState===ar_wait_rresp){
    when(io.r.fire){
      ReadRequstState:=ar_idle
      rreadyReg:=false.B

      ram_rdata:=io.r.bits.data
      //不用reg减少一周期
    }
  }
  WaitReadIdle:=(ReadRequstState=/=ar_idle)
  RrespFire:=io.r.fire
  io.ar.valid:=arvalidReg
  io.ar.bits.addr:=araddrReg
  io.ar.bits.prot:=0.U
  io.r.ready:=rreadyReg

  io.al.raddr_ok:= io.ar.fire
  io.dl.rdata_ok:= WaitReadIdle&&RrespFire
  io.dl.rdata:= ram_rdata
  /*
    如果状态位为
    1.等待read事务空闲，此时读相应，已读出来数据
    2.read事务此时空闲
    拉高rdata_ok
  */
//---------------------AXI4Lite AR R Channel---------------------

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
    when(io.s.wen){
      when(WaitReadIdle){
        when(RrespFire){
          WriteRequstState:=wr_wait_ready
          awvalidReg:=true.B
          awaddrReg:=io.s.waddr
          
          wvalidReg:=true.B
          wdataReg:=io.s.wdata
          wstrbReg:=io.s.wstrb
        }
      }.otherwise{
        WriteRequstState:=wr_wait_ready
        awvalidReg:=true.B
        awaddrReg:=io.s.waddr
        
        wvalidReg:=true.B
        wdataReg:=io.s.wdata
        wstrbReg:=io.s.wstrb
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

  io.s.waddr_ok:= io.aw.fire&&io.w.fire
  io.s.wdata_ok:= WaitWriteIdle&&BrespFire
//---------------------------AXI4 Lite---------------------------
}