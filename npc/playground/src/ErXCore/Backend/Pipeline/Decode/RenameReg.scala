package ErXCore

import chisel3._
import chisel3.util._
import DecodeSignal._
//ref ZhouShan:https://github.com/OSCPU-Zhoushan/Zhoushan

//TODO: 代码复用是在太低了，考虑重写
//在Rename级检查，如果源操作数不需要，则重命名一个永远COMMIT的寄存器(0号寄存器)
class Rename extends ErXCoreModule{
  val io = IO(new Bundle {
    val in  = Vec(DecodeWidth,Flipped(new MicroOpIO))
    val out = Vec(DecodeWidth,new RenameIO)
    val from_ex = Input(new RenameFromExecuteUpdate(updSize = IssueWidth))
    val from_rob = Input(new RenameFromCommitUpdate(updSize = CommitWidth))
    val fw_dp = Output(new RSFromRename)
  })
  //Rename
  val notNeedSrc1 = Wire(Vec(DecodeWidth,Bool()))
  val notNeedSrc2 = Wire(Vec(DecodeWidth,Bool())) //TODO: check in.srcType
  val PrfStateTable = Module(new PrfStateTable)
  PrfStateTable.io.flush := io.from_rob.recover
  io.fw_dp.availList := PrfStateTable.io.availList.asTypeOf(io.fw_dp.availList)
  for(i <- 0 until DecodeWidth){
    notNeedSrc1(i) := io.in(i).cs.src1Type =/= SDEF(A_RS1)
    notNeedSrc2(i) := io.in(i).cs.src2Type =/= SDEF(B_RS2)
    PrfStateTable.io.rfWen(i) := io.in(i).cs.rfWen
    PrfStateTable.io.rfDst(i) := io.in(i).cs.rfDest
  }
  
  val RenameTable = Module(new RenameTable)

  for(i <- 0 until DecodeWidth){
    RenameTable.io.in.rfWen(i) := io.in(i).cs.rfWen
    RenameTable.io.in.rfSrc1(i) := Mux(!notNeedSrc1(i),io.in(i).cs.rfSrc1 ,0.U)
    RenameTable.io.in.rfSrc2(i) := Mux(!notNeedSrc2(i),io.in(i).cs.rfSrc2 ,0.U)
    RenameTable.io.in.rfDst(i)  := io.in(i).cs.rfDest
    RenameTable.io.in.prfDst(i) := PrfStateTable.io.prfDst(i)
  }
  
  for(i <- 0 until DecodeWidth){
    io.out(i).cs := io.in(i).cs
    io.out(i).cf := io.in(i).cf
    io.out(i).pf.pprfDst  := RenameTable.io.out.pprfDst(i)
    io.out(i).pf.prfSrc1  := RenameTable.io.out.prfSrc1(i)
    io.out(i).pf.prfSrc2  := RenameTable.io.out.prfSrc2(i)
    io.out(i).pf.prfDst   := PrfStateTable.io.prfDst(i)
    io.out(i).robIdx      := DontCare
  }
  //from execute
  for(i <- 0 until IssueWidth){ 
    PrfStateTable.io.from.executeUpdate(i) := Mux(io.from_ex.upd(i).wen,io.from_ex.upd(i).prfDst,0.U)
  }
  //from commit
  RenameTable.io.from_rob := io.from_rob
  PrfStateTable.io.from.commitRecover := io.from_rob.recover
  for(i <- 0 until CommitWidth){
    PrfStateTable.io.from.commitUpdate(i) := Mux(io.from_rob.upd(i).wen,io.from_rob.upd(i).prfDst,0.U)
    PrfStateTable.io.from.commitFree(i)   := Mux(io.from_rob.upd(i).wen,io.from_rob.upd(i).freePrfDst,0.U)
  }
}

class RenameTable extends ErXCoreModule{
  val io = IO(new Bundle {
    val in = new Bundle {
      val rfWen  = Input(Vec(DecodeWidth,Bool()))
      val rfSrc1 = Input(Vec(DecodeWidth,UInt(log2Up(ArfSize).W)))
      val rfSrc2 = Input(Vec(DecodeWidth,UInt(log2Up(ArfSize).W)))
      val rfDst  = Input(Vec(DecodeWidth,UInt(log2Up(ArfSize).W)))
      val prfDst = Input(Vec(DecodeWidth,UInt(log2Up(PrfSize).W)))
    }
    val out = new Bundle {
      val prfSrc1 = Output(Vec(DecodeWidth,UInt(log2Up(PrfSize).W)))
      val prfSrc2 = Output(Vec(DecodeWidth,UInt(log2Up(PrfSize).W)))
      val pprfDst = Output(Vec(DecodeWidth,UInt(log2Up(PrfSize).W)))
    }
    val from_rob = Input(new RenameFromCommitUpdate(updSize = CommitWidth))
  })
  val specTable = RegInit(VecInit(Seq.tabulate(ArfSize)(i => i.U(log2Up(PrfSize).W))))
  val archTable = RegInit(VecInit(Seq.tabulate(ArfSize)(i => i.U(log2Up(PrfSize).W))))
  //The physical register addresses 0-31 are assigned by default.
  /*NOTE:使用拓展prf进行寄存器重命名
  reset后specTable里存储着标记为COMMITED的物理寄存器映射，即为0-15号体系结构寄存器对应0-15号物理寄存器
  按照超标量处理器定义一个重命名的寄存器被释放即出现了第二条和与先前指令rfDest一样的指令
  因而，每次先取出先前重命名使用的地址用于释放，再向specTable写入最新重命名的地址。为了做区分，用于释放的地址为pprf，同时更新specTable对体系结构寄存器（rf）和prf物理寄存器的映射
  为什么不支持同时退休两条地址一样的指令。当两条地址一样时（待补充）
  */
  

