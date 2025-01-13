import chisel3._
import chisel3.util._
import CoreConfig.Configs._
import CoreConfig.{DeviceConfig,CacheConfig}
import CoreConfig.GenerateParams
import PipeLine.{PreFetchStage,InstFetchStage,InstDecodeStage,ExecuteStage}

class SimTop extends Module with DeviceConfig with CacheConfig{
  override val desiredName = "ysyx_23060115"
  val io = IO(new Bundle {
    val interrupt=if(GenerateParams.getParam("SOC_MODE").asInstanceOf[Boolean]){
      Some(Input(Bool()))
    }else None
    // val master=if(GenerateParams.getParam("SOC_MODE").asInstanceOf[Boolean]){ 
    //   Some(new AxiTopBundle()) 
    // }else None
    // val slave=if(GenerateParams.getParam("SOC_MODE").asInstanceOf[Boolean]){
    //   Some(Flipped(new AxiTopBundle())) 
    // }else None
  })
  val PreFetch  = Module(new PreFetchStage())
  val InstFetch = Module(new InstFetchStage())
  val InstDecode= Module(new InstDecodeStage())
  val Execute   = Module(new ExecuteStage())
  val CsrFile   = Module(new CsrFile())

}