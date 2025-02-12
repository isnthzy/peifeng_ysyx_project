package ErXCore

import chisel3._
import chisel3.util._
import DecodeSignal._

class RS(rsSize: Int = 4,enqWidth: Int,deqWidth: Int,StoreSeq: Boolean = false) extends ErXCoreModule {
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
  def getIdx(x: UInt): UInt = x(RobIdxWidth - 1, 0)
  def getFlag(x: UInt): Bool = x(RobAgeWidth - 1).asBool
  def OldFirstArb(inputs: Vec[ArbAgeBundle], ArbSize: Int): Vec[ArbAgeBundle] = {
    require(isPow2(rsSize), "rsSize must be power of 2")
    require(isPow2(ArbSize), "ArbSize must be a power of 2!") // 确保 ArbSize 是 2 的幂次
    require(deqWidth == 1 || deqWidth == 2, "deqWidth must be 1 or 2")
    def getIdx(x: UInt): UInt = x(RobIdxWidth - 1, 0)
    def getFlag(x: UInt): Bool = x(RobAgeWidth - 1).asBool
    def selectIdx(age: UInt, xReady: Bool): UInt = {
      getIdx(age) & Sext(xReady, RobAgeWidth - 1)
    }

    def SelectAge(A: ArbAgeBundle, B: ArbAgeBundle): ArbAgeBundle = {
      def compare(older: ArbAgeBundle,younger: ArbAgeBundle): ArbAgeBundle = {
        if(StoreSeq){
/*compare power of deepseek , is not true deepseek give me error result!!
| 条件分类            | 关键条件                                                     | 选择结果 |
| :------------------ | :----------------------------------------------------------- | :------- |
| **young 无效**      | `y_valid = 0`                                                | old      |
| **young 有效**      |                                                              |          |
| 1. old 无效         | `y_valid = 1` 且 `o_valid = 0`                               | young    |
| 2. old 有效但未就绪 | `y_valid = 1` 且 `o_valid = 1` 且 `o_rdy = 0`                | young    |
| 3. old 有效且就绪   | `y_valid = 1` 且 `o_valid = 1` 且 `o_rdy = 1` 且 `y_store = 1` | old      |
| 4. old 有效且就绪   | `y_valid = 1` 且 `o_valid = 1` 且 `o_rdy = 1` 且 `y_store = 0` | young    |
1. **young 无效时**:
   - 直接选择 old 条目。
2. **young 有效时**:
   - **优先选择 young** 的三种情况：
     - old 无效（`o_valid = 0`）。
     - old 有效但未就绪（`o_rdy = 0`）。
     - old 有效且就绪，但 young 是非存储指令（`y_store = 0`）。
   - **仅当以下条件时选择 old**：
     - old 有效且就绪（`o_rdy = 1`），且 young 是存储指令（`y_store = 1`）。
*/
          Mux(younger.isValid & (~older.isValid | (younger.srcReady & ~older.srcReady & ~older.isStore)), younger, older)
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
      //年龄越小的越old，优先级越高
      //NOTE:ageCompare is true.B，A older B
      val compareResult = WireDefault(Mux(ageCompare, 
        compare(A, B), 
        compare(B, A)
      ))
      dontTouchUtil(compareResult)
      Mux(ageCompare, 
        compare(A, B), 
        compare(B, A)
      )
    } //return older
    
    if(deqWidth == 1){
      if (ArbSize == 1) {
        // 只有一个输入时，直接返回该项的年龄
        val retVec = Wire(Vec(deqWidth,new ArbAgeBundle(rsSize)))
        retVec := inputs(0).asTypeOf(Vec(deqWidth,new ArbAgeBundle(rsSize)))
        retVec
      } else if (ArbSize == 2) {
        // 两个输入时直接比较，返回年龄较小者
        val retVec = Wire(Vec(deqWidth,new ArbAgeBundle(rsSize)))
        retVec := SelectAge(inputs(0), inputs(1)).asTypeOf(Vec(deqWidth,new ArbAgeBundle(rsSize)))
        retVec
      } else {
        // 多于两个输入时，递归处理左右两部分
        val tmp1 = OldFirstArb(VecInit(inputs.take(ArbSize / 2)), ArbSize / 2)
        val tmp2 = OldFirstArb(VecInit(inputs.drop(ArbSize / 2)), ArbSize / 2)
        val retVec = Wire(Vec(deqWidth,new ArbAgeBundle(rsSize)))
        retVec := SelectAge(tmp1(0),tmp2(0)).asTypeOf(Vec(deqWidth,new ArbAgeBundle(rsSize)))
        retVec
      }
    }else{
      require(rsSize >= 2, "rsSize must be greater than 4")
      if (ArbSize == 2) {
        val older = SelectAge(inputs(0), inputs(1))
        val younger = Mux(older === inputs(0), inputs(1), inputs(0))
        val retVec = Wire(Vec(deqWidth, new ArbAgeBundle(rsSize)))
        retVec(0) := older
        retVec(1) := younger
        retVec
      } else {
      // 对于大于两个输入的情况，先将输入分为左右两部分，每部分递归得到一对候选
        val leftPair = OldFirstArb(VecInit(inputs.take(ArbSize / 2)), ArbSize / 2)
        val rightPair = OldFirstArb(VecInit(inputs.drop(ArbSize / 2)), ArbSize / 2)
        // overallMin 为左右两部分中较小的那个
        val overallMin = SelectAge(leftPair(0), rightPair(0))
        // 根据 overallMin 来源决定第二小的候选
        val overallSecond = Mux(
          overallMin === leftPair(0),
          SelectAge(leftPair(1), rightPair(0)),
          SelectAge(rightPair(1), leftPair(0))
        )
        val retVec = Wire(Vec(deqWidth, new ArbAgeBundle(rsSize)))
        retVec(0) := overallMin
        retVec(1) := overallSecond
        retVec
      }
    }
  }
  /*
  |      | isValid | age  | isStore | srcReady |
| ---- | ------- | ---- | ------- | -------- |
| a    | 1       | 4    | 0       | 0        |
| b    | 1       | 5    | 1       | 0        |
| c    | 1       | 7    | 0       | 1        |
| d    | 0       |      |         |          |
以前的仲裁器可能有这个问题
思路：因而，要维护部分乱序发射。对于store指令的仲裁，需要仲裁两次。
第一个仲裁是按照正常的store部分乱序发射仲裁
第二个仲裁是仲裁出来年龄最小的store指令（并且有效）
比较第一次和第二次的结果，如果第一个仲裁结果的年龄小于store，使用第一次的结果
如果第一个仲裁结果的年龄大于最小store年龄，使用第二次的store结果
  */
  def OldStoreFirstArb(inputs: Vec[ArbAgeBundle], ArbSize: Int): Vec[ArbAgeBundle] = {
    require(isPow2(rsSize), "rsSize must be power of 2")
    require(isPow2(ArbSize), "ArbSize must be a power of 2!") // 确保 ArbSize 是 2 的幂次
    require(deqWidth == 1 || deqWidth == 2, "deqWidth must be 1 or 2")
    def selectIdx(age: UInt, xReady: Bool): UInt = {
      getIdx(age) & Sext(xReady, RobAgeWidth - 1)
    }

    def SelectAge(A: ArbAgeBundle, B: ArbAgeBundle): ArbAgeBundle = {
      def compare(older: ArbAgeBundle,younger: ArbAgeBundle): ArbAgeBundle = {
        Mux(((younger.isValid & !older.isValid) | (younger.isValid & older.isValid 
         & !older.isStore & (younger.isStore | (younger.srcReady & !older.srcReady)))), younger, older)
      }
      val ageCompare = Mux(getFlag(A.age) === getFlag(B.age), getIdx(A.age) < getIdx(B.age),
                                                              getIdx(A.age) > getIdx(B.age))
      //年龄越小的越old，优先级越高
      //NOTE:ageCompare is true.B，A older B
      val compareResult = WireDefault(Mux(ageCompare, 
        compare(A, B), 
        compare(B, A)
      ))
      dontTouchUtil(compareResult)
      Mux(ageCompare, 
        compare(A, B), 
        compare(B, A)
      )
    } //return older
    
    if(deqWidth == 1){
      if (ArbSize == 1) {
        // 只有一个输入时，直接返回该项的年龄
        val retVec = Wire(Vec(deqWidth,new ArbAgeBundle(rsSize)))
        retVec := inputs(0).asTypeOf(Vec(deqWidth,new ArbAgeBundle(rsSize)))
        retVec
      } else if (ArbSize == 2) {
        // 两个输入时直接比较，返回年龄较小者
        val retVec = Wire(Vec(deqWidth,new ArbAgeBundle(rsSize)))
        retVec := SelectAge(inputs(0), inputs(1)).asTypeOf(Vec(deqWidth,new ArbAgeBundle(rsSize)))
        retVec
      } else {
        // 多于两个输入时，递归处理左右两部分
        val tmp1 = OldStoreFirstArb(VecInit(inputs.take(ArbSize / 2)), ArbSize / 2)
        val tmp2 = OldStoreFirstArb(VecInit(inputs.drop(ArbSize / 2)), ArbSize / 2)
        val retVec = Wire(Vec(deqWidth,new ArbAgeBundle(rsSize)))
        retVec := SelectAge(tmp1(0),tmp2(0)).asTypeOf(Vec(deqWidth,new ArbAgeBundle(rsSize)))
        retVec
      }
    }else{
      require(rsSize >= 2, "rsSize must be greater than 4")
      if (ArbSize == 2) {
        val older = SelectAge(inputs(0), inputs(1))
        val younger = Mux(older === inputs(0), inputs(1), inputs(0))
        val retVec = Wire(Vec(deqWidth, new ArbAgeBundle(rsSize)))
        retVec(0) := older
        retVec(1) := younger
        retVec
      } else {
      // 对于大于两个输入的情况，先将输入分为左右两部分，每部分递归得到一对候选
        val leftPair = OldStoreFirstArb(VecInit(inputs.take(ArbSize / 2)), ArbSize / 2)
        val rightPair = OldStoreFirstArb(VecInit(inputs.drop(ArbSize / 2)), ArbSize / 2)
        // overallMin 为左右两部分中较小的那个
        val overallMin = SelectAge(leftPair(0), rightPair(0))
        // 根据 overallMin 来源决定第二小的候选
        val overallSecond = Mux(
          overallMin === leftPair(0),
          SelectAge(leftPair(1), rightPair(0)),
          SelectAge(rightPair(1), leftPair(0))
        )
        val retVec = Wire(Vec(deqWidth, new ArbAgeBundle(rsSize)))
        retVec(0) := overallMin
        retVec(1) := overallSecond
        retVec
      }
    }
  } //仲裁出来年龄最old的store指令索引，如果结果里没有store指令，则不使用这个仲裁器的结果（通过isStore来判断）
  //TODO:use ai power merge to one function


  val rsArbPacket = Wire(Vec(rsSize, new ArbAgeBundle(rsSize)))
  val rsReadyList = Wire(Vec(rsSize, Bool()))
  for(i <- 0 until rsSize){
    var rs1Ready = io.from_dr.availList(rsBuff(i).pf.prfSrc1)
    var rs2Ready = io.from_dr.availList(rsBuff(i).pf.prfSrc2)
    rsReadyList(i) := rs1Ready && rs2Ready && rsBuffValid(i)
    rsArbPacket(i).isValid := rsBuffValid(i)
    rsArbPacket(i).age := rsROBAge(i)
    rsArbPacket(i).srcReady := rsReadyList(i)
    rsArbPacket(i).isStore := isStore(i)
    rsArbPacket(i).rsIdx := i.U
  }
  val deqSelect = if(!StoreSeq){ 
    OldFirstArb(rsArbPacket, rsSize).map(_.rsIdx)
  }else{
    var oldfirstMem = OldFirstArb(rsArbPacket, rsSize)
    var oldfirstStore = OldStoreFirstArb(rsArbPacket, rsSize)
    def ageCompare(A: ArbAgeBundle, B: ArbAgeBundle): Bool = {
      Mux(getFlag(A.age) === getFlag(B.age), getIdx(A.age) < getIdx(B.age),
      getIdx(A.age) > getIdx(B.age))
    }

    //only support  mem deqwidth === 1
    Mux(!oldfirstStore(0).isStore,oldfirstMem,
      Mux(ageCompare(oldfirstMem(0),oldfirstStore(0)),oldfirstMem,oldfirstStore)).map(_.rsIdx)
  }

  val deqSelectIdx = Wire(Vec(deqWidth, UInt(log2Up(rsSize).W)))
  deqSelectIdx := deqSelect
  dontTouchUtil(deqSelectIdx)
  dontTouchUtil(rsArbPacket)
  for(i <- 0 until deqWidth){
    if(i == 0){
      io.out(i).valid := rsReadyList(deqSelect(i))
    }else{
      val isPriv = rsBuff(deqSelect(i)).cs.fuType === SDEF(FU_PRIV)
      io.out(i).valid := rsReadyList(deqSelect(i)) && !isPriv
      //Priv must to exeute pipe0
    }
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
  val isValid  = Bool()
  val isStore  = Bool()
  val age      = UInt(RobAgeWidth.W)
  val srcReady = Bool()
  val rsIdx    = UInt(log2Up(rsSize).W)
}