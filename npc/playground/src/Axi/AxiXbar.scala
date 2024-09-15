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
class AxiXbarA2X(addressSpace: List[(UInt, UInt, Boolean)]) extends Module{
  val io=IO(new Bundle{
    val a=Flipped(new Axi4LiteMaster())
    val x=Vec(addressSpace.length, new Axi4LiteMaster())
  })
  val state_idle :: state_wait_fire :: Nil = Enum(2)
  val ReadRequstState=RegInit(state_idle)
  val WriteRequstState=RegInit(state_idle)

  val raddr=io.a.ar.bits.addr
  val XreadSelVec=VecInit(
    addressSpace.map(list=>
      (raddr>=list._1&&raddr<=list._1+list._2)
    )
  )
  val XreadHitIdx=OHToUInt(XreadSelVec)
  val Xread=io.x(XreadHitIdx)
  val arValidReg=RegInit(false.B)
  val XreadRespIdx=RegInit(0.U(addressSpace.length.W))
  val XreadResp   =io.x(XreadRespIdx)

  switch(ReadRequstState){
    is(state_idle){
      Xread.ar<>io.a.ar
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
  io.a.r<>XreadResp.r


  val waddr=io.a.aw.bits.addr
  val XwriteSelVec=VecInit(
    addressSpace.map(list=>
      (waddr>=list._1&&waddr<=list._1+list._2)
    )
  )
  val XwriteHitIdx=OHToUInt(XwriteSelVec)
  val Xwrite=io.x(XwriteHitIdx)
  val XwriteRespIdx=RegInit(0.U(addressSpace.length.W))
  val XwriteResp   =io.x(XwriteRespIdx)

  switch(WriteRequstState){
    is(state_idle){
      Xwrite.aw<>io.a.aw
      when(Xwrite.aw.fire){
        WriteRequstState:=state_wait_fire
        XwriteRespIdx:=XwriteHitIdx
      }
    }
    is(state_wait_fire){
      when(XwriteResp.b.fire){
        WriteRequstState:=state_idle
        XreadRespIdx:=0.U
      }
    }
  }
  XwriteResp.w<>io.a.w
  XwriteResp.b<>io.a.b

}


class AxiXbarX2A extends Module{

}