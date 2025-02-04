package ErXCore.Cache
import ErXCore._
import chisel3._
import chisel3.util._


//NOTE:DCache未实现
class DCache extends ErXCoreModule{
  val io=IO(new Bundle{
    val ds = Flipped(new SimpleMemIO)
    val dl = Flipped(new SimpleMemIO)
    val out= new AxiCacheIO()
  })
  io.out.rret.ready:=true.B

  io.out.rd.valid     :=io.dl.req.valid
  io.out.rd.bits.stype:=io.dl.req.bits.size
  io.out.rd.bits.addr :=io.dl.req.bits.addr
  io.dl.req.ready     :=io.out.rd.fire
  io.dl.resp.bits.data:=io.out.rret.bits.data
  io.dl.resp.valid    :=io.out.rret.fire

  io.out.wr.valid     := io.ds.req.valid
  io.out.wr.bits.stype:= io.ds.req.bits.size
  io.out.wr.bits.addr := io.ds.req.bits.addr
  io.out.wr.bits.data := io.ds.req.bits.wdata
  io.out.wr.bits.strb := io.ds.req.bits.wmask
  dontTouch(io.out.wr.ready)
  io.ds.req.ready  := io.out.wr.fire
  io.ds.resp.valid := io.out.wr.fire
}