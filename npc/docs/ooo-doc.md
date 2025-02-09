# 果壳的乱序实践



```
taskkill /f /im wslservice.exe
```

ysyx要求较低

简单做一个符合ysyx要求的双发乱序核当乱序入门了。

核：二象 ErX(iang)

先写后端

```
./build/emu -i ready-to-run/coremark-2-iteration.bin --diff ready-to-run/riscv64-nemu-interpreter-so --dump-wave -b 2000 -e 2271 2> perf.log
```



```
./build/emu -i ./ready-to-run/microbench.bin --diff ./ready-to-run/riscv64-nemu-interpreter-so -b 0 -e 0 --dump-wave-full
```



## 代码规范

来自其他地方的飞线旁路：
例如：

### rename级接收了来自ex的转发

是否使用缩写：**是**

rename接受方向

```
val from_ex = Input(new Bundle())
```

ex转发方向（fw即为forward）

```
val fw_dr =  
```

rob向commit的级的流水传递

```
val to_commit
```

流水级间的握手方式

chisel带来了一个命名选择困难症，例如，流水级间的信号传递可以这样传递

```scala
val in  = Vec(DecodeWidth,Flipped(Decoupled(new InstIO)))
val in  = Flipped(Decoupled(Vec(DecodeWidth,new InstIO)))
val in  = Input(new InstIO)重写IO
```

如此多的传输方式，给流水级间传递带来了困难，另一方面需要传输不同的数据，这也带来了困难、

因此，需要统一一个传输标准

经过思考，使用这样的传输方式，并且在传输时的Bundle里指示valid信号。

```scala
val in  = Flipped(Decoupled(Vec(DecodeWidth,new InstIO)))
约定：
val io.out.ready := fu.in.ready
val io.out.valid := io.in.bits.map(_.valid).reduce(_||_)
```

这样也带来一个问题，bundle与bundle间的链接往往通过Bundle直接链接。这样valid有可能带来赋值困难的问题

因此，只有一个选择，即

```
val in  = Vec(DecodeWidth,Flipped(Decoupled(new InstIO)))
约定：

```



前递的转发信号，原因，转发时有些控制类型信号有唯一性，有些信号是为了更新状态，因此，既需要区分，也需要选择

```
val in  = Input(new bundleFrom())
class BundleFrom(updSize: UInt) extends Bundle{
	val recover = 
	val flush = 
	val upd = Vec(updSizes,new Bundle{
	
	})
}
```



## Frontend

使用现代分支预测器，预测器解耦

## Backend

使用ROB进行隐式寄存器重命名[书198，322]



使用拓展prf进行寄存器重命名

reset后specTable里存储着标记为COMMITED的物理寄存器映射，即为0-15号体系结构寄存器对应0-15号物理寄存器

 按照超标量处理器定义一个重命名的寄存器被释放即出现了第二条和与先前指令rfDest一样的指令

  因而，每次先取出先前重命名使用的地址用于释放，再向specTable写入最新重命名的地址。为了做区分，用于释放的地址为pprf，同时更新specTable对体系结构寄存器（rf）和prf物理寄存器的映射

为什么不支持同时退休两条地址一样的指令。当两条地址一样时（待补充）

## 理想的Backend设计

显示寄存器rename、非数据捕捉结构流水线、非压缩式分布式保留站（如果保留站过大，考虑并行分配设计，选择设计）、唤醒旁路（有点复杂）、非压缩的发射队列（可以考虑设计使用ROB的年龄信息作为优先仲裁）

### 显示重命名与隐式重命名

