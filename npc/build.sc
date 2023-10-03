import mill._, scalalib._
import mill.scalalib.scalafmt.ScalafmtModule

object npc extends RootModule with ScalaModule with ScalafmtModule {
  def scalaVersion = "2.13.10"
   def scalacOptions = Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-Xcheckinit"
  )
  def ivyDeps = Agg(
    ivy"org.chipsalliance::chisel:5.0.0",
  )
  def scalacPluginIvyDeps = Agg(
    ivy"org.chipsalliance:::chisel-plugin:5.0.0",
  )
  
  object test extends ScalaTests {
    def ivyDeps = Agg(ivy"com.lihaoyi::utest:0.8.1",
                      ivy"edu.berkeley.cs::chiseltest:5.0.0"
           )
    def testFramework = "utest.runner.Framework"
  }
}