import chisel3._
import chiseltest._
import chisel3.experimental.BundleLiterals._

import utest._

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly gcd.GcdDecoupledTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly gcd.GcdDecoupledTester'
  * }}}
  */

class MyModuleTest extends FlatSpec with ChiselScalatestTester {
  behavior of "MyModule"

  it should "correctly select input bits" in {
    test(new MyModule) { dut =>
      // 输入测试向量
      val testInputs = Seq(
        (0.U(4.W), 0.U(2.W), 0.U(1.W)),  // 输入0，选择0，期望输出0
        (0.U(4.W), 1.U(2.W), 0.U(1.W)),  // 输入0，选择1，期望输出0
        (0.U(4.W), 2.U(2.W), 0.U(1.W)),  // 输入0，选择2，期望输出0
        (0.U(4.W), 3.U(2.W), 0.U(1.W)),  // 输入0，选择3，期望输出0
        (7.U(4.W), 0.U(2.W), 1.U(1.W)),  // 输入7，选择0，期望输出1
        (7.U(4.W), 1.U(2.W), 1.U(1.W)),  // 输入7，选择1，期望输出1
        (7.U(4.W), 2.U(2.W), 0.U(1.W)),  // 输入7，选择2，期望输出0
        (7.U(4.W), 3.U(2.W), 0.U(1.W))   // 输入7，选择3，期望输出0
      )

      // 逐个输入测试向量进行测试
      for ((in, sel, expectedOut) <- testInputs) {
        dut.io.in.poke(in)
        dut.io.sel.poke(sel)
        dut.clock.step()

        dut.io.out.expect(expectedOut)
      }
    }
  }
}
