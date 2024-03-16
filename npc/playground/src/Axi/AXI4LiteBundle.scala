import chisel3._
import chisel3.util._  
import config.Configs._



class Axi4LiteSlave extends Bundle{
  val ar=Flipped(Decoupled(new AxiAddressBundle()))
  val r=Decoupled(new AxiReadDataBundle())
  val aw=Flipped(Decoupled(new AxiAddressBundle()))
  val w=Flipped(Decoupled(new AxiWriteDataBundle()))
  val b=Decoupled(new AxiWriteResponseBundle())
}


class Axi4LiteMaster extends Bundle{
  val ar=Decoupled(new AxiAddressBundle())
  val r=Flipped(Decoupled(new AxiReadDataBundle()))
  val aw=Decoupled(new AxiAddressBundle())
  val w=Decoupled(new AxiWriteDataBundle())
  val b=Flipped(Decoupled(new AxiWriteResponseBundle()))
}

class AxiWriteDataBundle extends Bundle {
  val data = UInt(DATA_WIDTH.W)
  val strb = UInt((DATA_WIDTH/4).W)
}

class AxiWriteResponseBundle  extends Bundle{
  val resp = UInt(2.W)
}

class AxiAddressBundle extends Bundle {
  val addr = UInt(ADDR_WIDTH.W)
  val prot = UInt(3.W)
}

class AxiReadDataBundle extends Bundle {
  val data = UInt(DATA_WIDTH.W)
  val resp = UInt(2.W)
}

/*-----------------------------AxiBridgeBundle-----------------------------*/
class AxiBridgeAddrLoad extends Bundle{ //类sram plus的地址读通道
  val ren=Output(Bool())
  val raddr=Output(UInt(ADDR_WIDTH.W))
  val raddr_ok=Input(Bool())
} //缩写为al接口

class AxiBridgeDataLoad extends Bundle{ //类sram plus的读数据响应通道
  val rdata=Input(UInt(DATA_WIDTH.W))
  val rdata_ok=Input(Bool())
} //缩写为dl接口

class AxiBridgeStore extends Bundle{ //类sram plus的存储通道
  val wen=Output(Bool())
  val waddr=Output(UInt(ADDR_WIDTH.W))
  val waddr_ok=Input(Bool())
  val wstrb=Output(UInt((DATA_WIDTH/8).W))
  val wdata=Output(UInt(DATA_WIDTH.W))
  val wdata_ok=Input(Bool())
} //缩写为s接口

/*-----------------------------AxiBridgeBundle-----------------------------*/