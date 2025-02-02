package ErXCore

import chisel3._
import chisel3.util._  



class Axi4Slave extends ErXCoreBundle{
  val ar=Flipped(Decoupled(new Axi4AddressBundle()))
  val r=Decoupled(new Axi4ReadDataBundle())
  val aw=Flipped(Decoupled(new Axi4AddressBundle()))
  val w=Flipped(Decoupled(new Axi4WriteDataBundle()))
  val b=Decoupled(new Axi4WriteResponseBundle())
}


class Axi4Master extends ErXCoreBundle{
  val ar=Decoupled(new Axi4AddressBundle())
  val r=Flipped(Decoupled(new Axi4ReadDataBundle()))
  val aw=Decoupled(new Axi4AddressBundle())
  val w=Decoupled(new Axi4WriteDataBundle())
  val b=Flipped(Decoupled(new Axi4WriteResponseBundle()))
}

class Axi4WriteDataBundle extends ErXCoreBundle {
  val data = UInt(XLEN.W)
  val strb = UInt((XLEN/4).W)
  val last = UInt(1.W)
}

class Axi4WriteResponseBundle  extends ErXCoreBundle{
  val resp = UInt(2.W)
  val id   = UInt(4.W)
}

class Axi4AddressBundle extends ErXCoreBundle {
  val addr = UInt(XLEN.W)
  val id   = UInt(4.W)
  val len  = UInt(8.W)
  val size = UInt(3.W)
  val burst= UInt(2.W)
}

class Axi4ReadDataBundle extends ErXCoreBundle {
  val data = UInt(XLEN.W)
  val resp = UInt(2.W)
  val last = UInt(1.W)
  val id   = UInt(4.W)
}

