// package Axi
// import chisel3._
// import chisel3.util._
// import Axi.Axi4LiteSlave
// import CoreConfig.Configs._
// import CoreConfig.DeviceConfig

// class AxiSlaveWapper extends Module with DeviceConfig{
//   val io=IO(new Bundle {
//     val axi=new Axi4LiteSlave()
//     val out=new Bundle {
//       val wea = Output(Bool())
//       val addra = Output(UInt(ADDR_WIDTH.W))
//       val douta = Output(UInt(DATA_WIDTH.W))
//       val addrb = Input(UInt(ADDR_WIDTH.W))
//       val dinb = Input(UInt(DATA_WIDTH.W))
//     }
//   })

//   val timer=RegInit(0.U(64.W))
//   timer:=timer+1.U

//   val r_idle :: r_resp :: Nil = Enum(2)
//   val readState=RegInit(r_idle)
//   val addrResp=RegInit(0.U(ADDR_WIDTH.W))
//   io.axi.ar.ready:=true.B
//   io.axi.r.valid :=false.B
//   io.axi.r.bits:=0.U.asTypeOf(io.axi.r.bits)

//   switch(readState){
//     is(r_idle){
//       when(io.axi.ar.fire){
//         readState:=r_resp
//         addrResp:=io.axi.ar.bits.addr
//       }
//     }
//     is(r_resp){
//       io.axi.r.valid:=true.B
//       when(io.axi.r.fire){
//         when(addrResp===RTC_ADDR){
//           io.axi.r.bits.data:=timer(31,0)
//         }.otherwise{
//           io.axi.r.bits.data:=timer(63,32)
//         }
//         readState:=r_idle
//       }
//     }
//   }

//   io.axi.aw.ready:=false.B
//   io.axi.w.ready:=false.B
//   io.axi.b.valid:=false.B
//   io.axi.b.bits:=0.U.asTypeOf(io.axi.b.bits)
// }
