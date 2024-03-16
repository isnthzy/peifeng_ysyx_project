import chisel3._
import chisel3.util._
import config.Configs._

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
  
  when(ArbiterState===arb_idle){
    when(io.fs.al.ren&&io.ls.al.ren){
      ArbiterState:=arb_wait_ls_arready
      io.out.al.ren:=io.ls.al.ren
      io.out.al.raddr:=io.ls.al.raddr

      fs_ren_reg:=io.fs.al.ren
      fs_raddr_reg:=io.fs.al.raddr
    }.otherwise{
      ArbiterState:=arb_wait_fs_arready
      io.out.al.ren:=io.fs.al.ren
      io.out.al.raddr:=io.fs.al.raddr
    }
  }.elsewhen(ArbiterState===arb_wait_fs_arready){
    when(io.out.al.raddr_ok){
      io.fs.al.raddr_ok:=true.B
      ArbiterState:=arb_wait_fs_rresp
    }
  }.elsewhen(ArbiterState===arb_wait_ls_arready){
    when(io.out.al.raddr_ok){
      io.fs.al.raddr_ok:=true.B
      ArbiterState:=arb_wait_ls_rresp
    }
  }.elsewhen(ArbiterState===arb_wait_fs_rresp){
    when(io.out.dl.rdata_ok){
      ArbiterState:=arb_idle
      io.fs.dl:=io.out.dl
    }
  }.elsewhen(ArbiterState===arb_wait_ls_rresp){
    when(io.out.dl.rdata_ok){
      io.ls.dl:=io.out.dl
      when(fs_ren_reg){
        ArbiterState:=arb_wait_fs_arready
        io.out.al.ren:=fs_ren_reg
        io.out.al.raddr:=fs_raddr_reg

        fs_ren_reg:=false.B
        fs_raddr_reg:=0.U
      }.otherwise{
        ArbiterState:=arb_idle
      }
    }
  }
  
  io.fs.s.waddr_ok:=false.B
  io.fs.s.wdata_ok:=false.B

  io.fs.al.raddr_ok:=WireDefault(false.B)
  io.fs.dl.rdata_ok:=WireDefault(false.B)
  io.fs.dl.rdata:=WireDefault(0.U)
  io.ls.al.raddr_ok:=WireDefault(false.B)
  io.ls.dl.rdata_ok:=WireDefault(false.B)
  io.ls.dl.rdata:=WireDefault(0.U)
  io.out.al.ren:=WireDefault(false.B)
  io.out.al.raddr:=WireDefault(0.U)
  io.out.s<>io.ls.s
}


class AxiBridge extends Bundle{
  val al=new AxiBridgeAddrLoad() //类sram plus的地址读通道
  val dl=new AxiBridgeDataLoad() //类sram plus的读数据响应通道
  val s =new AxiBridgeStore()    //类sram plus的存储通道
}