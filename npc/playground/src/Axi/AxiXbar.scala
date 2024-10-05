package Axi
import chisel3._
import chisel3.util._
import CoreConfig.Configs._

/*NOTE: 
  Xbar分为两种：
  A2X: a to x 负责将一个请求根据目标地址转换到x个地址
  X2A: x to a 负责将x个请求仲裁为一个请求
      （优先级由0->n依次递减）（使用chisel自带Arbiter）

 */

//Xbar只判断一次地址行么？不行，Xbar也要走一遍axi的状态机转换过程，因为xbar要持续追踪r，b，w的响应
//Xbar的作用是转发，因此Xbar只需要追踪开始和结束状态
class AxiXbarA2X(addressSpace: List[(Long, Long, Boolean)]) extends Module{
  val io=IO(new Bundle{
    val a=Flipped(new Axi4Master())
    val x=Vec(addressSpace.length, new Axi4Master())
  })
  val state_idle :: state_wait_fire :: Nil = Enum(2)
  val ReadRequstState=RegInit(state_idle)
  val WriteRequstState=RegInit(state_idle)

  val raddr=io.a.ar.bits.addr
  val XreadSelVec=VecInit(
    addressSpace.map(list=>
      (raddr>=list._1.U&&raddr<=(list._1+list._2).U)
    )
  )
  val XreadHitIdx=OHToUInt(XreadSelVec)
  val Xread=io.x(XreadHitIdx)
  val arValidReg=RegInit(false.B)
  val XreadRespIdx=RegInit(0.U(log2Ceil(addressSpace.length).W))
  val XreadResp   =io.x(XreadRespIdx)
  val readStateIdle=ReadRequstState===state_idle
  val readStateResp=ReadRequstState===state_wait_fire
  for(i<-0 until addressSpace.length){
    var readAddrHit=i.U===XreadHitIdx
    var readRespHit=i.U===XreadRespIdx
    io.x(i).ar.valid:=readAddrHit&&io.a.ar.valid&&readStateIdle
    io.x(i).ar.bits :=io.a.ar.bits
    io.x(i).r.ready :=readRespHit&&io.a.r.ready&&readStateResp
  }
  io.a.ar.ready:=Xread.ar.ready
  io.a.r.valid :=XreadResp.r.valid
  io.a.r.bits  :=XreadResp.r.bits

  switch(ReadRequstState){
    is(state_idle){      
      when(Xread.ar.fire){
        ReadRequstState:=state_wait_fire
        XreadRespIdx:=XreadHitIdx
      }
    }
    is(state_wait_fire){
      when(XreadResp.r.fire){
        ReadRequstState:=state_idle
        XreadRespIdx:=0.U
      }
    }
  }


  val waddr=io.a.aw.bits.addr
  val XwriteSelVec=VecInit(
    addressSpace.map(list=>
      (waddr>=list._1.U&&waddr<=(list._1+list._2).U)
    )
  )
  val XwriteHitIdx=OHToUInt(XwriteSelVec)
  val Xwrite=io.x(XwriteHitIdx)
  val XwriteRespIdx=RegInit(0.U(log2Ceil(addressSpace.length).W))
  val XwriteResp   =io.x(XwriteRespIdx)
  val writeStateIdle=WriteRequstState===state_idle
  val writeStateResp=WriteRequstState===state_wait_fire
  for(i<-0 until addressSpace.length){
    var writeAddrHit=i.U===XwriteHitIdx
    var writeRespHit=i.U===XwriteRespIdx
    io.x(i).aw.valid:=writeAddrHit&&io.a.aw.valid&&writeStateIdle
    io.x(i).aw.bits :=io.a.aw.bits
    io.x(i).w.valid :=writeAddrHit&&io.a.w.valid //NOTE:aw和w一起给的
    io.x(i).w.bits  :=io.a.w.bits
    io.x(i).b.ready :=writeRespHit&&io.a.b.ready&&writeStateResp
  }
  io.a.aw.ready:=Xwrite.aw.ready
  io.a.w.ready :=XwriteResp.w.ready
  io.a.b.valid :=XwriteResp.b.valid
  io.a.b.bits  :=XwriteResp.b.bits

  switch(WriteRequstState){
    is(state_idle){      
      when(Xwrite.aw.fire){
        WriteRequstState:=state_wait_fire
        XwriteRespIdx:=XwriteHitIdx
      }
    }
    is(state_wait_fire){
      when(XwriteResp.b.fire){
        WriteRequstState:=state_idle
        XwriteRespIdx:=0.U
      }
    }
  }
}


class AxiXbarX2A extends Module{

}