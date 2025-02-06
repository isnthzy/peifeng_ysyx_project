package ErXCore

import chisel3._
import chisel3.util._
import DecodeSignal._

class LSU extends ErXCoreModule{
  val io = IO(new Bundle{
    val req = Flipped(DecoupledIO(new LsuReqBundle))
    val resp = DecoupledIO(new LsuRespBundle)

    val DMemStore = new SimpleMemIO
    val DMemLoad  = new SimpleMemIO
  })
  
  val isMem = isLoadStore(io.req.bits.lsType)
  val isLoad = isLoadInst(io.req.bits.lsType)
  val isStore = isStoreInst(io.req.bits.lsType)

  val memMisalignedAddr=io.req.bits.addr
  val memStoreSrc=io.req.bits.wdata
  val memByteSize=(io.req.bits.lsType===SDEF(LD_LB)||io.req.bits.lsType===SDEF(LD_LBU)
                 ||io.req.bits.lsType===SDEF(ST_SB))
  val memHalfSize=(io.req.bits.lsType===SDEF(LD_LH)||io.req.bits.lsType===SDEF(LD_LHU)
                 ||io.req.bits.lsType===SDEF(ST_SH))
  val memSize=Cat(memHalfSize,memByteSize)
  val memSbSel=Cat(
    memMisalignedAddr(1,0)===3.U,
    memMisalignedAddr(1,0)===2.U,
    memMisalignedAddr(1,0)===1.U,
    memMisalignedAddr(1,0)===0.U
  )
  val memShSel=Cat(
    memMisalignedAddr(1,0)===2.U,
    memMisalignedAddr(1,0)===2.U,
    memMisalignedAddr(1,0)===0.U,
    memMisalignedAddr(1,0)===0.U
  )
  val memSbCont=Cat(
    Fill(8,memSbSel(3))&memStoreSrc(7,0),
    Fill(8,memSbSel(2))&memStoreSrc(7,0),
    Fill(8,memSbSel(1))&memStoreSrc(7,0),
    Fill(8,memSbSel(0))&memStoreSrc(7,0)
  )
  val memShCont=Cat(
    Fill(16,memShSel(3))&memStoreSrc(15,0),
    Fill(16,memShSel(0))&memStoreSrc(15,0)
  )
  val memDataSize=(
    Fill(3,memSize(0))&0.U(3.W)
   |Fill(3,memSize(1))&1.U(3.W)
   |Fill(3, !memSize) &2.U(3.W)
  )
  val memWstrb=(
    Fill(4,memSize(0))&memSbSel
   |Fill(4,memSize(1))&memShSel
   |Fill(4, !memSize) &15.U(4.W)
  )
  val memWdata=(
    Fill(XLEN,memSize(0))&memSbCont
   |Fill(XLEN,memSize(1))&memShCont
   |Fill(XLEN, !memSize )&memStoreSrc
  )

  val s_idle :: s_wait_write :: s_wait_read :: Nil = Enum(3)
  val LsuState = RegInit(s_idle)

  io.req.ready := LsuState === s_idle
  io.DMemLoad.resp.ready := true.B
  io.DMemStore.resp.ready := true.B

  switch(LsuState){
    is(s_idle){
      when(io.DMemStore.req.fire){
        LsuState := s_wait_write
      }
      when(io.DMemLoad.req.fire){
        LsuState := s_wait_read
      }
    }
    is(s_wait_write){
      when(io.DMemStore.resp.fire){
        LsuState := s_idle
      }
    }
    is(s_wait_read){
      when(io.DMemLoad.resp.fire){
        LsuState := s_idle
      }
    }
  }

  io.DMemStore.req.bits.addr := memMisalignedAddr
  io.DMemStore.req.bits.size := memDataSize
  io.DMemStore.req.bits.wen  := isStore
  io.DMemStore.req.bits.wmask := memWstrb
  io.DMemStore.req.bits.wdata := memWdata
  io.DMemStore.req.valid := isStore && io.req.valid && LsuState===s_idle

  io.DMemLoad.req.bits.addr := io.req.bits.addr
  io.DMemLoad.req.bits.size := 2.U
  io.DMemLoad.req.bits.wen  := false.B
  io.DMemLoad.req.bits.wmask := 0.U
  io.DMemLoad.req.bits.wdata := 0.U
  io.DMemLoad.req.valid := isLoad && io.req.valid && LsuState===s_idle

  val addrLow2Bit = io.req.bits.addr(1,0)
  val loadByteData=Mux1hMap(addrLow2Bit,Map(
    "b00".U -> io.DMemLoad.resp.bits.data(7 , 0),
    "b01".U -> io.DMemLoad.resp.bits.data(15, 8),
    "b10".U -> io.DMemLoad.resp.bits.data(23,16),
    "b11".U -> io.DMemLoad.resp.bits.data(31,24)
  ))
  val loadHalfData=Mux1hMap(addrLow2Bit,Map(
    "b00".U -> io.DMemLoad.resp.bits.data(15, 0), 
    "b01".U -> io.DMemLoad.resp.bits.data(15, 0),
    "b10".U -> io.DMemLoad.resp.bits.data(31,16), 
    "b11".U -> io.DMemLoad.resp.bits.data(31,16)
  ))
  val loadDataResult=Mux1hDefMap(addrLow2Bit,Map(
    LD_LW -> io.DMemLoad.resp.bits.data,
    LD_LH -> Sext(loadHalfData,32),
    LD_LB -> Sext(loadByteData,32),
    LD_LHU-> Zext(loadHalfData,32),
    LD_LBU-> Zext(loadByteData,32),
  ))

  io.resp.valid := io.DMemStore.resp.fire || io.DMemLoad.resp.fire
  io.resp.bits.rdata := loadDataResult
}

class LsuReqBundle extends ErXCoreBundle{
  val lsType = UInt(LS_XXX.length.W)
  val addr   = UInt(XLEN.W)
  val wdata  = UInt(XLEN.W)
}

class LsuRespBundle extends ErXCoreBundle{
  val rdata = UInt(XLEN.W)
}