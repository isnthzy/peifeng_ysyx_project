Chisel Project Template
=======================

Another version of the [Chisel template](https://github.com/ucb-bar/chisel-template) supporting mill.
mill is another Scala/Java build tool without obscure DSL like SBT. It is much faster than SBT.

Contents at a glance:

* `.gitignore` - helps Git ignore junk like generated files, build products, and temporary files.
* `build.sc` - instructs mill to build the Chisel project
* `Makefile` - rules to call mill
* `playground/src/GCD.scala` - GCD source file
* `playground/src/DecoupledGCD.scala` - another GCD source file
* `playground/src/Elaborate.scala` - wrapper file to call chisel command with the GCD module
* `playground/test/src/GCDSpec.scala` - GCD tester

Feel free to rename or delete files under `playground/` or use them as a reference/template.

## Getting Started

First, install mill by referring to the documentation [here](https://com-lihaoyi.github.io/mill).

To run all tests in this design (recommended for test-driven development):
```bash
make test
```

To generate Verilog:
```bash
make verilog
```

## Change FIRRTL Compiler

You can change the FIRRTL compiler between SFC (Scala-based FIRRTL compiler) and
MFC (MLIR-based FIRRTL compiler) by modifying the `useMFC` variable in `playground/src/Elaborate.scala`.
The latter one requires `firtool`, which is included under `utils/`.

## verilator参数
-MMD: 生成依赖关系文件。Verilator 将根据源代码文件的包含关系自动生成依赖关系，以便在构建过程中自动重新编译相关文件。
--build: 在构建期间生成 C++ 代码。Verilator 将会生成用于仿真的 C++ 代码，这些代码将用于构建仿真可执行文件。
-cc: 生成 C++ 代码。这个选项指示 Verilator 生成用于构建 C++ 仿真代码的文件。
-O3: 启用优化级别 3。Verilator 将应用高级优化技术来优化生成的仿真代码，以提高仿真性能。
--x-assign fast: 在仿真中使用快速信号赋值。这个选项指示 Verilator 使用一种快速的信号赋值机制，以提高仿真的运行速度。
--x-initial fast: 在仿真开始时使用快速的初始值设定。这个选项指示 Verilator 使用一种快速的初始值设定机制，以提高仿真的启动速度。
--noassert: 禁用断言。这个选项指示 Verilator 在生成的仿真代码中禁用断言语句，从而减少代码量并提高仿真速度。
--timing: 启用时序分析。这个选项指示 Verilator 在仿真过程中执行时序分析，以捕获和报告时序信息。
--trace: 启用波形跟踪。这个选项指示 Verilator 在仿真过程中生成波形跟踪文件，用于波形查看器进行仿真波形分析。