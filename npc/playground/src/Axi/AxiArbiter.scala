package Axi

import chisel3._
import chisel3.util._
import CoreConfig.Configs._
import Cache.{AxiCacheIO,AxiCacheReadIO,AxiCacheWriteIO}


class AxiArbiter(inNum: Int) extends Module {
  val io=IO(new Bundle {
    val in = Vec(inNum,Flipped(new AxiCacheIO())) 
    // NOTE:可拓展的axiarbiter，使用类龙芯接口仲裁,优先级为从下标0开始逐级递减
    // val fs=Flipped(new AxiCacheIO()) //用fs规避if关键字，其实是if
    // val ls=Flipped(new AxiCacheIO())
    val out = new AxiCacheIO()
  })
  val (arb_read_idle ::
       arb_read_resp ::
      Nil) = Enum(2)
  val ArbReadState = RegInit(arb_read_idle)
  val readArb = Module(new Arbiter(new AxiCacheReadIO(),inNum))
  val readChosenIdx = RegInit(0.U(log2Ceil(inNum).W))
  (readArb.io.in zip io.in.map(_.rd)).foreach{case (readArb,in) => readArb <> in}
  for(i <- 0 until inNum){
    when(i.U === readArb.io.chosen && readArb.io.out.fire 
      && ArbReadState === arb_read_idle){
        io.in(i).rd.ready := true.B
      }.otherwise{
        io.in(i).rd.ready := false.B
      }
  }
  readArb.io.out.ready := io.out.rd.ready && ArbReadState === arb_read_idle
  io.out.rd.valid := readArb.io.out.valid && ArbReadState === arb_read_idle
  io.out.rd.bits  := readArb.io.out.bits
  switch(ArbReadState){
    is(arb_read_idle){
      when(readArb.io.out.fire){
        readChosenIdx := readArb.io.chosen
        ArbReadState := arb_read_resp
      }
    }
    is(arb_read_resp){
      when(io.out.rret.fire && io.out.rret.bits.last){
        ArbReadState := arb_read_idle
      }
    }
  }
  io.out.rret.ready := io.in(readChosenIdx).rret.ready
  for(i <- 0 until inNum){
    io.in(i).rret.valid := io.out.rret.valid & (i.U === readChosenIdx)
    io.in(i).rret.bits := (io.out.rret.bits.asUInt 
                        & Fill(io.out.rret.bits.asUInt.getWidth,(i.U === readChosenIdx))).asTypeOf(io.in(0).rret.bits)
  }


  val (arb_write_idle ::
      arb_write_resp ::
      Nil) = Enum(2)
  val ArbWriteState = RegInit(arb_write_idle)
  val writeArb = Module(new Arbiter(new AxiCacheWriteIO(),inNum))
  val writeChosenIdx = RegInit(0.U(log2Ceil(inNum).W))
  (writeArb.io.in zip io.in.map(_.wr)).foreach{case (writeArb,in) => writeArb <> in}
  val chose = dontTouch(Wire(Bool()))
  for(i <- 0 until inNum){
    when(i.U === writeArb.io.chosen && writeArb.io.out.fire 
      && ArbWriteState === arb_write_idle){
        chose := true.B
        io.in(i).wr.ready := true.B
      }.otherwise{
        chose := false.B
        io.in(i).wr.ready := false.B
      }
  }
  
  writeArb.io.out.ready := io.out.wr.ready && ArbWriteState === arb_write_idle
  io.out.wr.valid := writeArb.io.out.valid && ArbWriteState === arb_write_idle
  io.out.wr.bits  := writeArb.io.out.bits
  switch(ArbWriteState){
    is(arb_write_idle){
      when(writeArb.io.out.fire){
        writeChosenIdx := writeArb.io.chosen
        ArbWriteState := arb_write_resp
      }
    }
    is(arb_write_resp){
      when(io.out.wret.fire && io.out.wret.bits.last){
        ArbWriteState := arb_write_idle
      }
    }
  }
  io.out.wret.ready := io.in(writeChosenIdx).wret.ready
  for(i <- 0 until inNum){
    io.in(i).wret.valid := io.out.wret.valid & (i.U === readChosenIdx)
    io.in(i).wret.bits := (io.out.wret.bits.asUInt 
                        & (i.U === writeChosenIdx)).asTypeOf(io.in(0).wret.bits)
  }

}