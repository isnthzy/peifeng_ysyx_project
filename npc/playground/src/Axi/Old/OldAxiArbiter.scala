package Axi.Old

import chisel3._
import chisel3.util._
import CoreConfig.Configs._

class AxiArbiter extends Module {
  val io=IO(new Bundle {
    val fs=Flipped(new AxiBridge()) //用fs规避if关键字，其实是if
    val ls=Flipped(new AxiBridge())
    val out=new AxiBridge()
  })
  val (arb_idle ::
      arb_wait_fs_arready ::
      arb_wait_ls_arready ::
      arb_wait_fs_rresp ::
      arb_wait_ls_rresp ::
      Nil) = Enum(5)
  val ArbiterState=RegInit(arb_idle)
  //两层状态机仲裁fetch和load,当arb_idle时，正常fetch
  //当fetch和load都请求时，先让load执行完在给fetch

  val fs_ren_reg=RegInit(false.B)
  val fs_raddr_reg=RegInit(0.U(32.W))
  val fs_rsize_reg=RegInit(2.U(3.W))

  val fs_raddr_ok=WireDefault(false.B)
  val fs_rdata_ok=WireDefault(false.B)
  val fs_rdata=WireDefault(0.U)
  val ls_raddr_ok=WireDefault(false.B)
  val ls_rdata_ok=WireDefault(false.B)
  val ls_rdata=WireDefault(0.U)
  val out_ren_reg=RegInit(false.B)
  val out_raddr_reg=RegInit(0.U(ADDR_WIDTH.W))
  val out_rsize_reg=RegInit(2.U(3.W))
  
  when(ArbiterState===arb_idle){ //000
    when(io.fs.al.ren&&io.ls.al.ren){
      ArbiterState:=arb_wait_ls_arready
      out_ren_reg:=io.ls.al.ren
      out_raddr_reg:=io.ls.al.raddr
      out_rsize_reg:=io.ls.al.rsize

      fs_ren_reg:=io.fs.al.ren
      fs_raddr_reg:=io.fs.al.raddr
      fs_rsize_reg:=io.fs.al.rsize
    }.elsewhen(io.fs.al.ren){
      ArbiterState:=arb_wait_fs_arready
      out_ren_reg:=io.fs.al.ren
      out_raddr_reg:=io.fs.al.raddr
      out_rsize_reg:=io.fs.al.rsize
    }.elsewhen(io.ls.al.ren){
      ArbiterState:=arb_wait_ls_arready
      out_ren_reg:=io.ls.al.ren
      out_raddr_reg:=io.ls.al.raddr
      out_rsize_reg:=io.ls.al.rsize
    }
  }.elsewhen(ArbiterState===arb_wait_fs_arready){ //001
    when(io.out.al.raddr_ok){
      fs_raddr_ok:=true.B
      ArbiterState:=arb_wait_fs_rresp

      out_ren_reg:=false.B
      out_raddr_reg:=0.U
    }
  }.elsewhen(ArbiterState===arb_wait_ls_arready){ //010
    when(io.out.al.raddr_ok){
      ls_raddr_ok:=true.B
      ArbiterState:=arb_wait_ls_rresp
      
      out_ren_reg:=false.B
      out_raddr_reg:=0.U
    }
  }.elsewhen(ArbiterState===arb_wait_fs_rresp){  //011
    when(io.out.dl.rdata_ok){
      ArbiterState:=arb_idle

      fs_rdata_ok:=io.out.dl.rdata_ok
      fs_rdata:=io.out.dl.rdata
    }
  }.elsewhen(ArbiterState===arb_wait_ls_rresp){  //100
    when(io.out.dl.rdata_ok){
      ls_rdata_ok:=io.out.dl.rdata_ok
      ls_rdata:=io.out.dl.rdata
      when(fs_ren_reg){
        ArbiterState:=arb_wait_fs_arready
        out_ren_reg:=fs_ren_reg
        out_raddr_reg:=fs_raddr_reg
        out_rsize_reg:=fs_rsize_reg
        fs_ren_reg:=false.B
        fs_raddr_reg:=0.U
      }.otherwise{
        ArbiterState:=arb_idle
      }
    }
  }
  
  io.fs.s.waddr_ok:=false.B
  io.fs.s.wdata_ok:=false.B
  io.fs.al.raddr_ok:=fs_raddr_ok
  io.fs.dl.rdata_ok:=fs_rdata_ok
  io.fs.dl.rdata:=fs_rdata
  io.ls.al.raddr_ok:=ls_raddr_ok
  io.ls.dl.rdata_ok:=ls_rdata_ok
  io.ls.dl.rdata:=ls_rdata
  io.out.al.ren:=out_ren_reg
  io.out.al.raddr:=out_raddr_reg
  io.out.al.rsize:=out_rsize_reg
  io.out.s<>io.ls.s
}


// class AxiBridge extends Bundle{
//   val al=new AxiBridgeAddrLoad() //类sram plus的地址读通道
//   val dl=new AxiBridgeDataLoad() //类sram plus的读数据响应通道
//   val s =new AxiBridgeStore()    //类sram plus的存储通道
// }