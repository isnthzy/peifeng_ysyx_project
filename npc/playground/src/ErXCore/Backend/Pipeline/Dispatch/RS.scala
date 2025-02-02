package ErXCore

import chisel3._
import chisel3.util._

class RS(rsSize: Int = 4,enqWidth: Int = 2,deqWidth: Int = 1,StoreSeq: Boolean = false) extends ErXCoreModule {
  //support other rsSize
  val io = IO(new Bundle {
    val in = Vec(enqWidth,Flipped(Decoupled(new RenameIO)))
    val from_dr   = Input(new RSFromRename())
    val from_rob  = Input(new RSFromROB())
    val out = Vec(deqWidth,Decoupled(new RenameIO))
  })

  val rsBuff = RegInit(VecInit(Seq.fill(rsSize)(0.U.asTypeOf(new RenameIO))))
  val rsBuffValid = RegInit(VecInit(Seq.fill(rsSize)(false.B)))
  val rsROBAge  = RegInit(VecInit(Seq.fill(rsSize)(0.U(RobAgeWidth.W))))
  val rsCount   = PopCount(Cat(rsBuffValid.map(_ === true.B)))
  val rsFull    = rsBuffValid.reduce(_&&_)
  val rsAllowin = rsSize.U - enqWidth.U >= rsCount 
  val rsEmpty   = !rsBuffValid.reduce(_||_)

  //RS enqueue
  (0 until enqWidth).foreach{i=> io.in(i).ready := rsAllowin}
  val enqSelect = Wire(Vec(enqWidth,UInt(log2Up(rsSize).W)))
  val emptySlot = Wire(Vec(enqWidth,UInt(rsSize.W)))
  for(i <- 0 until enqWidth){
    if(i == 0){
      emptySlot(i) := ~rsBuffValid.asUInt
      enqSelect(i) := PriorityEncoder(emptySlot(i))
    }else{
      emptySlot(i) := ~rsBuffValid.asUInt & ~UIntToOH(enqSelect(i-1))
      enqSelect(i) := PriorityEncoder(emptySlot(i))
    }
  }
  // val emptySlot = ~rsBuffValid.asUInt
  // val enqSelect = PriorityEncoder(emptySlot)
  for(i <- 0 until enqWidth){
    when(io.in(i).fire){
      rsBuff(enqSelect(i)) := io.in(i).bits
      rsBuffValid(enqSelect(i)) := true.B
      rsROBAge := 0.U
    }
  }



  //RS dequeue
  //only support one deqWidth

