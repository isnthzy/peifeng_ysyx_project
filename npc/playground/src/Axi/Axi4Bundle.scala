package Axi

import chisel3._
import chisel3.util._  
import CoreConfig.Configs._



class Axi4Slave extends Bundle{
  val ar=Flipped(Decoupled(new Axi4AddressBundle()))
  val r=Decoupled(new Axi4ReadDataBundle())
  val aw=Flipped(Decoupled(new Axi4AddressBundle()))
  val w=Flipped(Decoupled(new Axi4WriteDataBundle()))
  val b=Decoupled(new Axi4WriteResponseBundle())
}


class Axi4Master extends Bundle{
  val ar=Decoupled(new Axi4AddressBundle())
  val r=Flipped(Decoupled(new Axi4ReadDataBundle()))
  val aw=Decoupled(new Axi4AddressBundle())
  val w=Decoupled(new Axi4WriteDataBundle())
  val b=Flipped(Decoupled(new Axi4WriteResponseBundle()))
}

class Axi4WriteDataBundle extends Bundle {
  val data = UInt(DATA_WIDTH.W)
  val strb = UInt((DATA_WIDTH/4).W)
  val last = UInt(1.W)
}

class Axi4WriteResponseBundle  extends Bundle{
  val resp = UInt(2.W)
  val id   = UInt(4.W)
}

class Axi4AddressBundle extends Bundle {
  val addr = UInt(ADDR_WIDTH.W)
  val id   = UInt(4.W)
  val len  = UInt(8.W)
  val size = UInt(3.W)
  val burst= UInt(2.W)
}

class Axi4ReadDataBundle extends Bundle {
  val data = UInt(DATA_WIDTH.W)
  val resp = UInt(2.W)
  val last = UInt(1.W)
  val id   = UInt(4.W)
}

/*-----------------------------AxiBridgeBundle-----------------------------*/
class AxiBridgeAddrLoad extends Bundle{ //类sram plus的地址读通道
  val ren=Output(Bool())
  val raddr=Output(UInt(ADDR_WIDTH.W))
  val rsize=Output(UInt(3.W))
  val raddr_ok=Input(Bool())
} //缩写为al接口

class AxiBridgeDataLoad extends Bundle{ //类sram plus的读数据响应通道
  val rdata=Input(UInt(DATA_WIDTH.W))
  val rdata_ok=Input(Bool())
} //缩写为dl接口

class AxiBridgeStore extends Bundle{ //类sram plus的存储通道
  val wen=Output(Bool())
  val wsize=Output(UInt(3.W))
  val waddr=Output(UInt(ADDR_WIDTH.W))
  val waddr_ok=Input(Bool())
  val wstrb=Output(UInt((DATA_WIDTH/8).W))
  val wdata=Output(UInt(DATA_WIDTH.W))
  val wdata_ok=Input(Bool())
} //缩写为s接口

/*-----------------------------AxiBridgeBundle-----------------------------*/