  for(i <- 0 until DecodeWidth){
    io.out.prfSrc1(i) := specTable(io.in.rfSrc1(i))
    io.out.prfSrc2(i) := specTable(io.in.rfSrc2(i))
    io.out.pprfDst(i) := specTable(io.in.rfDst(i))
  }
  
  for {
    i <- 0 until DecodeWidth
    j <- i+1 until DecodeWidth
  } {
    when(io.in.rfWen(i)){
      //RAW
      when(io.in.rfDst(i).orR && io.in.rfDst(i) === io.in.rfSrc1(j)) {
        io.out.prfSrc1(j) := io.in.prfDst(i)
      }
      when(io.in.rfDst(i).orR && io.in.rfDst(i) === io.in.rfSrc2(j)) {
        io.out.prfSrc2(j) := io.in.prfDst(i)
      }
      //WAW
      when(io.in.rfDst(i).orR && io.in.rfDst(i) === io.in.rfDst(j)) {
        io.out.pprfDst(j) := io.in.prfDst(i)
      }
    }
  }


  when(!io.from_rob.recover){
    (0 until DecodeWidth).map(i => {
      when(io.in.rfWen(i) && io.in.rfDst(i).orR){
        specTable(io.in.rfDst(i)) := io.in.prfDst(i)
      }
    })
    (0 until CommitWidth).map(i => {
      when(io.from_rob.upd(i).wen && io.from_rob.upd(i).prfDst.orR){
        archTable(io.from_rob.upd(i).rfDst) := io.from_rob.upd(i).prfDst
      }
    })
  }.otherwise{
    specTable := archTable
  }

  // io.boreRNandPRF.archTable := 0.U.asTypeOf(io.boreRNandPRF.archTable)
  if(EnableVerlatorSim){
    ExcitingUtils.addSource(archTable,"archTable",ExcitingUtils.Func)
  }
}

class PrfStateTable extends ErXCoreModule{
  def FREE      = 0.U(2.W)
  def MAPPED    = 1.U(2.W)
  def EXECUTED  = 2.U(2.W)
  def COMMITTED = 3.U(2.W)
  val io = IO(new Bundle {
    val rfWen  = Input(Vec(DecodeWidth,Bool()))
    val rfDst  = Input(Vec(DecodeWidth,UInt(log2Up(ArfSize).W)))
    val flush  = Input(Bool())
    val prfDst = Output(Vec(DecodeWidth,UInt(log2Up(PrfSize).W)))
    val availList = Output(UInt(PrfSize.W))
    val from = Input(new Bundle {
      val executeUpdate = Input(Vec(IssueWidth ,UInt(log2Up(PrfSize).W)))
      val commitUpdate  = Input(Vec(CommitWidth,UInt(log2Up(PrfSize).W)))
      val commitFree    = Input(Vec(CommitWidth,UInt(log2Up(PrfSize).W)))
      val commitRecover = Input(Bool())
    })
  })
  val prfStateTable = RegInit(VecInit(Seq.fill(ArfSize)(COMMITTED) ++ Seq.fill(PrfSize - ArfSize)(FREE)))
  val freeList = Wire(Vec(DecodeWidth,UInt(PrfSize.W)))
  val freePrfDst = Wire(Vec(DecodeWidth,UInt(log2Up(PrfSize).W)))
  io.prfDst := freePrfDst
  freeList(0) := Cat(prfStateTable.map(_ === FREE).reverse)  
  for(i <- 0 until DecodeWidth){
    freePrfDst(i) := Mux(io.rfWen(i),PriorityEncoder(freeList(i).asBools),0.U)
    if(i < DecodeWidth - 1){
      freeList(i + 1) := freeList(i) & ~UIntToOH(freePrfDst(i),PrfSize)
    }
  }
  io.availList := (Cat(prfStateTable.map(_ === EXECUTED ).reverse) 
                 | Cat(prfStateTable.map(_ === COMMITTED).reverse))

  (0 until DecodeWidth).map(i => { when(io.rfDst(i).orR && io.rfWen(i)){
    prfStateTable(freePrfDst(i)) := MAPPED
  }})
  (0 until IssueWidth).map(i => { when(io.from.executeUpdate(i).orR){
    prfStateTable(io.from.executeUpdate(i)) := EXECUTED
  }})
  (0 until CommitWidth).map(i => { when(io.from.commitUpdate(i).orR){
    prfStateTable(io.from.commitUpdate(i)) := COMMITTED
  }})
  (0 until CommitWidth).map(i => { when(io.from.commitFree(i).orR){
    prfStateTable(io.from.commitFree(i)) := FREE
  }})

  for(i <- 0 until PrfSize){
    when(io.flush && prfStateTable(i) =/= COMMITTED){
      prfStateTable(i) := FREE
    }
  }

  prfStateTable(0) := COMMITTED

  // if (DecodeWidth > 1) {
  //   for (i <- (1 until DecodeWidth).reverse) {
  //     when(io.prfDst(i - 1) =/= io.prfDst(i)){
  //       assert(false.B)
  //     }
  //   }
  // }
}