  def OldFirstArb(inputs: Vec[ArbAgeBundle], ArbSize: Int): Vec[UInt] = {
    if(deqWidth == 1){
      require(isPow2(rsSize), "rsSize must be power of 2")
      val n = ArbSize / 2
      require(ArbSize > 0 && (ArbSize & (ArbSize - 1)) == 0, "ArbSize must be a power of 2!") // 确保 ArbSize 是 2 的幂次
      def getIdx(x: UInt): UInt = x(RobAgeWidth - 1, 0)
      def getFlag(x: UInt): Bool = x(RobAgeWidth - 1).asBool

      if (ArbSize == 1) {
        // 只有一个输入时，直接返回该项的年龄
        inputs(0).age.asTypeOf(Vec(deqWidth,UInt(RobAgeWidth.W)))
      } else if (ArbSize == 2) {
        // 两个输入时直接比较，返回年龄较小者
        val retAge = Wire(UInt(RobAgeWidth.W))
        when(getFlag(inputs(0).age) === getFlag(inputs(1).age)){
          var selectIdx0 = getIdx(inputs(0).age) & Sext(inputs(0).srcReady, RobAgeWidth - 1)
          var selectIdx1 = getIdx(inputs(1).age) & Sext(inputs(1).srcReady, RobAgeWidth - 1)
          retAge := Mux(selectIdx0 < selectIdx1, inputs(0).age, inputs(1).age)
        }.otherwise{
          var selectIdx0 = getIdx(inputs(0).age) & Sext(inputs(0).srcReady, RobAgeWidth - 1)
          var selectIdx1 = getIdx(inputs(1).age) & Sext(inputs(1).srcReady, RobAgeWidth - 1)
          retAge := Mux(selectIdx0 > selectIdx1, inputs(0).age, inputs(1).age)
        }
        retAge.asTypeOf(Vec(deqWidth,UInt(RobAgeWidth.W)))
      } else {
        // 多于两个输入时，递归处理左右两部分
        val tmp1 = OldFirstArb(VecInit(inputs.take(n)), n)
        val tmp2 = OldFirstArb(VecInit(inputs.drop(n)), n)
        val retArg = Vec(deqWidth,UInt(RobAgeWidth.W))
        when(getFlag(tmp1(0)) === getFlag(tmp2(0))){
          var tmpIdx0 = getIdx(tmp1(0)) & Sext(inputs(0).srcReady, RobAgeWidth - 1)
          var tmpIdx1 = getIdx(tmp2(0)) & Sext(inputs(1).srcReady, RobAgeWidth - 1)
          retArg(0) := Mux(tmpIdx0 < tmpIdx1, tmp1(0), tmp2(0))
        }.otherwise{
          var tmpIdx0 = getIdx(tmp1(0)) & Sext(inputs(0).srcReady, RobAgeWidth - 1)
          var tmpIdx1 = getIdx(tmp2(0)) & Sext(inputs(1).srcReady, RobAgeWidth - 1)
          retArg(0) := Mux(tmpIdx0 > tmpIdx1, tmp1(0), tmp2(0))
        }
        retArg
        // Mux(tmp1(0) < tmp2(0), tmp1(0), tmp2(0)) // 返回左右部分中年龄较小者
      }
    }else{
      require(isPow2(rsSize), "rsSize must be power of 2")
      require(rsSize >= 2, "rsSize must be greater than 2")
      val n = ArbSize / 2
      require(ArbSize > 0 && (ArbSize & (ArbSize - 1)) == 0, "ArbSize must be a power of 2!") // 确保 ArbSize 是 2 的幂次
      def getIdx(x: UInt): UInt = x(RobAgeWidth - 1, 0)
      def getFlag(x: UInt): Bool = x(RobAgeWidth - 1).asBool

      if (ArbSize == 2) {
        // 两个输入时直接比较，返回年龄较小者
        val retAge = Vec(1,UInt(RobAgeWidth.W))
        when(getFlag(inputs(0).age) === getFlag(inputs(1).age)){
          var selectIdx0 = getIdx(inputs(0).age) & Sext(inputs(0).srcReady, RobAgeWidth - 1)
          var selectIdx1 = getIdx(inputs(1).age) & Sext(inputs(1).srcReady, RobAgeWidth - 1)
          retAge(0) := Mux(selectIdx0 < selectIdx1, inputs(0).age, inputs(1).age)
        }.otherwise{
          var selectIdx0 = getIdx(inputs(0).age) & Sext(inputs(0).srcReady, RobAgeWidth - 1)
          var selectIdx1 = getIdx(inputs(1).age) & Sext(inputs(1).srcReady, RobAgeWidth - 1)
          retAge(0) := Mux(selectIdx0 > selectIdx1, inputs(0).age, inputs(1).age)
        }
        retAge
      } else {
        // 多于两个输入时，递归处理左右两部分
        val retAge = Vec(deqWidth,UInt(RobAgeWidth.W))
        val tmp1 = OldFirstArb(VecInit(inputs.take(n)), n)
        val tmp2 = OldFirstArb(VecInit(inputs.drop(n)), n)
        retAge(0) := tmp1
        retAge(1) := tmp2
        retAge
      }
    }
  }

  val rsArbPacket = VecInit(Seq.fill(rsSize)(new ArbAgeBundle))
  val rsReadyList = VecInit(Seq.fill(rsSize)(Bool()))
  for(i <- 0 until rsSize){
    var rs1Ready = io.from_dr.availList(rsBuff(i).pf.prfSrc1)
    var rs2Ready = io.from_dr.availList(rsBuff(i).pf.prfSrc2)
    rsReadyList(i) := rs1Ready && rs2Ready && rsBuffValid(i)
    rsArbPacket(i).age := rsROBAge(i)
    rsArbPacket(i).srcReady := rsReadyList(i)
  }
  val deqSelect = OldFirstArb(rsArbPacket, rsSize)
  for(i <- 0 until deqWidth){
    io.out(i).valid := rsReadyList(deqSelect(i))
    io.out(i).bits  := rsBuff(deqSelect(i))
    when(io.out(i).fire){
      rsBuffValid(deqSelect(i)) := false.B
    }
  }

}

class ArbAgeBundle extends ErXCoreBundle {
  val age = UInt(RobAgeWidth.W)
  val srcReady = Bool()
}