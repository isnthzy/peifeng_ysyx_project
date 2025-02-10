package ErXCore

import chisel3._
import chisel3.util._
import coursier.error.ResolutionError.Simple

class StoreQueue extends ErXCoreModule {
  val storeQueueSize = 4
  require(isPow2(storeQueueSize), "storeQueueSize must be power of 2")
  val io = IO(new Bundle {
    val from_rob = Input(new StoreQueueFromROB)
    val st = Flipped(new SimpleMemIO)
    val ld = Flipped(new SimpleMemIO)
    val out = new Bundle{
      val st = new SimpleMemIO
      val ld = new SimpleMemIO
    }
  })
  //NOTE:ref from Chisel Decoupled.scala
  val queue = RegInit(VecInit(Seq.fill(storeQueueSize)(0.U.asTypeOf(Valid(new SimpleReqIO)))))
  val enqPtr = Counter(storeQueueSize)
  val deqPtr = Counter(storeQueueSize)
  val maybeFull = RegInit(false.B)
  val ptrMatch = enqPtr.value === deqPtr.value
  val empty = ptrMatch && !maybeFull
  val full  = ptrMatch && maybeFull
  val doDeqCount = RegInit(0.U((log2Up(storeQueueSize)+1).W))

  val doEnqFire = io.st.req.fire
  val doDeqFire = io.out.st.resp.fire

  val flush_idle :: flush_wait_resp :: Nil = Enum(2)
  val flushState =  RegInit(flush_idle)
//store
  val st_idle :: st_wait_resp :: Nil = Enum(2)
  val stState = RegInit(false.B)
  io.st.req.ready := !full
  io.st.resp.valid := RegNext(doEnqFire) //store resp fire at RegNext(req.fire) (lsu)
  io.st.resp.bits.data := DontCare
  io.out.st.req.valid := !empty && (doDeqCount > 0.U)
  io.out.st.req.bits := queue(deqPtr.value).bits
  io.out.st.resp.ready := true.B


  when(io.from_rob.doDeq&&doDeqFire){
    doDeqCount := doDeqCount
  }.otherwise{
    when(io.from_rob.doDeq){
      doDeqCount := doDeqCount + 1.U
    }
    when(doDeqFire){
      doDeqCount := doDeqCount - 1.U
    }
  }

  when(doEnqFire) {
    queue(enqPtr.value).bits  := io.st.req.bits
    queue(enqPtr.value).valid := true.B
    enqPtr.inc()
  }
  when(doDeqFire) {
    deqPtr.inc()
  }
  when(doEnqFire =/= doDeqFire) {
    maybeFull := doEnqFire
  }


  //load
  val s_idle :: s_queue_hit :: s_cache_hit :: Nil = Enum(3)
  val loadState = RegInit(s_idle)
  val loadBuff = RegInit(0.U.asTypeOf(new SimpleReqIO))
  val loadStoreQueueHit = WireDefault(false.B)
  val loadStoreQueueHitIdx = RegInit(0.U(log2Up(storeQueueSize).W))
  dontTouchUtil(loadStoreQueueHitIdx)
  for(i <- 0 until storeQueueSize){
    when(queue(i).bits.addr === io.ld.req.bits.addr && queue(i).valid){
      loadStoreQueueHit := true.B
      loadStoreQueueHitIdx := i.U
    }
  }
  io.out.ld.req.valid := io.ld.req.valid && !loadStoreQueueHit
  io.out.ld.req.bits  := io.ld.req.bits
  io.out.ld.resp.ready := true.B
  io.ld.req.ready  := false.B //override
  io.ld.resp.valid := false.B //override
  io.ld.resp.bits := 0.U.asTypeOf(new SimpleRespIO) //override
  switch(loadState){
    is(s_idle){
      when(io.ld.req.valid){
        when(loadStoreQueueHit){
          io.ld.req.ready := true.B
          loadState := s_queue_hit
        }.otherwise{
          io.ld.req.ready := io.out.ld.req.ready
          when(io.ld.req.fire){
            loadState := s_cache_hit
          }
        }
      }
    }
    is(s_queue_hit){
      io.ld.resp.valid := true.B
      when(io.ld.resp.fire){
        io.ld.resp.bits.data := queue(loadStoreQueueHitIdx).bits.wdata
        loadState := s_idle
      }
    }
    is(s_cache_hit){
      io.ld.resp.valid := io.out.ld.resp.valid
      when(io.ld.resp.fire){
        io.ld.resp.bits := io.out.ld.resp.bits
        loadState := s_idle
      }
    }
  }
  
  def FlushAll() = {
    enqPtr.reset()
    deqPtr.reset()
    // doDeqCount := 0.U
    maybeFull := false.B
    queue.foreach(_.valid := false.B)
  }
  
  switch(flushState){
    is(flush_idle){
      when(io.from_rob.flush){
        when((doDeqCount === 1.U && doDeqFire) || doDeqCount === 0.U){
          FlushAll()
        }.otherwise{
          flushState := flush_wait_resp
        }
        // when(io.out.st.req.valid && !io.out.st.req.ready){
        //   flushState := flush_wait_resp
        // }.otherwise{
        //   FlushAll()
        // }
      }
    }
    is(flush_wait_resp){
      when(io.out.st.resp.fire && doDeqCount === 1.U){
        //确保写事务都已完成
        flushState := flush_idle
        FlushAll()
      }
    }
  }

}

