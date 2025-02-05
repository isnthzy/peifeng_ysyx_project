package ErXCore

import chisel3._
import chisel3.util._
import DecodeSignal._

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
  val isStore   = RegInit(VecInit(Seq.fill(rsSize)(false.B)))

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
  for(i <- 0 until enqWidth){
    when(io.in(i).fire){
      rsBuff(enqSelect(i)) := io.in(i).bits
      rsBuffValid(enqSelect(i)) := true.B
      rsROBAge(enqSelect(i)) := io.from_rob.robAge(i)
      isStore(enqSelect(i)) := isStoreInst(io.in(i).bits.cs.lsType)
    }
  }

  //RS dequeue
  //only support one deqWidth

  def OldFirstArb(inputs: Vec[ArbAgeBundle], ArbSize: Int): Vec[ArbAgeBundle] = {
    require(isPow2(rsSize), "rsSize must be power of 2")
    require(isPow2(ArbSize), "ArbSize must be a power of 2!") // 确保 ArbSize 是 2 的幂次
    require(deqWidth == 1 || deqWidth == 2, "deqWidth must be 1 or 2")
    def getIdx(x: UInt): UInt = x(RobAgeWidth - 1, 0)
    def getFlag(x: UInt): Bool = x(RobAgeWidth - 1).asBool
    def selectIdx(age: UInt, xReady: Bool): UInt = {
      if(StoreSeq){
        getIdx(age) & Sext(xReady, RobAgeWidth - 1)
      }else{
        getIdx(age) & Sext(xReady, RobAgeWidth - 1)
      }
    }

    def SelectAge(A: ArbAgeBundle, B: ArbAgeBundle): ArbAgeBundle = {
      def compare(older: ArbAgeBundle, younger: ArbAgeBundle): ArbAgeBundle = {
        if(StoreSeq){
/*compare power of 
| >(younger) | young - isStore | young - instRdy | old - isStore | old - instRdy | 判断结果 |
| ---------- | --------------- | --------------- | ------------- | ------------- | -------- |
| young>old  | 0               | 0               | 0             | 0             | old      |
| young>old  | 0               | 0               | 0             | 1             | old      |
| young>old  | 0               | 0               | 1             | 0             | old      |
| young>old  | 0               | 0               | 1             | 1             | old      |
| young>old  | 0               | 1               | 0             | 0             | young    |
| young>old  | 0               | 1               | 0             | 1             | old      |
| young>old  | 0               | 1               | 1             | 0             | old      |
| young>old  | 0               | 1               | 1             | 1             | old      |
| young>old  | 1               | 0               | 0             | 0             | old      |
| young>old  | 1               | 0               | 0             | 1             | old      |
| young>old  | 1               | 0               | 1             | 0             | old      |
| young>old  | 1               | 0               | 1             | 1             | old      |
| young>old  | 1               | 1               | 0             | 0             | old      |
| young>old  | 1               | 1               | 0             | 1             | old      |
| young>old  | 1               | 1               | 1             | 0             | old      |
| young>old  | 1               | 1               | 1             | 1             | old      |
*/ 
          Mux(!older.srcReady && younger.srcReady && !older.isStore && !younger.isStore, younger, older)
        }else{
/*compare power of 
| >(younger) | young - instRdy | old - instRdy | 判断结果 |
| ---------- | --------------- | ------------- | -------- |
| young>old  | 0               | 0             | old      |
| young>old  | 0               | 1             | old      |
| young>old  | 1               | 0             | young    |
| young>old  | 1               | 1             | old      |
*/
          Mux(!older.srcReady && younger.srcReady, younger, older)
        }
      }
      val ageCompare = Mux(getFlag(A.age) === getFlag(B.age), getIdx(A.age) < getIdx(B.age),
                                                              getIdx(A.age) > getIdx(B.age)) 
      //NOTE:ageCompare is true.B，A older B
      Mux(ageCompare, 
        compare(A, B), 
        compare(B, A)
      )
    }
    
    if(deqWidth == 1){
      if (ArbSize == 1) {
        // 只有一个输入时，直接返回该项的年龄
        inputs(0).asTypeOf(Vec(deqWidth,new ArbAgeBundle(rsSize)))
      } else if (ArbSize == 2) {
        // 两个输入时直接比较，返回年龄较小者
        val retAge = SelectAge(inputs(0), inputs(1))
        retAge.asTypeOf(Vec(deqWidth,new ArbAgeBundle(rsSize)))
      } else {
        // 多于两个输入时，递归处理左右两部分
        val tmp1 = OldFirstArb(VecInit(inputs.take(ArbSize / 2)), ArbSize / 2)
        val tmp2 = OldFirstArb(VecInit(inputs.drop(ArbSize / 2)), ArbSize / 2)
        val retArg = Vec(deqWidth,new ArbAgeBundle(rsSize))
        retArg := SelectAge(tmp1(0),tmp2(0))
        retArg
      }
    }else{
      require(rsSize >= 2, "rsSize must be greater than 2")
      if (ArbSize == 2) {
        val retAge = SelectAge(inputs(0), inputs(1))
        retAge.asTypeOf(Vec(deqWidth,new ArbAgeBundle(rsSize)))
      } else {
        // 多于两个输入时，递归处理左右两部分
        val retAge = Vec(deqWidth,new ArbAgeBundle(rsSize))
        val tmp1 = OldFirstArb(VecInit(inputs.take(ArbSize / 2)), ArbSize / 2)
        val tmp2 = OldFirstArb(VecInit(inputs.drop(ArbSize / 2)), ArbSize / 2)
        retAge(0) := tmp1
        retAge(1) := tmp2
        retAge
      }
    }
  }

  val rsArbPacket = Wire(Vec(rsSize, new ArbAgeBundle(rsSize)))
  val rsReadyList = Wire(Vec(rsSize, Bool()))
  for(i <- 0 until rsSize){
    var rs1Ready = io.from_dr.availList(rsBuff(i).pf.prfSrc1)
    var rs2Ready = io.from_dr.availList(rsBuff(i).pf.prfSrc2)
    rsReadyList(i) := rs1Ready && rs2Ready && rsBuffValid(i)
    rsArbPacket(i).age := rsROBAge(i)
    rsArbPacket(i).srcReady := rsReadyList(i)
    rsArbPacket(i).isStore := isStore(i)
    rsArbPacket(i).rsIdx := i.U
  }
  val deqSelect = Wire(Vec(deqWidth, UInt(log2Up(rsSize).W)))
  dontTouchUtil(deqSelect)
  deqSelect := OldFirstArb(rsArbPacket, rsSize).map(_.rsIdx)

  for(i <- 0 until deqWidth){
    io.out(i).valid := rsReadyList(deqSelect(i))
    io.out(i).bits  := rsBuff(deqSelect(i))
    io.out(i).bits.robIdx := rsROBAge(deqSelect(i))
    when(io.out(i).fire){
      rsBuffValid(deqSelect(i)) := false.B
      isStore(deqSelect(i)) := false.B
    }
  }

  //flush 
  when(io.from_rob.flush){
    io.in.map(_.ready := false.B)
    io.out.map(_.valid := false.B)
    for(i <- 0 until rsSize){
      rsBuffValid(i) := false.B
      isStore(i) := false.B
    }
  }
}


class ArbAgeBundle(rsSize: Int) extends ErXCoreBundle {
  val age = UInt(RobAgeWidth.W)
  val srcReady = Bool()
  val isStore  = Bool()
  val rsIdx    = UInt(log2Up(rsSize).W)
}