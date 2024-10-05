// package Axi

// import chisel3._
// import chisel3.util._  
// import CoreConfig.Configs._



// class Axi4LiteSlave extends Bundle{
//   val ar=Flipped(Decoupled(new AxiAddressBundle()))
//   val r=Decoupled(new AxiReadDataBundle())
//   val aw=Flipped(Decoupled(new AxiAddressBundle()))
//   val w=Flipped(Decoupled(new AxiWriteDataBundle()))
//   val b=Decoupled(new AxiWriteResponseBundle())
// }


// class Axi4LiteMaster extends Bundle{
//   val ar=Decoupled(new AxiAddressBundle())
//   val r=Flipped(Decoupled(new AxiReadDataBundle()))
//   val aw=Decoupled(new AxiAddressBundle())
//   val w=Decoupled(new AxiWriteDataBundle())
//   val b=Flipped(Decoupled(new AxiWriteResponseBundle()))
// }

// class AxiWriteDataBundle extends Bundle {
//   val data = UInt(DATA_WIDTH.W)
//   val strb = UInt((DATA_WIDTH/4).W)
// }

// class AxiWriteResponseBundle  extends Bundle{
//   val resp = UInt(2.W)
// }

// class AxiAddressBundle extends Bundle {
//   val addr = UInt(ADDR_WIDTH.W)
//   val prot = UInt(3.W)
// }

// class AxiReadDataBundle extends Bundle {
//   val data = UInt(DATA_WIDTH.W)
//   val resp = UInt(2.W)
// }