显式重命名方案参考：[NOP-Processor](https://github.com/NOP-Processor/NOP-Core/tree/master)，[RISCV-BOOM](https://docs.boom-core.org/en/latest/)。的基本结构是：map_table 记录逻辑寄存器与物理寄存器之间的映射关系；free_list记录物理寄存器的空闲状态；busy_table 记录寄存器是否可读。显式重命名方案中 ROB 不记录指令的结果，即将提交的数据和处于推测状态的数据都保存在物理寄存器中，因此物理寄存器数目要高于逻辑寄存器数目。

个人理解：显示重命名创建了一个关于逻辑寄存器与物理寄存器的映射，即逻辑寄存器并不存在，而是通过映射获取

隐式重命名方案参考：[NutShell](https://github.com/OSCPU/NutShell)。采用隐式重命名方案时，ROB (Recorder Buffer) 保存正在执行、尚未提交的指令的结果；ARF(ISA Register File) 保存已经提交的指令中即将写入寄存器中的值。隐式重命名方案中 ARF 只保存已经提交的指令的值，处于 “推测” 状态的指令的值由 ROB 保存，因此需要的物理寄存器数量与逻辑寄存器数量相同。隐式重命名方案还需要建立一个映射表，记录操作数在 ROB 中的位置。由于流水线中后续指令与已经提交的指令可能有相同的目的寄存器(意味着该寄存器将被修改)，映射表需要增加一个表项，记录对应寄存器的最新值保存在 ROB 还是 ARF 中，这一设计为实现数据前馈、消除 RAW 冲突创造了条件。隐式重命名方案不需要 free_list 来记录物理寄存器状态，指令被写进 ROB 即完成重命名。相比于显式重命名，隐式重命名需要的物理寄存器数目更少，但每个操作数在其生命周期中需要保存在 ROB 和 ARF 两个位置，读取数据的复杂度较高、功耗更高。

![img](./typora-user-images/果壳的乱序实践/v2-3256218b1b0a7fca7cec0042d4ed98f3_1440w.jpg)

[^]: 显式重命名(左)与隐式重命名(右)

### 保留站的设计

将多个指令的保留站合并为一个保留站，例如在rv32i中，无需设计agu，bru等保留站，合并为intRS保留站。对于mem设计为memRS保留站。这样可以让指令仲裁选择的效率提升到最大。

#### IntRS

支持乱序发射，乱序发射时会根据指令顺序扫描那些指令ready，仲裁选出来最适合发射的指令推送到执行单元中。可选的操作：基于压缩队列优先级的乱序发射。没有优先级，直接从0开始扫描ready的乱序发射。基于rob年龄优先级的乱序发射。

#### MemRS

##### 思考

乱序处理器如何维护TLB的cache，uncache模式。如果指令顺序如下

```
st1 int csr(uncache) int int int int st2
```

进入保留站后

```
IntRS int csr int int
MemRS st1 st2
```

3宽度执行单元可能执行的乱序形式：

```scla
int int csr 
int int int

st1 st2
```

如果csr先发生了修改，可能会导致因为csr修改导致的st指令错误开启uncache状态等。



方案1：

因此要维护一致性，在csr指令退休时，再对csr进行修改！！！由于此时store的数据还在storeQueue中，根据最新的内容读取。

如果不刷，那么要对csr的数据做转发处理，否则指令的执行会读到错误的数据

方案2：

每当有一条csr退休时，直接刷掉整个流水线。

考虑到csr指令不是很多，可以考虑方案2

##### 维护完全顺序执行：

略

##### 维护部分乱序执行：

此时store指令依然按照程序指定的顺序执行，但是处于两条store中的load可以乱序执行

方案1：基于压缩队列的维护部分乱序执行，由于压缩队列的特性是从0-n指令的程度是由旧到新。因此可以每次维护一个storemask，storemask用于指示当前指令能否被派遣，例如 zhoushan

方案2：基于robAge维护部分乱序执行，robAge的特性是哪有空间向那派遣，发射的时候根据robAge和store维护指令派遣顺序。方式：根据robAge和isStore决定那些的指令可以派遣

递归仲裁主要考虑三个要素

| robAge  | isStore         | instRdy      |
| ------- | --------------- | ------------ |
| rob年龄 | 是否为store指令 | 指令是否就绪 |

针对以下情况的判断，假设age越小指令越旧

没有isStore的情况下

| >(younger) | young - instRdy | old - instRdy | 判断结果 |
| ---------- | --------------- | ------------- | -------- |
| young>old  | 0               | 0             | old      |
| young>old  | 0               | 1             | old      |
| young>old  | 1               | 0             | young    |
| young>old  | 1               | 1             | old      |



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

1. 【腾讯文档】年龄仲裁器 https://docs.qq.com/sheet/DRVBmbnBTcm1tWUhK?tab=BB08J2
2. | 条件分类            | 关键条件                                                     | 选择结果 |
   | :------------------ | :----------------------------------------------------------- | :------- |
   | **young 无效**      | `y_valid = 0`                                                | old      |
   | **young 有效**      |                                                              |          |
   | 1. old 无效         | `y_valid = 1` 且 `o_valid = 0`                               | young    |
   | 2. old 有效但未就绪 | `y_valid = 1` 且 `o_valid = 1` 且 `o_rdy = 0`                | young    |
   | 3. old 有效且就绪   | `y_valid = 1` 且 `o_valid = 1` 且 `o_rdy = 1` 且 `y_store = 1` | old      |
   | 4. old 有效且就绪   | `y_valid = 1` 且 `o_valid = 1` 且 `o_rdy = 1` 且 `y_store = 0` | young    |

   ------

   #### **逻辑表达式**

   最终的发射选择逻辑可以简化为：

   verilog

   复制

   ```
   select_young = y_valid & (
       ~o_valid |                // old 无效
       ~o_rdy |                  // old 未就绪
       (~y_store & o_rdy)        // young 非存储指令且 old 就绪
   );
   ```

   ------

   ### **结论**

   1. **young 无效时**:
      - 直接选择 old 条目。
   2. **young 有效时**:
      - **优先选择 young** 的三种情况：
        - old 无效（`o_valid = 0`）。
        - old 有效但未就绪（`o_rdy = 0`）。
        - old 有效且就绪，但 young 是非存储指令（`y_store = 0`）。
      - **仅当以下条件时选择 old**：
        - old 有效且就绪（`o_rdy = 1`），且 young 是存储指令（`y_store = 1`）。
   3. **设计意图**:
      - **存储指令顺序性**: 当 young 是存储指令时，需等待更早的 old 存储指令完成（隐含 old 是存储指令时的优先级）。
      - **就绪优先**: 未就绪的 old 指令不会阻塞 young 指令的发射。
      - **年龄优先级**: 在非存储指令场景下，young 的优先级高于 old（隐含 FIFO 策略）。



以下是以前的思考，已废弃

例如存在以下队列

```
robAge	 8 7 1 2 0 5 6 4 3
isStore  1 0 1 0 0 1 0 0 1
mask     0 0 0 0 1 0 0 0 0
```

但是，在rtl中进行快排其实不是一个很简单的方法

一种方法是像nutShell一样，设计一个**`priorityMask` **

```scala
val priorityMask = RegInit(VecInit(Seq.fill(rsSize)(VecInit(Seq.fill(rsSize)(false.B)))))
```

- 这是一个二维向量，大小为 `rsSize x rsSize`（每个保留站条目对其他条目的依赖掩码）。
- `priorityMask(i)(j) = true.B` 表示条目 `i` 必须在条目 `j` **之前** 执行（条目 `j` 需等待 `i` 完成）。

- **核心作用**:
  - **维护指令间的依赖顺序**，确保指令按正确顺序发射。
  - 实现 **FIFO（先进先出）** 或 **优先级调度** 的机制。

当一条指令被派遣后时：

```scala
when(io.out.fire) {
  (0 until rsSize).map(i => priorityMask(i)(dequeueSelect) := false.B)
}
```

当EnableOutOfOrderMemAccess = false 时，判断当前指令是否有别的指令依赖来选择出最优的指令

EnableOutOfOrderMemAccess = true 时。

~~如果当前指令是store指令，并且当前指令没有依赖项，并且当前指令已经准备好时，可以~~

```scala
if(EnableOutOfOrderMemAccess){
  val dequeueSelectVec = VecInit(List.tabulate(rsSize)(i => {
    valid(i) &&
    Mux(
      needStore(i),
      !(priorityMask(i).asUInt.orR), // there is no other inst ahead
      true.B
    ) &&
    !(priorityMask(i).asUInt & instRdy.asUInt).orR & instRdy(i)
  }))
  dequeueSelect := OHToUInt(dequeueSelectVec)
  io.out.valid := instRdy(dequeueSelect) && dequeueSelectVec(dequeueSelect)
}else{
  dequeueSelect := OHToUInt(List.tabulate(rsSize)(i => {
    !priorityMask(i).asUInt.orR && valid(i)
  }))
  io.out.valid := instRdy(dequeueSelect) && !priorityMask(dequeueSelect).asUInt.orR && valid(dequeueSelect)
}
```



## 如何教别人乱序入门

我本身学习的时候，遇到了很多难以理解的问题，在这个背景下，姚永彬的超标量处理器设计成为了我的百科全书，当我遇到类似书上的结构时，我会查询书籍加深自己的印象。因此，我认为在编写超标量相关的文档时，在阐述自己设计时，同时应该标注在书那里能找到这样的设计

# NutShell乱序阅读

果壳是基于ROB进行寄存器重命名，ROB大小width(2) * size(16) = 32

暂时不读，分支预测恢复，store，前端

## Backend

### Dispatch

```scala
  val mduCnt = Wire(UInt(2.W))
  val lsuCnt = Wire(UInt(2.W))
  val bruCnt = Wire(UInt(2.W))
  val csrCnt = Wire(UInt(2.W))
```

统计这次in里mdu,lsu,bru,csr的个数

```scala
  List.tabulate(DispatchWidth)(i => {
    inst(i).decode := io.in(i).bits
    inst(i).prfDest := Cat(rob.io.index, i.U(1.W))
    inst(i).prfSrc1 := rob.io.aprf(2*i)
    inst(i).prfSrc2 := rob.io.aprf(2*i+1)
    inst(i).src1Rdy := !rob.io.rvalid(2*i) || rob.io.rcommited(2*i)
    inst(i).src2Rdy := !rob.io.rvalid(2*i+1) || rob.io.rcommited(2*i+1)
    inst(i).decode.data.src1 := rf.read(rfSrc(2*i))
    when(rob.io.rvalid(2*i) && rob.io.rcommited(2*i)){inst(i).decode.data.src1 := rob.io.rprf(2*i)}
    inst(i).decode.data.src2 := rf.read(rfSrc(2*i + 1))
    when(rob.io.rvalid(2*i+1) && rob.io.rcommited(2*i+1)){inst(i).decode.data.src2 := rob.io.rprf(2*i+1)}
  })
```

逻辑寄存器（体系结构寄存器）

**instCango**

dispatch的能否输入也依赖instCango

instCango进行如下检查

- 检查指令是否有效，rob是否能装下
- 当前的保留站是否ready

```scala
  List.tabulate(DispatchWidth)(i => {
    rob.io.in(i).valid := io.in(i).valid && instCango(i)
    io.in(i).ready := rob.io.in(i).ready && instCango(i)
    rob.io.in(i).bits := io.in(i).bits
  })

  instCango(0) :=
    io.in(0).valid &&
    rob.io.in(0).ready && // rob has empty slot
    !(hasBlockInst(0) && !pipeLineEmpty) &&
    !blockReg &&
    !mispredictionRecovery &&
    LookupTree(io.in(0).bits.ctrl.fuType, List(
      FuType.bru -> brurs.io.in.ready,
      FuType.alu -> alu1rs.io.in.ready,
      FuType.lsu -> lsurs.io.in.ready,
      FuType.mdu -> mdurs.io.in.ready,
      FuType.csr -> csrrs.io.in.ready,
      FuType.mou -> csrrs.io.in.ready
    ))
  instCango(1) :=
    instCango(0) &&
    io.in(1).valid &&
    rob.io.in(1).ready && // rob has empty slot
    !hasBlockInst(0) && // there is no block inst
    !hasBlockInst(1) &&
    !blockReg &&
    !mispredictionRecovery &&
    LookupTree(io.in(1).bits.ctrl.fuType, List(
      FuType.bru -> (brurs.io.in.ready && (bruCnt < 2.U)),
      FuType.alu -> (alu2rs.io.in.ready),
      FuType.lsu -> (lsurs.io.in.ready && (lsuCnt < 2.U)),
      FuType.mdu -> (mdurs.io.in.ready && (mduCnt < 2.U)),
      FuType.csr -> (csrrs.io.in.ready && (csrCnt < 2.U)),
      FuType.mou -> (csrrs.io.in.ready && (csrCnt < 2.U))
    ))
```

**blockReg**

用于停止流水线指令执行

### RS(保留站)

分布式保留站

集中式：记分牌算法

分布式：Tomasulo

NutShell提供了分布式保留站module，可以通过此module快速创建多个module

```scala
  val brurs  = Module(new RS(priority = true, size = checkpointSize, checkpoint = true, name = "BRURS"))
  val alu1rs = Module(new RS(priority = true, size = 4, name = "ALU1RS"))
  val alu2rs = Module(new RS(priority = true, size = 4, name = "ALU2RS"))
  val csrrs  = Module(new RS(priority = true, size = 1, name = "CSRRS")) // CSR & MOU
  val lsurs  = Module(new RS(storeSeq = true, size = 4, name = "LSURS")) // FIXIT: out of order l/s disabled
  val mdurs  = Module(new RS(priority = true, size = 4, pipelined = false, name = "MDURS"))
```

保留站直接接受RenamedDecodeIO的输入，即为backend进入的译码的数据（一下进入这个多东西而不挑选，面积不会浪费么）

后端设计了一个公共数据总线，每个保留站size不一样，保留站内的每一个有效项都会监听公共总线的数据（物理寄存器的idx），如果公共总线数据提交完成，则保留站内的数据准备好

```scala
  val instRdy = WireInit(VecInit(List.tabulate(rsSize)(i => src1Rdy(i) && src2Rdy(i) && valid(i))))
  List.tabulate(rsSize)(i =>
    when(valid(i)){
      List.tabulate(rsCommitWidth)(j =>
        when(!src1Rdy(i) && prfSrc1(i) === io.cdb(j).bits.prfidx && io.cdb(j).valid){
            src1Rdy(i) := true.B
            src1(i) := io.cdb(j).bits.commits
        }
      )
      List.tabulate(rsCommitWidth)(j =>
        when(!src2Rdy(i) && prfSrc2(i) === io.cdb(j).bits.prfidx && io.cdb(j).valid){
            src2Rdy(i) := true.B
            src2(i) := io.cdb(j).bits.commits
        }
      )
      // Update RS according to brMask
      // If a branch inst is canceled by BRU, the following insts with subject to this branch should also be canceled
      brMask(i) := updateBrMask(brMask(i))
      when(needMispredictionRecovery(brMask(i))){ valid(i):= false.B }
    }
  )
```

srcRdy为reg型，因此对保留站的ready进行了持久化设计

保留站如果遇到flush，清空该保留站里的所有valid

#### 保留站的入队设计

```scala
  val emptySlot = ~valid.asUInt
  val enqueueSelect = PriorityEncoder(emptySlot) // TODO: replace PriorityEncoder with other logic 
  valid(enqueueSelect) := true.B
  when(io.in.fire){
    decode(enqueueSelect) := io.in.bits
    valid(enqueueSelect) := true.B
    prfSrc1(enqueueSelect) := io.in.bits.prfSrc1
    prfSrc2(enqueueSelect) := io.in.bits.prfSrc2
    src1Rdy(enqueueSelect) := io.in.bits.src1Rdy
    src2Rdy(enqueueSelect) := io.in.bits.src2Rdy
    src1(enqueueSelect) := io.in.bits.decode.data.src1
    src2(enqueueSelect) := io.in.bits.decode.data.src2
    brMask(enqueueSelect) := io.in.bits.brMask
  }
```

NutShell的保留站策略是非压缩发射队列

好写法，这样的写法可以让保留站的入队往空的地方写

#### 保留站的出队设计

保留站的出队监听列表里那些指令的指令rdy从而使其出队

```scala
  val dequeueSelect = Wire(UInt(log2Up(size).W))
  dequeueSelect := PriorityEncoder(instRdy)
  when(io.out.fire || forceDequeue){
    if(!checkpoint){
      valid(dequeueSelect) := false.B
    }
  }

  io.out.valid := rsReadygo // && validNext(dequeueSelect)
  io.out.bits := decode(dequeueSelect)
  io.out.bits.brMask := brMask(dequeueSelect)
  io.out.bits.decode.data.src1 := src1(dequeueSelect)
  io.out.bits.decode.data.src2 := src2(dequeueSelect)
```

#### 保留站的参数化定制

默认参数

```scala
class RS(size: Int = 2, pipelined: Boolean = true, fifo: Boolean = false, priority: Boolean = false, checkpoint: Boolean = false, storeBarrier: Boolean = false, storeSeq: Boolean = false, name: String = "unnamedRS") extends NutCoreModule
```

fifo，priority，storeBarrier，storeSeq用不上，暂不研究

##### 非流水化保留站

非流水化保留站的特点是会保持当前流水站的输出直到io.commit通知当前参数运算结束时释放（用途，mdu组件的运算过程依赖操作数保持n个周期直到mdu运行结束释放保留站保留的这个数据）

原文对此的解释

  // fix unpipelined
  // when `pipelined` === false, RS helps unpipelined FU to store its uop for commit
  // if an unpipelined fu can store uop itself, set `pipelined` to true (it behaves just like a pipelined FU)

##### checkpoint保留站

大概意思是开启这个选项后，当bru指令进入保留站后，要进行checkpoint便于快速恢复现场

### rob

#### rob入队 

入队内容：

- 前端发来的Decode的数据
- brMask
- Valid当前指令是否有效
- 是load还是store指令
- 设置commited，canceled，redirect，exception状态为false.B
- 如果检测到写使能为1，并且写入目标寄存器不为0，分配rmtMap(也写入checkpoint便于分支预测失败恢复现场)

后端与前端的交互：当rob为空时，后端处于ready状态，可以输入数据

当rob输入flash时，清空rob内容和表

#### rob writeback

在指令写回`writeback`阶段写入重命名寄存器



rob总线也会对cdb总线进行监听，当发现cdb总线上的值有效时。通过计算出对应位置的索引，并设置对应索引位置的状态。

//TODO: valid更新时机

例如commit项：

- 当这条指令通过FU计算完毕后且没有发生例外，即可以在rob设置为commited（通过在rob的size和rob的width中获取）

```scala
    val prfidx = io.cdb(k).bits.prfidx
    val index = prfidx(log2Up(robSize), log2Up(robWidth))
    val bank = prfidx(log2Up(robWidth)-1, 0)
    when(io.cdb(k).valid){
      // Mark an ROB term as commited
      when(!io.cdb(k).bits.exception){
        commited(index)(bank) := true.B
      }
      // Write result to ROB-PRF
      prf(prfidx) := io.cdb(k).bits.commits
      // Write other info which could be generated by function units
      isMMIO(index)(bank) := io.cdb(k).bits.isMMIO
      intrNO(index)(bank) := io.cdb(k).bits.intrNO
      // Write redirect info
      redirect(index)(bank) := io.cdb(k).bits.decode.cf.redirect
      redirect(index)(bank).valid := io.cdb(k).bits.decode.cf.redirect.valid
      exception(index)(bank) := io.cdb(k).bits.exception
      // Update wen
      // In several cases, FU will invalidate rfWen
      store(index)(bank) := io.cdb(k).bits.store
      decode(index)(bank).ctrl.rfWen := io.cdb(k).bits.decode.ctrl.rfWen
    }
```

### Retire退休

 *// ROB Retire*

 *// We write back at most #bank reg results back to arch-rf.*

 *// Then we mark those ROB terms as finished, i.e. `!valid`*

 *// No more than robWidth insts can retire from ROB in a single cycl*

val tailBankNotUsed 确保了ROB里的最后一项没有被使用或者使用了已被提交

val tailTermEmpty 确保了ROB里最后两条指令里边均没有被使用

val retireATerm 确保了rob width里的有的项被使用

val recycleATerm 确保了rob里后两项有效且rob表项不为空

经过ZhouShan的阅读，对于这里有了新的理解，这里确保了ROB的顺序下的两条指令都已准备完成。

#### 体系结构寄存写回

当rob width里的有的项被使用且rf有使能时，将prf的内容搬到体系结构寄存器中

### Issue发射

NutShell使用了数据捕捉+寄存器隐式重命名的策略，即发射前进行读取原操作数的值，并通过旁路网络（公共数据总线）监听结果更新内容。

发射过程即为在保留站中优先选择已准备好的指令，如果准备好，即进行发射。指令发射后，将指令送到对应的fu中，对应fu计算然后，标记当前的计算结果是否有效，送到写回阶段。

### WriteBack写回

```scala
  val (srcBRU, srcALU1, srcALU2, srcLSU, srcMDU, srcCSR, srcMOU, srcNone) = (0, 1, 2, 3, 4, 5, 6, 7)
  val commit = List(brucommitdelayed, alu1.io.out.bits, alu2.io.out.bits, lsucommit, mducommitdelayed, csrcommit, moucommit, nullCommit)
  val commitValid = List(bruDelayer.io.out.valid, alu1.io.out.valid, alu2.io.out.valid, lsu.io.out.valid, mduDelayer.io.out.valid, csr.io.out.valid, mou.io.out.valid, false.B)

  val WritebackPriority = Seq(
    srcCSR,
    srcMOU,
    srcLSU,
    srcMDU,
    srcBRU,
    srcALU1,
    srcALU2,
    srcNone
  )

  // select 2 CDB commit request with highest priority
  val commitPriority = VecInit(WritebackPriority.map(i => commit(i)))
  val commitValidPriority = VecInit(WritebackPriority.map(i => commitValid(i)))
```

写回队列维护了一个优先级，根据优先级读出来索引填充到CommitPriority中

每次回写的数据会根据局优先级进行广播（同时广播两条数据），以便唤醒其他队列中的内容。

```scala
  val cdbSrc1 = PriorityMux(commitValidPriority, commitPriority)
  val cdbSrc1Valid = PriorityMux(commitValidPriority, commitValidPriority)
  val cdbSrc2 = PriorityMux(secondCommitValid, commitPriority)
  val cdbSrc2Valid = PriorityMux(secondCommitValid, commitValidPriority)

  cdb(0).valid := cdbSrc1Valid
  cdb(0).bits := cdbSrc1

  cdb(1).valid := cdbSrc2Valid
  cdb(1).bits := cdbSrc2
```

于此同时

```scala
  val notFirstMask = Wire(Vec(WritebackPriority.size, Bool()))
  notFirstMask(0) := false.B
  for(i <- 0 until WritebackPriority.size){
    if(i != 0){notFirstMask(i) := notFirstMask(i-1) | commitValidPriority(i-1)}
  }//NOTE:commitValidPriority: 1101000 -> notFirstMask: 1110000
  val secondCommitValid = commitValidPriority.asUInt & notFirstMask.asUInt
  // NOTE:notFirstMask(1110000) & 1101000 -> 1100000
  val notSecondMask = Wire(Vec(WritebackPriority.size, Bool()))
  notSecondMask(0) := false.B
  for(i <- 0 until WritebackPriority.size){
    if(i != 0){notSecondMask(i) := notSecondMask(i-1) | secondCommitValid(i-1)}
  }
  val commitValidVec = commitValidPriority.asUInt & ~notSecondMask.asUInt
  //(notsecondMask)1000000 -> (~notsecondMask)0111111 -> (commitValidPriority)1101000 & 0111111 -> 0101000
```

这一步的目的是，当同时超过发射队列数的指令就绪，通过这一步仲裁，选择出最优先的两条有效指令作为commitValidVec

当数据被选中，设置ready状态，即影响到各大fu的准备，各大fu ready后，对应的保留站即可给予各大fu操作数

```scala
  mduWritebackReady  := commitValidVec(WritebackPriority.indexOf(srcMDU))
  bruWritebackReady  := commitValidVec(WritebackPriority.indexOf(srcBRU))
  alu1.io.out.ready := commitValidVec(WritebackPriority.indexOf(srcALU1))
  alu2.io.out.ready := commitValidVec(WritebackPriority.indexOf(srcALU))

  brurs.io.out.ready  := bru.io.in.ready
  alu1rs.io.out.ready := alu1.io.in.ready
  alu2rs.io.out.ready := alu2.io.in.ready
  csrrs.io.out.ready := csr.io.in.ready
  lsurs.io.out.ready := lsu.io.in.ready
  mdurs.io.out.ready := mdu.io.in.ready
```

？然后我就水灵灵的读完了backend的scala代码？我应该还漏了哪些细节

### cdb公共数据总线

指令在保留站监听未就绪的源操作数, 等待到所有源操作数就绪, 且功能单元(FU)允许指令进入后将指令发射到功能单元. 功能单元在完成指令指示的操作后将结果写回到公共数据总线(CDB)上. 公共数据总线上的结果会被广播到各保留站及重排序缓冲(ROB). 此后, 指令在ROB中等待前导指令全部提交完毕后进入提交流程. 提交操作使这条指令根据其结果对处理器状态产生不可逆的影响. 提交操作后, 流水线不再跟踪这条指令, 指令的执行彻底结束。

各个保留站通过监听cdb公共数据总线得到数据是否ready（payloadRAM）。保留站收到结果后，会通过公共数据总线广播结果来让其他fu获取数据准备。

#### 遇到load/store时

 

# ZhouShan乱序阅读

项目地址：[OSCPU-Zhoushan/Zhoushan: Open Source Chip Project by University (OSCPU) - Zhoushan Core](https://github.com/OSCPU-Zhoushan/Zhoushan)

码风，看着很好，其实有些地方槽点还是很多的·、

比如MicroOp的流水级互联，百思不得其解为什么一些信号没用上还能正常连线，最后发现val uop = WireInit(0.U.asTypeOf(new MicroOp))发生了一个初始化并且后边没有赋值就让他传下去，这也太坑了

## Backend

### Rename

ZhouShan的重命名使用了基于PRF的显示重命名。重命名映射表使用了基于SRAM（sRAT）的重命名映射表，Rename阶段至维护了寄存器的地址，没有对寄存器的值进行实质维护。

zhoushan维护了一个PrfStateTable表，在rename阶段当io.out.ready && io.in.valid握手成功时，至高val en。

```scala
  val en = io.out.ready && io.in.valid
  val pst = Module(new PrfStateTable)
  pst.io.en := en
  for (i <- 0 until DecodeWidth) {
    pst.io.rd_req(i) := in_uop(i).valid && in_uop(i).rd_en
  }
```

同时，Rename级接收了外部信号，起到维护PrfStateTable与RenameTable功能

Rename级有个没考虑的问题，当指令不需要源操作数时，可能会产生无意义的源操数等待，因此当遇到这种指令时需要向其分配0号寄存器

#### PrfStateTable

PrfStateTable维护了一个物理寄存器状态表

val table初始化了一个表，其中表里有不同的状态。ZhouShan默认64个prf，初始化时，32个设为COMMITTED状态，另外32个设为FREE状态

```scala
// ref: Weiwu Hu. Computer Architecture (2nd ed). THU Press. (page 136)
trait PrfStateConstant {
  val FREE      = 0.asUInt(2.W)
  val MAPPED    = 1.asUInt(2.W)
  val EXECUTED  = 2.asUInt(2.W)
  val COMMITTED = 3.asUInt(2.W)
}
```

free_list维护了当前处于free状态的寄存器的个数，当可以free时，io.allocatable对外展现至高状态 

```scala
val free_list = Cat(table.map(_ === FREE).reverse)
val free_count = PopCount(free_list)
io.allocatable := (free_count >= DecodeWidth.U)
```

avail_list维护了当前正在使用的prf

```scala
val avail_list = Cat(table.map(_ === EXECUTED).reverse) | Cat(table.map(_ === COMMITTED).reverse)
io.avail_list := avail_list
```

同理，因为Decode宽度为2，所以每次都要分配两个寄存器编号，因为每次从PriorityEncoder选择器中选择两个rd_paddr，作为被解码分配的物理寄存器地址，同时，被分配的寄存器标记为MAPPED

```scala
  val fl0 = free_list
  io.rd_paddr(0) := Mux(io.en && io.rd_req(0), PriorityEncoder(fl0), 0.U)

  val fl1 = fl0 & ~UIntToOH(io.rd_paddr(0), PrfSize)
  io.rd_paddr(1) := Mux(io.en && io.rd_req(1), PriorityEncoder(fl1), 0.U)
  for (i <- 0 until DecodeWidth) {
    when (io.en && io.rd_req(i)) {
      table(io.rd_paddr(i)) := MAPPED
    }
  }
```

PrfStateTable也维护了以下输入，其中exe代表执行时，会将table对应的地址分配为EXECUTED状态，cm和free分别会分配为COMMITTED和FREE状态

//TODO:cm_recover

```
    // update prf state
    val exe = Vec(IssueWidth, Input(UInt(log2Up(PrfSize).W)))
    val cm = Vec(CommitWidth, Input(UInt(log2Up(PrfSize).W)))
    val free = Vec(CommitWidth, Input(UInt(log2Up(PrfSize).W)))
    val cm_recover = Input(Bool())

    // default
    table(0) := COMMITTED
```

因此，可以这么说，Rename级除了负责分配prf，也要根据来自cm，exe等级的请求及时修改自身的状态。

#### RenameTable

ZhouShan的Rename

里分别设定了arch_table（用于difftest）与spec_table，table为32项，存储着prf的长度

rename级通过spec_table转化出对应的paddr（物理地址）以及目标地址的paddr（物理地址）
同时RenameTable还进行了WAW和RAW检查。

一般情况下，RenameTable输出映射的物理寄存器地址，当发生RAW，WAW时分配物理寄存器为同一地址，此时ppaddr(1)为rd最新的重命名地址

```scala
  for (i <- 0 until DecodeWidth) {
    io.rs1_paddr(i) := spec_table(io.in(i).rs1_addr)
    io.rs2_paddr(i) := spec_table(io.in(i).rs2_addr)
    io.rd_ppaddr(i) := spec_table(io.rd_addr(i))
  }

  // in-group RAW dependency check
  // todo: currently only support 2-way rename
  when ((io.in(1).rs1_addr === io.rd_addr(0)) && (io.rd_addr(0) =/= 0.U)) {
    io.rs1_paddr(1) := io.rd_paddr(0)
  }
  when ((io.in(1).rs2_addr === io.rd_addr(0)) && (io.rd_addr(0) =/= 0.U)) {
    io.rs2_paddr(1) := io.rd_paddr(0)
  }
  // in-group WAW dependency check
  when ((io.in(1).rd_addr === io.rd_addr(0)) && (io.rd_addr(0) =/= 0.U)) {
    io.rd_ppaddr(1) := io.rd_paddr(0)
  }
```

在decode阶段中，会将来自paddr的重命名结果更新至spec_table中

在commit时，会向体系结构寄存器更新结果，以便checkpoint恢复

```scala
    // be careful with WAW dependency here
    for (i <- 0 until DecodeWidth) {
      when (io.rd_addr(i) =/= 0.U && io.en) {
        spec_table(io.rd_addr(i)) := io.rd_paddr(i)
      }
    }
    for (i <- 0 until CommitWidth) {
      when (io.cm_rd_addr(i) =/= 0.U) {
        arch_table(io.cm_rd_addr(i)) := io.cm_rd_paddr(i)
      }
    }
```

因此，在Rename阶段PrfStateTable掌管物理寄存器状态，当需要寄存器重命名时，由prf优先对rd_addr进行重命名，同时在rd_addr有效的情况下，向spec_table更新命名的寄存器。

同时RenameTable会对重命名的寄存器进行检查waw和raw的依赖，给出最新分配的重命名的寄存器，由此，寄存器重命名结束。

此外，当cm_recover时（即分支预测失败时）即将体系结构寄存器（记录提交时的寄存器，保证了正确性）里的东西搬到spec_table中

### Issue

ZhouShan的寄存器重命名使用了基于PRF的显示寄存器重命名，因此对应的发射过程使用了非数据捕捉结构的流水线

采用了非压缩队列的设计，同时，为了保证性能，使用rob的年龄信息作为指令的优先选择条件

issue级对ROB和IssueUnit进行了初始化

```scala

  val stall_reg = Module(new StallRegister)
  stall_reg.io.in <> rename.io.out
  stall_reg.io.flush := flush

  val rob = Module(new Rob)
  val isu = Module(new IssueUnit)

  rob.io.in.bits := stall_reg.io.out.bits
  rob.io.in.valid := stall_reg.io.out.valid && isu.io.in.ready
  rob.io.flush := flush

  rename.io.cm_recover := RegNext(rob.io.jmp_packet.mis)
  rename.io.cm := rob.io.cm

  fetch.io.jmp_packet := rob.io.jmp_packet
  flush := rob.io.jmp_packet.mis

  isu.io.in.bits := stall_reg.io.out.bits
  isu.io.in.valid := stall_reg.io.out.valid && rob.io.in.ready
  isu.io.rob_addr := rob.io.rob_addr
  isu.io.flush := flush
  isu.io.avail_list := rename.io.avail_list
  isu.io.sys_ready := rob.io.sys_ready

  stall_reg.io.out.ready := rob.io.in.ready && isu.io.in.ready
```

#### ROB

为了便于ROB的操作，ROB接受来自rename级的输入作为入队，同时也会接受来自exe级和commit级的输入。

ROB居然用的同步MEM！！ val rob = SyncReadMem(entries, new MicroOp, SyncReadMem.WriteFirst)

太复杂了，大体上还是根据指令顺序维护了一个队列，exe，commit决定状态和退出。

ROB的队列输入：假设当前ROB里count为3，加入输入的num_enq，当确保rob容量输入后，拉高Reg类型的enq_ready(+&是一个位扩展加法，用于确保加法不会溢出)

```scala
  val num_after_enq = count +& num_enq
  val next_valid_entry = num_after_enq

  // be careful that enq_ready is register, not wire
  val enq_ready = RegInit(true.B)
  enq_ready := (entries - enq_width).U >= next_valid_entry
```

入队时，先计算入队的指令的偏移量，接着获取写入的idx，当io.in输入的数据有效且rob已经准备好时，让操作码写入的。

next_enq_vec维护了rob下次写入的地址

```scala
  val offset = Wire(Vec(enq_width, UInt(log2Up(enq_width).W)))
  for (i <- 0 until enq_width) {
    if (i == 0) {
      offset(i) := 0.U
    } else {
      // todo: currently only support 2-way
      offset(i) := PopCount(io.in.bits.vec(0).valid)
    }
  }

  for (i <- 0 until enq_width) {
    val enq = Wire(new MicroOp)
    enq := io.in.bits.vec(i)

    val enq_idx = getIdx(enq_vec(offset(i)))

    when (io.in.bits.vec(i).valid && io.in.fire() && !io.flush) {
      rob.write(enq_idx, enq)          // write to rob
      complete(enq_idx) := false.B     // mark as not completed
      ecp(enq_idx) := 0.U.asTypeOf(new ExCommitPacket)
      io.rob_addr(i) := enq_idx
    } .otherwise {
      io.rob_addr(i) := 0.U
    }
  }

  val next_enq_vec = VecInit(enq_vec.map(_ + num_enq))

  when (io.in.fire() && !io.flush) {
    enq_vec := next_enq_vec
  }

  io.in.ready := enq_ready
```

ROB接收了来自exe级的输入，会将对应rob_addr标记为compelete

同时，会维护一个`complete_mask(i)` 表示第 `i` 条指令及其之前的所有指令是否已经完成

rob会检查队列里末尾的两个数据，如果队列末尾的数据准备好了，向cm(i).valid标记。这时即可进入dequeue环节。此时，会根据 val num_deq = PopCount(cm.map(_.valid))计算要弹出几个数据

```scala
    // resolve WAW dependency
    // don't commit two instructions with same rd_addr at the same time
    if (i == 0) {
      cm(i).valid := valid_vec(i) && complete_mask(i) && jmp_mask(i) && store_mask(i)
    } else {
      // todo: currently only support 2-way commit
      cm(i).valid := valid_vec(i) && complete_mask(i) && jmp_mask(i) && store_mask(i) &&
                        Mux(cm(0).valid && cm(0).rd_en && cm(1).rd_en, cm(0).rd_addr =/= cm(1).rd_addr, true.B)
    }

```

ROB的队列弹出：ROB接受来自exe的输入。来自exe的输入会标记ROB的状态。ROB会计算队列中哪些数据是complete状态了可以弹出，弹出的数据送入commitStage，此时，一条指令的乱序执行到此结束

```scala
    // from execution --> commit stage
    val exe = Vec(IssueWidth, Input(new MicroOp))
    val exe_ecp = Vec(IssueWidth, Input(new ExCommitPacket))
    // commit stage
    val cm = Vec(deq_width, Output(new MicroOp))
    val cm_rd_data = Vec(deq_width, Output(UInt(64.W)))
    val cm_mmio = Vec(deq_width, Output(Bool()))
    val jmp_packet = Output(new JmpPacket)
    val sq_deq_req = Output(Bool())
```



#### ISU

issue会接受来自重排序缓存中获得的rob_addr以及重命名寄存器中的avail_list（指示处于EXECUTED或者COMMITED状态的物理寄存器）

ISSUE初始化了int和mem的保留站。如果指令类型是相关指令，则让他进入保留站。

对于保留站的输出，ISSUE起到连线到out接口的作用

```scala
  for (i <- 0 until IssueWidth - 1) {
    io.out(i) := int_iq.io.out(i)
  }
  io.out(IssueWidth - 1) := mem_iq.io.out(0)

  io.in.ready := int_iq.io.in.ready && mem_iq.io.in.read
```



##### IssueQueueOutOfOrder

###### INT

ZhouShan的INT发射队列与CSR合并，保留站维护一个ready_list，当指令处于avail状态时，并且Fu功能单元处于ready状态，可以宣告这条指令ready

```scala
  val deq_vec = Wire(Vec(deq_width, UInt(idx_width.W)))
  val deq_vec_valid = Wire(Vec(deq_width, Bool()))

  // ready to issue check
  val ready_list = WireInit(VecInit(Seq.fill(entries)(false.B)))
  for (i <- 0 until entries) {
    val rs1_avail = io.avail_list(buf(i).rs1_paddr)
    val rs2_avail = io.avail_list(buf(i).rs2_paddr)
    val fu_ready = io.fu_ready
    if (i == 0) {
      ready_list(i) := rs1_avail && rs2_avail && fu_ready && (!is_sys(i) || (is_sys(i) && io.sys_ready))
    } else {
      ready_list(i) := rs1_avail && rs2_avail && fu_ready && !is_sys(i)
    }
  }

```

发射队列的dequeue

rl即为readyList，当发现有指令ready时，可以拉高deq_vec_valid(wire)

```scala
  // todo: currently only support 2-way
  val rl0 = Cat(ready_list.reverse)
  deq_vec(0) := PriorityEncoder(rl0)
  deq_vec_valid(0) := ready_list(deq_vec(0))

  val rl1 = rl0 & ~UIntToOH(deq_vec(0), entries)
  deq_vec(1) := PriorityEncoder(rl1)
  deq_vec_valid(1) := ready_list(deq_vec(1)) && (deq_vec(1) =/= deq_vec(0))

  for (i <- 0 until deq_width) {
    val deq = buf(deq_vec(i))
    io.out(i) := deq
    io.out(i).valid := deq.valid && deq_vec_valid(i)
  }
```

发射队列的数据压缩

前情提要：deq_vec(0)和deq_vec(1)代表第一个第二个出队的指令。

遍历发射队列，当发射指令有效时找到第一个和第二个大于该指令的指令的有效指令。

指令向下压缩到buf中。

总结，该段代码实现了发射的数据压缩，每次从下向上扫描可以获取更高的性能.

```scala
  // collapse logic
  // todo: currently only support 2-way
  val up1 = WireInit(VecInit(Seq.fill(entries)(false.B)))
  val up2 = WireInit(VecInit(Seq.fill(entries)(false.B)))

  for (i <- 0 until entries) {
    up1(i) := (i.U >= deq_vec(0)) && deq_vec_valid(0) && !up2(i)
  }
  for (i <- 0 until entries) {
    up2(i) := (i.U >= deq_vec(1) - 1.U) && deq_vec_valid(1)
  }
  for (i <- 0 until entries) {
    when (up1(i)) {
      if (i < entries - 1) {
        buf(i.U) := buf((i + 1).U)
      } else {
        buf(i.U) := 0.U.asTypeOf(new MicroOp)
      }
    }
    when (up2(i)) {
      if (i < entries - 2) {
        buf(i.U) := buf((i + 2).U)
      } else {
        buf(i.U) := 0.U.asTypeOf(new MicroOp)
      }
    }
  }
```

发射队列的enqueue。

通过计算当前队列的最新指针，计算并入队

```scala
  val enq_offset = Wire(Vec(enq_width, UInt(log2Up(enq_width).W)))
  for (i <- 0 until enq_width) {
    if (i == 0) {
      enq_offset(i) := 0.U
    } else {
      // todo: currently only support 2-way
      enq_offset(i) := PopCount(io.in.bits.vec(0).valid)
    }
  }

  for (i <- 0 until enq_width) {
    val enq = Wire(new MicroOp)
    enq := io.in.bits.vec(i)
    enq.rob_addr := io.rob_addr(i)

    when (enq.valid && io.in.fire() && !io.flush) {
      buf(getIdx(enq_vec_real(enq_offset(i)))) := enq
    }
  }

  val next_enq_vec = VecInit(enq_vec.map(_ + num_enq - num_deq))

  when ((io.in.fire() || Cat(io.out.map(_.valid)).orR) && !io.flush) {
    enq_vec := next_enq_vec
  }

  io.in.ready := enq_ready && !has_sys
```

###### MEM

TODO

### PRF

prf起到读取物理寄存器作用

在commit级通过以下端口更新内容

- rd_en
- rd_paddr 
- rd_data

通过uop传入的值可以索引出输出

- val rs1_data = Vec(IssueWidth, Output(UInt(64.W)))
- val rs2_data = Vec(IssueWidth, Output(UInt(64.W)))

同时prf还设计了针对指令的转发

```scala
  for (i <- 0 until IssueWidth) {
    for (j <- 0 until IssueWidth - 1) {
      when (io.rd_en(j) && (io.rd_paddr(j) =/= 0.U)) {
        when (io.rd_paddr(j) === rs1_paddr(i)) {
          rs1_data(i) := io.rd_data(j)
        }
        when (io.rd_paddr(j) === rs2_paddr(i)) {
          rs2_data(i) := io.rd_data(j)
        }
      }
    }
  }
```



```scala
class Prf extends Module with ZhoushanConfig {
  val io = IO(new Bundle {
    val in = Vec(IssueWidth, Input(new MicroOp))
    val out = Vec(IssueWidth, Output(new MicroOp))
    val rs1_data = Vec(IssueWidth, Output(UInt(64.W)))
    val rs2_data = Vec(IssueWidth, Output(UInt(64.W)))
    val rd_en = Vec(IssueWidth, Input(Bool()))
    val rd_paddr = Vec(IssueWidth, Input(UInt(log2Up(PrfSize).W)))
    val rd_data = Vec(IssueWidth, Input(UInt(64.W)))
    val flush = Input(Bool())
  })
}
```

### Execution

Exe级初始化了Pipe0、1、2用于支持最大发射三条指令，Ex级没有传统的流水线握手，alu当拍即出计算完毕即拉高io.out.valid。mem这种非流水的需要等待计算完毕才可唤醒。

当计算完毕时，标记rob的complete状态为高，并赋值状态位 ROB //TODO

```scala
  for (i <- 0 until IssueWidth) {
    val rob_addr = io.exe(i).rob_addr
    when (io.exe(i).valid) {
      complete(rob_addr) := true.B
      ecp(rob_addr) := io.exe_ecp(i)
    }
  }
```



同时，可以设置Rename器件中

更新PrfStateTable状态为EXECUTED

```scala
  for (i <- 0 until IssueWidth) {
    when (io.exe(i) =/= 0.U) {
      table(io.exe(i)) := EXECUTED
    }
  }
```

### COMMIT

ROB传出的数据即顺序后的数据

经过流水寄存器后，来到了COMMIT级

写回PRF

```scala
  rf.io.rd_en := execution.io.rd_en
  rf.io.rd_paddr := execution.io.rd_paddr
  rf.io.rd_data := execution.io.rd_data
```

同时，COMMIT级也会更新Rename级中PrfStateTable表中寄存器的状态rd_ppaddr标记为FREE，rd_paddr标记为COMMITED

rd_ppaddr和rd_paddr的区别，当发生WAW时，会更新最新的ppaddr，此时，因为这个地址被再次写入，因而这个地址的生命可以被宣告结束，所以在提交时，可以把ppaddr的地址标记为Free

```scala
//Rename Module
  for (i <- 0 until CommitWidth) {
    pst.io.cm(i) := Mux(io.cm(i).valid && io.cm(i).rd_en, io.cm(i).rd_paddr, 0.U)
    pst.io.free(i) := Mux(io.cm(i).valid && io.cm(i).rd_en, io.cm(i).rd_ppaddr, 0.U)
  }
//PrfStateTable Module
  for (i <- 0 until CommitWidth) {
    when (io.cm(i) =/= 0.U) {
      table(io.cm(i)) := COMMITTED
    }
  }

  for (i <- 0 until CommitWidth) {
    when (io.free(i) =/= 0.U) {
      table(io.free(i)) := FREE
    }
  }
```

//疑问：ZhouShan的rob不支持当两条指令的目的地址一样时指令的retire，这是为何

## 一条指令在Backend的执行过程

### INT

太普通了，这个就不细讲了、

### MEM

ZhouShan的mem支持乱序load顺序store该功能需要配合保留站实现

store_mask

```scala
val store_mask = Wire(Vec(entries, Bool()))
for (i <- 0 until entries) {
  store_mask(i) := !(Cat((0 to i).map(is_store(_))).orR)
}
```

**计算逻辑**：

- 对于每个条目 `i`，检查从 0 到 `i` 的所有条目中是否有存储指令（`is_store` 为 `true`）。
- 如果没有存储指令，则 `store_mask(i)` 为 `true`，表示可以派发。
- 如果有存储指令，则 `store_mask(i)` 为 `false`，表示不能派发。

storemask维护了乱序派发的一致性，由于zhoushan使用了压缩队列发射的形式，因而从0-i的指令顺序为从旧到新

#### **初始状态**
队列大小为 8，内容如下（1 代表 Store，0 代表 Load）：
```
队列内容：0 0 1 0 0 1 0 1
索引：    0 1 2 3 4 5 6 7
```

- **`is_store`**：
  ```
  is_store = [false, false, true, false, false, true, false, true]
  ```
  
- **`store_mask`**：
  - `store_mask(i)` 表示从 0 到 i 的条目中没有 Store 指令。
  
  - 最终 `store_mask`：
    ```
    store_mask = [true, true, false, false, false, false, false, false]
    ```
  
- **可派发条目**：
  
  - 只有索引 0 和 1 的条目满足 `store_mask` 为 `true`，可以乱序派发。

---

#### **第一次派发**
派发索引 0 和 1 的条目（Load 指令）后，队列压缩：
```
队列内容：1 0 0 1 0 1
索引：    0 1 2 3 4 5
```

- **`is_store`**：
  ```
  is_store = [true, false, false, true, false, true]
  ```
- **`store_mask`**：
  ```
  store_mask = [false, false, false, false, false, false]
  ```
- **可派发条目**：
  - 只有索引 0 的条目（Store 指令）可以派发。

---

#### **第二次派发**
派发索引 0 的条目（Store 指令）后，队列压缩：
```
队列内容：0 0 1 0 1
索引：    0 1 2 3 4
```

- **`is_store`**：
  ```
  is_store = [false, false, true, false, true]
  ```
- **`store_mask`**：
  ```
  store_mask = [true, true, false, false, false]
  ```
- **可派发条目**：
  - 索引 0 和 1 的条目（Load 指令）可以乱序派发。

---

#### **第三次派发**
派发索引 0 和 1 的条目（Load 指令）后，队列压缩：
```
队列内容：1 0 1
索引：    0 1 2
```

- **`is_store`**：
  ```
  is_store = [true, false, true]
  ```
- **`store_mask`**：
  ```
  store_mask = [false, false, false]
  ```
- **可派发条目**：
  
  - 只有索引 0 的条目（Store 指令）可以派发。

---

#### **第四次派发**
派发索引 0 的条目（Store 指令）后，队列压缩：
```
队列内容：0 1
索引：    0 1
```

- **`is_store`**：
  ```
  is_store = [false, true]
  ```
- **`store_mask`**：
  ```
  store_mask = [true, false]
  ```
- **可派发条目**：
  - 只有索引 0 的条目（Load 指令）可以派发。

---

#### **第五次派发**
派发索引 0 的条目（Load 指令）后，队列压缩：
```
队列内容：1
索引：    0
```

- **`is_store`**：
  ```
  is_store = [true]
  ```
- **`store_mask`**：
  ```
  store_mask = [false]
  ```
- **可派发条目**：
  - 只有索引 0 的条目（Store 指令）可以派发。

---

#### **第六次派发**
派发索引 0 的条目（Store 指令）后，队列为空。

### CSR

### BRU
