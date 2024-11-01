package Cache
import chisel3._
import chisel3.util._
import CoreConfig.Configs._
import Axi.{AxiBridgeAddrLoad,AxiBridgeStore,AxiBridgeDataLoad}
//NOTE:DCache未实现
class DCache extends Module{
  val io=IO(new Bundle{
    val in=Flipped(new Bundle {
      val al=new AxiBridgeAddrLoad()
      val dl=new AxiBridgeDataLoad()
      val s =new AxiBridgeStore()
    })
    val out=new AxiCacheIO()
  })
  io.out.rret.ready:=true.B
  io.out.wret.ready:=true.B

  io.out.rd.valid:=io.in.al.ren
  io.out.rd.bits.stype:=io.in.al.rsize
  io.out.rd.bits.addr :=io.in.al.raddr
  io.in.al.raddr_ok:=io.out.rd.fire
  io.in.dl.rdata:=io.out.rret.bits.data
  io.in.dl.rdata_ok:=io.out.rret.fire

  io.out.wr.valid:=io.in.s.wen
  io.out.wr.bits.stype:=io.in.s.wsize
  io.out.wr.bits.addr:=io.in.s.waddr
  io.out.wr.bits.data:=io.in.s.wdata
  io.out.wr.bits.strb:=io.in.s.wstrb
  io.in.s.waddr_ok:=io.out.wr.fire
  io.in.s.wdata_ok:=io.out.rret.fire
}