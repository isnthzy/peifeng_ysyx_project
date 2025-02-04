package ErXCore
import ErXCore.Cache._
import chisel3._
import chisel3.util._

class Frontend extends ErXCoreModule {
  val io = IO(new Bundle {
    val from_bck = Input(new FrontFromBack)
    val imem = new AxiCacheIO()
    val out = Vec(DecodeWidth,Decoupled(new InstIO))
  })
  val PreFetch  = Module(new PfStage())
  val InstFetch = Module(new IfStage())
  val InstBuff  = Module(new InstBuff())
//
  val ICache=Module(new ICache())

  ICache.io.valid :=PreFetch.io.al.req||PreFetch.io.fenceI
  ICache.io.fenceI:=PreFetch.io.fenceI
  ICache.io.tag   :=PreFetch.io.al.addr(31,31-TAG_WIDTH+1)
  ICache.io.index :=PreFetch.io.al.addr(31-TAG_WIDTH,OFFSET_WIDTH)
  ICache.io.offset:=PreFetch.io.al.addr(OFFSET_WIDTH-1,0)
  PreFetch.io.al.addrOk :=ICache.io.addrRp
  InstFetch.io.dl.dataOk:=ICache.io.dataRp
  InstFetch.io.dl.data  :=ICache.io.rdata
  io.imem <> ICache.io.out
// PreIF begin
  PreFetch.io.from_bck:= io.from_bck
// if begin
  FrontendConnect(PreFetch.io.to_if,InstFetch.io.in)
  InstFetch.io.from_bck.flush := io.from_bck.flush
// inst buff
  InstBuff.io.in := InstFetch.io.to_id
  InstBuff.io.from_bck.flush := io.from_bck.flush
  io.out <> InstBuff.io.out
}

object FrontendConnect {
  def apply[T <: Data](out: DecoupledIO[T], in: DecoupledIO[T]) = {
    val arch = "pipeline"
    if (arch == "pipeline") { 
      out.ready:=in.ready
      in.valid:=out.valid
      in.bits <> RegEnable(out.bits, out.fire) 
    }
  }
}

