import chisel3._
import chisel3.util._
import config.Configs._

class AxiArbiter extends Module {
  val io=IO(new Bundle {
    val fs=Flipped(new AxiBridgeOut()) //用fs规避if关键字，其实是if
    val ls=Flipped(new AxiBridgeOut())
    val out=new AxiBridgeOut()
  })
  val arb_idle :: arb_wait_ls :: Nil = Enum(2)
  val ArbiterState=RegInit(arb_idle)
  //两层状态机仲裁fetch和load,当arb_idle时，正常fetch
  //当fetch和load都请求时，先让load执行完在给fetch

  when(ArbiterState===arb_idle){
    when(io.fs.al.ren&&io.ls.al.ren){
      ArbiterState:=arb_wait_ls
      io.out.al<>io.ls.al
    }.otherwise{
      io.out.al<>io.fs.al
      io.out.dl<>io.fs.dl
    }
  }.elsewhen(ArbiterState===arb_wait_ls){
    when(io.ls.dl.rdata_ok){
      ArbiterState:=arb_idle
      io.out.dl<>io.fs.dl
    }
  }
  
  io.out.s<>io.ls.s
}


class AxiBridgeOut extends Bundle{
  val al=new AxiBridgeAddrLoad() //类sram plus的地址读通道
  val dl=new AxiBridgeDataLoad() //类sram plus的读数据响应通道
  val s =new AxiBridgeStore()    //类sram plus的存储通道
}