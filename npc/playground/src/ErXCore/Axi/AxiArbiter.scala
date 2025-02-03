package ErXCore

import chisel3._
import chisel3.util._
// import CoreConfig.Configs._
// import Cache.{AxiCacheIO,AxiCacheReadIO,AxiCacheWriteIO}


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

  //NOTE:写事务优化掉回复
  val (arb_write_idle ::
      arb_write_resp ::
      Nil) = Enum(2)
  val ArbWriteState = RegInit(arb_write_idle)
  val writeArb = Module(new Arbiter(new AxiCacheWriteIO(),inNum))
  val writeChosenIdx = RegInit(0.U(log2Ceil(inNum).W))
  (writeArb.io.in zip io.in.map(_.wr)).foreach{case (writeArb,in) => writeArb <> in}
  dontTouch(io.in(0).wr.ready)
  for(i <- 0 until inNum){
    when(i.U === writeArb.io.chosen && writeArb.io.out.fire){
        io.in(i).wr.ready := true.B
      }.otherwise{
        io.in(i).wr.ready := false.B
      }
  }
  
  writeArb.io.out.ready := io.out.wr.ready 
  io.out.wr.valid := writeArb.io.out.valid 
  io.out.wr.bits  := writeArb.io.out.bits
  switch(ArbWriteState){
    is(arb_write_idle){
      when(writeArb.io.out.valid){
        writeChosenIdx := writeArb.io.chosen
        ArbWriteState := arb_write_resp
      }
    }
    is(arb_write_resp){
      when(io.out.wr.fire){
        ArbWriteState := arb_write_idle
      }
    }
  }
}