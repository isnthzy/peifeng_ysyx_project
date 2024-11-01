package Cache
import chisel3._
import chisel3.util._
import CoreConfig.CacheConfig
// import Axi.AxiBridge
import Util.RandomNum

class ICache extends Module with CacheConfig {
  val BANK_SIZE = 1 << INDEX_WIDTH
  val io = IO(new Bundle {
    val valid  = Input(Bool())
    val tag    = Input(UInt(TAG_WIDTH.W)) //tag下一周期输入
    val index  = Input(UInt(INDEX_WIDTH.W))
    val offset = Input(UInt(OFFSET_WIDTH.W))
    val addrRp = Output(Bool()) //Respond
    val dataRp = Output(Bool())
    val rdata  = Output(UInt(32.W))

    val out = new AxiCacheIO()
  })
  val DataBank = Array.fill(WAY_NUM_I)(Module(new DataRAM(BANK_SIZE, LINE_WIDTH)).io)
  val TagvBank = Array.fill(WAY_NUM_I)(Module(new TagvRAM(BANK_SIZE, TAG_WIDTH)).io)
  //外部维护一个tagv，根据reset清除
  val tagValid = RegInit(VecInit(Seq.fill(BANK_SIZE)(0.U.asTypeOf(Vec(WAY_NUM_I,Bool())))))
  val readTagv = Wire(Vec(WAY_NUM_I, new Bundle {
    val v = Bool()
    val tag = UInt(TAG_WIDTH.W)
  })) 
  val readData = Wire(Vec(WAY_NUM_I, UInt(LINE_WIDTH.W)))

  val requestIdxBuff = RegInit(0.U(INDEX_WIDTH.W))
  val requestTagBuff = RegInit(0.U(TAG_WIDTH.W))
  val requestOffsetBuff = RegInit(0.U(OFFSET_WIDTH.W))
  val hitWayBuff = RegInit(0.U(log2Ceil(WAY_NUM_I).W))
  val readDataLineBuff = RegInit(VecInit(Seq.fill(LINE_WORD_NUM)(0.U(32.W))))
  val readDataLineIdx  = RegInit(0.U(log2Ceil(LINE_WORD_NUM).W))
  
  for(i <- 0 until WAY_NUM_I){
    readTagv(i).v   := RegNext(tagValid(requestIdxBuff)(i))
    readTagv(i).tag := TagvBank(i).douta
    readData(i) := DataBank(i).doutb
  }

  val (s_idle   ::
       s_lookup ::
       s_miss   ::
       s_refill  ::
       s_respond ::
       Nil) = Enum(5)
  val cacheState = RegInit(s_idle)
  val reqIndex = WireDefault(0.U(log2Ceil(INDEX_WIDTH).W))
  val cacheReqValid = cacheState === s_idle && io.valid
  val randomWay = RandomNum("b10111011".U)(log2Ceil(WAY_NUM_I) - 1,0)
  for(i <- 0 until WAY_NUM_I){
    DataBank(i).clka := clock    
    DataBank(i).wea  := (cacheState === s_respond) && (hitWayBuff === i.U)
    DataBank(i).addra := requestIdxBuff
    DataBank(i).dina  := readDataLineBuff.asUInt
    DataBank(i).clkb  := clock
    DataBank(i).addrb := Mux(cacheReqValid,io.index,requestIdxBuff)
  
    TagvBank(i).clka := clock
    TagvBank(i).wea  := (cacheState === s_miss) && (randomWay === i.U)
    TagvBank(i).addra := Mux(cacheReqValid,io.index,requestIdxBuff)
    TagvBank(i).dina  := requestTagBuff
    when((cacheState === s_miss) && (randomWay === i.U)){
      tagValid(requestIdxBuff)(i) := 1.U
    }
    
  } //NOTE:设置默认值，后续通过覆写实现读
  val cacheLookupHit = WireDefault(false.B)
  val cacheUnBusy = cacheState === s_idle || cacheState === s_respond || cacheLookupHit
  
  
  io.addrRp := cacheUnBusy
  io.dataRp := cacheState === s_respond || cacheLookupHit
  io.rdata  := readDataLineBuff(requestOffsetBuff(OFFSET_WIDTH - 1,2))
  io.out.rd.valid := cacheState === s_miss
  io.out.rd.bits.stype := "b100".U
  io.out.rd.bits.addr := Cat(requestTagBuff,requestIdxBuff,0.U(OFFSET_WIDTH.W))
  io.out.rret.ready := true.B
  io.out.wret.ready := true.B

  switch(cacheState){
    is(s_idle){
      when(io.valid){
        requestTagBuff    := io.tag
        requestIdxBuff    := io.index
        requestOffsetBuff := io.offset
        cacheState := s_lookup
      }
    }
    is(s_lookup){
      for(i <- 0 until WAY_NUM_I){
        when(readTagv(i).v && readTagv(i).tag === requestTagBuff){
          cacheLookupHit := true.B
          hitWayBuff := i.U
          readDataLineBuff := readData(i).asTypeOf(readDataLineBuff)
        }
      }
      when(cacheLookupHit){
        when(io.valid){
          requestTagBuff    := io.tag
          requestIdxBuff    := io.index
          requestOffsetBuff := io.offset
          cacheState := s_lookup
        }.otherwise{
          cacheState := s_respond
        }
      }.otherwise{
        cacheState := s_miss
      }
    }
    is(s_miss){
      readDataLineIdx  := 0.U
      readDataLineBuff := 0.U.asTypeOf(readDataLineBuff)
      when(io.out.rd.fire){
        cacheState := s_refill
      }
    }
    is(s_refill){
      when(io.out.rret.valid){
        readDataLineIdx := readDataLineIdx + 1.U
        readDataLineBuff(readDataLineIdx) := io.out.rret.bits.data
        when(io.out.rret.bits.last){
          cacheState := s_respond
        }
      }
    }
    is(s_respond){
      when(io.valid){
        requestTagBuff    := io.tag
        requestIdxBuff    := io.index
        requestOffsetBuff := io.offset
        cacheState := s_lookup
      }.otherwise{//优化cache状态机
        cacheState := s_idle
      } 
    }
  }

  io.out.wr.bits:=0.U.asTypeOf(io.out.wr.bits)
  io.out.wr.valid:=false.B
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
  //设计思路：
  //Icache通过AxiCacheIO接口发送到Axi仲裁器
  //lsu把信号转换成AxiCacheIO，不使用仲裁器
  val rd  = DecoupledIO(new AxiCacheReadIO())
  val wr  = DecoupledIO(new AxiCacheWriteIO())
  val rret = Flipped(DecoupledIO(new AxiCacheReadReturnIO()))
  val wret = Flipped(DecoupledIO(new AxiCacheWriteReturnIO()))
}

class Core2AxiReadIO extends Bundle{
  val req  = Output(Bool())
  val addr = Output(UInt(32.W))
  val size = Output(UInt(3.W))
  val addrOk = Input(Bool())
}

class Core2AxiRespondIO extends Bundle{
  val dataOk= Input(Bool())
  val data  = Input(UInt(32.W))
}