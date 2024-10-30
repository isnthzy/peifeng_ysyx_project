package Axi.`new`

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
  io.in.map(_.rret.bits := 0.U.asTypeOf(io.in(0).rret.bits))
  io.in.map(_.rd.ready := false.B)
  io.out.rd.valid := false.B
  io.out.rd.bits  := 0.U.asTypeOf(io.out.rd.bits)
  readArb.io.out.ready := false.B
  switch(ArbReadState){
    is(arb_read_idle){
      readArb.io.out.ready := io.out.rd.ready
      io.out.rd.valid := readArb.io.out.valid
      io.out.rd.bits  := readArb.io.out.bits
      when(readArb.io.out.fire){
        readChosenIdx := readArb.io.chosen
        io.in(readArb.io.chosen).rd.ready := true.B
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
                        & (i.U === readChosenIdx)).asTypeOf(io.in(0).rret.bits)
  }


  val (arb_write_idle ::
      arb_write_resp ::
      Nil) = Enum(2)
  val ArbWriteState = RegInit(arb_write_idle)
  val writeArb = Module(new Arbiter(new AxiCacheWriteIO(),inNum))
  val writeChosenIdx = RegInit(0.U(log2Ceil(inNum).W))
  (writeArb.io.in zip io.in.map(_.wr)).foreach{case (writeArb,in) => writeArb <> in}
  io.in.map(_.wret.bits := 0.U.asTypeOf(io.in(0).wret.bits))
  io.in.map(_.wr.ready := false.B)
  switch(ArbWriteState){
    is(arb_write_idle){
      writeArb.io.out.ready := io.out.wr.ready
      io.out.wr.valid := writeArb.io.out.valid
      io.out.wr.bits  := writeArb.io.out.bits
      when(writeArb.io.out.fire){
        writeChosenIdx := writeArb.io.chosen
        io.in(writeArb.io.chosen).wr.ready := true.B
        ArbReadState := arb_write_resp
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
  // val ArbiterState=RegInit(arb_idle)
  // //两层状态机仲裁fetch和load,当arb_idle时，正常fetch
  // //当fetch和load都请求时，先让load执行完在给fetch

  // val fs_ren_reg=RegInit(false.B)
  // val fs_raddr_reg=RegInit(0.U(32.W))
  // val fs_rsize_reg=RegInit(2.U(3.W))

  // val fs_raddr_ok=WireDefault(false.B)
  // val fs_rdata_ok=WireDefault(false.B)
  // val fs_rdata=WireDefault(0.U)
  // val ls_raddr_ok=WireDefault(false.B)
  // val ls_rdata_ok=WireDefault(false.B)
  // val ls_rdata=WireDefault(0.U)
  // val out_ren_reg=RegInit(false.B)
  // val out_raddr_reg=RegInit(0.U(ADDR_WIDTH.W))
  // val out_rsize_reg=RegInit(2.U(3.W))
  
  // when(ArbiterState===arb_idle){ //000
  //   when(io.fs.al.ren&&io.ls.al.ren){
  //     ArbiterState:=arb_wait_ls_arready
  //     out_ren_reg:=io.ls.al.ren
  //     out_raddr_reg:=io.ls.al.raddr
  //     out_rsize_reg:=io.ls.al.rsize

  //     fs_ren_reg:=io.fs.al.ren
  //     fs_raddr_reg:=io.fs.al.raddr
  //     fs_rsize_reg:=io.fs.al.rsize
  //   }.elsewhen(io.fs.al.ren){
  //     ArbiterState:=arb_wait_fs_arready
  //     out_ren_reg:=io.fs.al.ren
  //     out_raddr_reg:=io.fs.al.raddr
  //     out_rsize_reg:=io.fs.al.rsize
  //   }.elsewhen(io.ls.al.ren){
  //     ArbiterState:=arb_wait_ls_arready
  //     out_ren_reg:=io.ls.al.ren
  //     out_raddr_reg:=io.ls.al.raddr
  //     out_rsize_reg:=io.ls.al.rsize
  //   }
  // }.elsewhen(ArbiterState===arb_wait_fs_arready){ //001
  //   when(io.out.al.raddr_ok){
  //     fs_raddr_ok:=true.B
  //     ArbiterState:=arb_wait_fs_rresp

  //     out_ren_reg:=false.B
  //     out_raddr_reg:=0.U
  //   }
  // }.elsewhen(ArbiterState===arb_wait_ls_arready){ //010
  //   when(io.out.al.raddr_ok){
  //     ls_raddr_ok:=true.B
  //     ArbiterState:=arb_wait_ls_rresp
      
  //     out_ren_reg:=false.B
  //     out_raddr_reg:=0.U
  //   }
  // }.elsewhen(ArbiterState===arb_wait_fs_rresp){  //011
  //   when(io.out.dl.rdata_ok){
  //     ArbiterState:=arb_idle

  //     fs_rdata_ok:=io.out.dl.rdata_ok
  //     fs_rdata:=io.out.dl.rdata
  //   }
  // }.elsewhen(ArbiterState===arb_wait_ls_rresp){  //100
  //   when(io.out.dl.rdata_ok){
  //     ls_rdata_ok:=io.out.dl.rdata_ok
  //     ls_rdata:=io.out.dl.rdata
  //     when(fs_ren_reg){
  //       ArbiterState:=arb_wait_fs_arready
  //       out_ren_reg:=fs_ren_reg
  //       out_raddr_reg:=fs_raddr_reg
  //       out_rsize_reg:=fs_rsize_reg
  //       fs_ren_reg:=false.B
  //       fs_raddr_reg:=0.U
  //     }.otherwise{
  //       ArbiterState:=arb_idle
  //     }
  //   }
  // }
  
  // io.fs.s.waddr_ok:=false.B
  // io.fs.s.wdata_ok:=false.B
  // io.fs.al.raddr_ok:=fs_raddr_ok
  // io.fs.dl.rdata_ok:=fs_rdata_ok
  // io.fs.dl.rdata:=fs_rdata
  // io.ls.al.raddr_ok:=ls_raddr_ok
  // io.ls.dl.rdata_ok:=ls_rdata_ok
  // io.ls.dl.rdata:=ls_rdata
  // io.out.al.ren:=out_ren_reg
  // io.out.al.raddr:=out_raddr_reg
  // io.out.al.rsize:=out_rsize_reg
  // io.out.s<>io.ls.s
}