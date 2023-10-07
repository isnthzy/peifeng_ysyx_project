// MyModule.
// MyModuleTest.scala
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MyModuleTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "MyModule"
  it should "get right results" in {
    test(new MyModule) {c =>
      c.io.out_add.expect(5.U)
      c.io.out_sub.expect(1.U)
      c.io.out_mul.expect(8.U)
    }
    println("SUCCESS!!")
  }
}