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
    val fw_ex = Output(new RSFromRename)
  })
  //Rename
  val notNeedSrc1 = Wire(Vec(DecodeWidth,Bool()))
  val notNeedSrc2 = Wire(Vec(DecodeWidth,Bool())) //TODO: check in.srcType
  val PrfStateTable = Module(new PrfStateTable)
  io.fw_ex.availList := PrfStateTable.io.availList.asTypeOf(io.fw_ex.availList)
  for(i <- 0 until DecodeWidth){
    notNeedSrc1(i) := io.in(i).cs.src1Type =/= SDEF(A_RS1)
    notNeedSrc2(i) := io.in(i).cs.src2Type =/= SDEF(B_RS2)
    PrfStateTable.io.rfWen(i) := io.in(i).cs.rfWen
    PrfStateTable.io.rfDst(i) := io.in(i).cs.rfDest
  }
  
  val RenameTable = Module(new RenameTable)

  for(i <- 0 until DecodeWidth){
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
    io.out(i).pf.prfDst   := RenameTable.io.out.pprfDst(i)
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
      val rfSrc1 = Input(Vec(DecodeWidth,UInt(5.W)))
      val rfSrc2 = Input(Vec(DecodeWidth,UInt(5.W)))
      val rfDst  = Input(Vec(DecodeWidth,UInt(5.W)))
      val prfDst = Input(Vec(DecodeWidth,UInt(5.W)))
    }
    val out = new Bundle {
      val prfSrc1 = Output(Vec(DecodeWidth,UInt(5.W)))
      val prfSrc2 = Output(Vec(DecodeWidth,UInt(5.W)))
      val pprfDst = Output(Vec(DecodeWidth,UInt(5.W)))
    }
    val from_rob = Input(new RenameFromCommitUpdate(updSize = CommitWidth))
  })
  val specTable = RegInit(VecInit(Seq.tabulate(32)(i => i.U(log2Up(PrfSize).W))))
  val archTable = RegInit(VecInit(Seq.tabulate(32)(i => i.U(log2Up(PrfSize).W))))
  //The physical register addresses 0-31 are assigned by default.

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
      when(io.in.rfDst(i) =/= 0.U && io.in.rfDst(i) === io.in.rfSrc1(j)) {
        io.out.prfSrc1(j) := io.in.prfDst(i)
      }
      when(io.in.rfDst(i) =/= 0.U && io.in.rfDst(i) === io.in.rfSrc2(j)) {
        io.out.prfSrc2(j) := io.in.prfDst(i)
      }
      //WAW
      when(io.in.rfDst(i) =/= 0.U && io.in.rfDst(i) === io.in.rfDst(j)) {
        io.out.pprfDst(j) := io.in.prfDst(i)
      }
    }
  }


  when(!io.from_rob.recover){
    (0 until DecodeWidth).map(i => {
      when(io.in.rfWen(i)&&io.in.rfDst(i) =/= 0.U){
        specTable(io.in.rfDst(i)) := io.in.prfDst(i)
      }
    })
    (0 until CommitWidth).map(i => {
      when(io.from_rob.upd(i).wen&&io.from_rob.upd(i).prfDst =/= 0.U){
        archTable(io.from_rob.upd(i).rfDst) := io.from_rob.upd(i).prfDst
      }
    })
  }.otherwise{
    specTable := archTable
  }
}

class PrfStateTable extends ErXCoreModule{
  def FREE      = 0.U(2.W)
  def MAPPED    = 1.U(2.W)
  def EXECUTED  = 2.U(2.W)
  def COMMITTED = 3.U(2.W)
  val io = IO(new Bundle {
    val rfWen  = Input(Vec(DecodeWidth,Bool()))
    val rfDst  = Input(Vec(DecodeWidth,Bool()))
    val prfDst = Output(Vec(DecodeWidth,UInt(log2Up(PrfSize).W)))
    val availList = Output(UInt(PrfSize.W))
    val from = Input(new Bundle {
      val executeUpdate = Input(Vec(IssueWidth ,UInt(log2Up(PrfSize).W)))
      val commitUpdate  = Input(Vec(CommitWidth,UInt(log2Up(PrfSize).W)))
      val commitFree    = Input(Vec(CommitWidth,UInt(log2Up(PrfSize).W)))
      val commitRecover = Input(Bool())
    })
  })
  val prfStateTable = RegInit(VecInit(Seq.fill(32)(COMMITTED) ++ Seq.fill(PrfSize - 32)(FREE)))
  val freeList = Wire(Vec(DecodeWidth,UInt(PrfSize.W)))
  val freePrfDst = Wire(Vec(DecodeWidth,UInt(log2Up(PrfSize).W)))
  freeList(0) := Cat(prfStateTable.map(_ === FREE).reverse)  
  for(i <- 0 until DecodeWidth){
    freePrfDst(i) := Mux(io.rfWen(i),PriorityEncoder(freeList(i).asBools),0.U)
    if(i < DecodeWidth - 1){
      freeList(i + 1) := freeList(i) & ~UIntToOH(freePrfDst(i),PrfSize)
    }
  }
  io.availList := (Cat(prfStateTable.map(_ === EXECUTED ).reverse) 
                 | Cat(prfStateTable.map(_ === COMMITTED).reverse))

  (0 until DecodeWidth).map(i => { when(io.rfDst(i) =/= 0.U && io.rfWen(i)){
    prfStateTable(freePrfDst(i)) := MAPPED
  }})
  (0 until IssueWidth).map(i => { when(io.from.executeUpdate(i) =/= 0.U){
    prfStateTable(io.from.executeUpdate(i)) := EXECUTED
  }})
  (0 until CommitWidth).map(i => { when(io.from.commitUpdate(i) =/= 0.U){
    prfStateTable(io.from.commitUpdate(i)) := COMMITTED
  }})
  (0 until CommitWidth).map(i => { when(io.from.commitFree(i) =/= 0.U){
    prfStateTable(io.from.commitFree(i)) := FREE
  }})
  prfStateTable(0) := COMMITTED
}