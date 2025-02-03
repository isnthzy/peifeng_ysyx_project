package ErXCore

import chisel3._
import chisel3.util._
import DecodeSignal._

class LSU extends ErXCoreModule{
  val io = IO(new Bundle{
    val valid  = Input(Bool())
    val busy   = Output(Bool())
    val lsType = Input(UInt(2.W))
    val addr   = Input(UInt(XLEN.W))
    val stData = Input(UInt(XLEN.W))
    val ldData = Output(UInt(XLEN.W))

    val DMemStore = new SimpleMemIO
    val DMemLoad  = new SimpleMemIO
  })
  
  val isMem = isLoadStore(io.lsType)
  val isLoad = isLoadInst(io.lsType)
  val isStore = isStoreInst(io.lsType)

  val memMisalignedAddr=io.addr
  val memStoreSrc=io.stData
  val memByteSize=(io.lsType===SDEF(LD_LB)||io.lsType===SDEF(LD_LBU)
                 ||io.lsType===SDEF(ST_SB))
  val memHalfSize=(io.lsType===SDEF(LD_LH)||io.lsType===SDEF(LD_LHU)
                 ||io.lsType===SDEF(ST_SH))
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
  io.DMemStore.req.valid := isStore && io.valid && LsuState===s_idle

  io.DMemLoad.req.bits.addr := io.addr
  io.DMemLoad.req.bits.size := 2.U
  io.DMemLoad.req.bits.wen  := false.B
  io.DMemLoad.req.bits.wmask := 0.U
  io.DMemLoad.req.bits.wdata := 0.U
  io.DMemLoad.req.valid := isLoad && io.valid && LsuState===s_idle

  val addrLow2Bit = io.addr(1,0)
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

  io.ldData := loadDataResult
  io.busy := LsuState =/= s_idle


}

class SimpleReqIO extends ErXCoreBundle{
  val addr = Input(UInt(XLEN.W))
  val size = Input(UInt(3.W))
  val wen   = Input(Bool()) //read:0 write:1
  val wmask = Input(UInt((XLEN/8).W))
  val wdata = Input(UInt(XLEN.W))
}

class SimpleRespIO extends ErXCoreBundle{
  val data  = Input(UInt(32.W))
}


class SimpleMemIO extends ErXCoreBundle{
  //设计思路：
  //Icache通过SimpleIO接口发送到Axi仲裁器
  //lsu把信号转换成SimpleIO，不使用仲裁器
  val req  = DecoupledIO(new SimpleReqIO())
  val resp = Flipped(DecoupledIO(new SimpleRespIO()))
}

class Core2AxiReadIO extends ErXCoreModule{
  val req  = Output(Bool())
  val addr = Output(UInt(32.W))
  val size = Output(UInt(3.W))
  val addrOk = Input(Bool())
}

class Core2AxiRespondIO extends ErXCoreModule{
  val dataOk= Input(Bool())
  val data  = Input(UInt(32.W))
}

class AxiCacheReadIO extends Bundle{
  val stype = UInt(3.W)
  val addr = UInt(32.W)
}

class AxiCacheReadReturnIO extends Bundle{
  val last  = Input(Bool())
  val resp  = Input(UInt(2.W))
  val data  = Input(UInt(32.W))
}

class AxiCacheWriteReturnIO extends Bundle{
  val last  = Input(Bool())
  val resp  = Input(UInt(2.W))
}

class AxiCacheWriteIO extends Bundle{
  val stype = UInt(3.W)
  val addr = UInt(32.W)
  val strb = UInt(4.W)
  val data = UInt(32.W)
}

class AxiCacheIO extends Bundle{
  val rd  = DecoupledIO(new AxiCacheReadIO())
  val wr  = DecoupledIO(new AxiCacheWriteIO())
  val rret = Flipped(DecoupledIO(new AxiCacheReadReturnIO()))
}

