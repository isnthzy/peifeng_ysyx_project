package Axi

import chisel3._
import chisel3.util._
import CoreConfig.Configs._
import Axi._
import Cache.AxiCacheIO
import CoreConfig.CacheConfig

class Axi4Bridge extends Module with CacheConfig {
  val io=IO(new Bundle {
    val ar=Decoupled(new Axi4AddressBundle())
    val r=Flipped(Decoupled(new Axi4ReadDataBundle()))

    val aw=Decoupled(new Axi4AddressBundle())
    val w=Decoupled(new Axi4WriteDataBundle())
    val b=Flipped(Decoupled(new Axi4WriteResponseBundle()))

    val in=Flipped(new AxiCacheIO())
  })

//---------------------------AXI4 Lite---------------------------
  val WaitWriteIdle=dontTouch(Wire(Bool()))
  //如果write不是idle状态，拉高信号
  val BrespFire=dontTouch(Wire(Bool()))
  //b通道握手，拉高信号
  val LoadStoreFire=io.in.rd.valid&&io.in.wr.valid

  val WaitReadIdle=dontTouch(Wire(Bool()))
//---------------------AXI4Lite AR R Channel---------------------
  val ar_idle :: ar_req_ready  ::Nil = Enum(2)
  val arvalidReg=RegInit(false.B)
  val araddrReg=RegInit(0.U(ADDR_WIDTH.W))
  val artypeReg=RegInit(0.U(3.W))
  val ReadRequstState=RegInit(ar_idle)
  val readCacheLine=artypeReg==="b100".U
  val lastReadRespFire=io.r.fire&&io.r.bits.last.asBool
  WaitReadIdle:=(ReadRequstState=/=ar_idle)
  io.ar.valid:=arvalidReg
  io.ar.bits.addr:=araddrReg
  io.ar.bits.id  :=0.U
  io.ar.bits.burst:=1.U
  io.ar.bits.len :=Mux(readCacheLine,(LINE_WORD_NUM-1).U,0.U)
  io.ar.bits.size:=Mux(readCacheLine,"b10".U,artypeReg)
  io.r.ready:=true.B

  io.in.rd.ready:=(ReadRequstState===ar_idle&&((~WaitWriteIdle)
                                             ||(WaitWriteIdle&&BrespFire)))
  io.in.rret.valid:=io.r.valid
  io.in.rret.bits.data:=io.r.bits.data
  io.in.rret.bits.last:=io.r.bits.last
  io.in.rret.bits.resp:=io.r.bits.resp

  switch(ReadRequstState){
    is(ar_idle){
      when(io.in.rd.valid){
        // when(~LoadStoreFire){ //仲裁:当取指(load)和store同时发生时,阻塞取指(load),先让store通过
          when(WaitWriteIdle){
            when(BrespFire){   //为了防止写后读发生冲突，等待写回复
              ReadRequstState:=ar_req_ready
              araddrReg:=io.in.rd.bits.addr
              artypeReg:=io.in.rd.bits.stype
              arvalidReg:=true.B    
            }
            //如果此时是等待写回复并且已经回复的状态就可以发起ar
          }.otherwise{
            ReadRequstState:=ar_req_ready
            araddrReg:=io.in.rd.bits.addr
            artypeReg:=io.in.rd.bits.stype
            arvalidReg:=true.B
          }
        // }z
      }
    }
    is(ar_req_ready){
      when(io.ar.fire){
        ReadRequstState:=ar_idle
        arvalidReg:=false.B
      }
    }
  }

  val r_idle :: r_wait_respond :: Nil = Enum(2)
  val ReadRespondState=RegInit(r_idle)
  switch(ReadRespondState){
    is(r_idle){
      when(io.r.fire){
        ReadRespondState:=r_wait_respond
      }
    }
    is(r_wait_respond){
      when(io.r.fire&&io.r.bits.last.asBool){
        ReadRespondState:=r_idle
      }
    }
  }
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
  WaitWriteIdle:=(WriteRequstState=/=wr_idle)
  BrespFire:=io.b.fire
  io.aw.valid:=false.B
  io.aw.bits.addr:=io.in.wr.bits.addr
  io.aw.bits.id  :=0.U
  io.aw.bits.burst:=1.U
  io.aw.bits.len :=0.U
  io.aw.bits.size:=io.in.wr.bits.stype
  io.w.valid:=false.B
  io.w.bits.data:=io.in.wr.bits.data
  io.w.bits.strb:=io.in.wr.bits.strb
  io.w.bits.last:=1.U
  io.b.ready:=true.B
  io.in.wr.ready:=io.b.fire&&WriteRequstState===wr_wait_bresp

  val awFire=RegInit(false.B)
  switch(WriteRequstState){
    is(wr_idle){
      when(io.in.wr.valid&& ~WaitReadIdle){
        // io.aw.valid:=true.B
        // io.w.valid :=true.B //NOTE:未来对比这两个设计的时序
        WriteRequstState:=wr_wait_ready
      }
    }
    is(wr_wait_ready){
      io.aw.valid:=true.B
      io.w.valid :=true.B
      when(io.aw.fire&&io.w.fire){
        WriteRequstState:=wr_wait_bresp
      }.elsewhen(io.aw.fire){
        awFire:=true.B
      }
      when(awFire&&io.w.fire){
        WriteRequstState:=wr_wait_bresp
      }
    }
    is(wr_wait_bresp){
      when(io.b.fire){
        WriteRequstState:=wr_idle
      }
    }

  }

//---------------------------AXI4 Lite---------------------------
}