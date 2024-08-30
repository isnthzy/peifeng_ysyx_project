package Bundles
import chisel3._
import chisel3.util._
import config.Configs._

class Pf2IfBusBundle extends Bundle{
  val pc=UInt(ADDR_WIDTH.W)
}

class If2IdBusBundle extends Bundle{
  val pc=UInt(ADDR_WIDTH.W)
  val inst=UInt(DATA_WIDTH.W)
}

class Id2ExBusBundle extends Bundle{

}

class Ex2LsBusBundle extends Bundle{

}

class Ls2WbBusBundle extends Bundle{

}
