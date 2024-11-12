#include "../include/npc_verilator.h"
#include "../include/npc_common.h"
#include "../include/npc/npc_waveform.h"
#include <ctime>

#ifdef TRACE_FST
VerilatedFstC* tfp = NULL;
#else
VerilatedVcdC* tfp = NULL;
#endif
extern uint64_t wavebegin;

void Waveform::dump_waveform(){
  total_wave_dump++;
  if(total_wave_dump>=wavebegin&&wavebegin!=0){
    tfp->dump(contextp->time()); //使用时间进行dump
  }
}

void Waveform::init_waveform(){
  total_wave_dump=0;
#ifdef TRACE_FST
  tfp = new VerilatedFstC;
  contextp->traceEverOn(true);
  top->trace(tfp, 0);
  tfp->open("dump.fst"); 
#else
  tfp = new VerilatedVcdC;
  contextp->traceEverOn(true);
  top->trace(tfp, 0);
  tfp->open("dump.vcd"); 
#endif
  //使用make sim生成的dump.vcd在npc/
  //SimTop+*.bin生成的dump.vcd在npc/build
}

void Waveform::save_suggest_dump_time(){
  std::time_t now = std::time(nullptr); //获取当前时间
  std::tm *local = std::localtime(&now);
  char str[80];
  sprintf(str,"build/Suggest-WaveForm %02d:%02d:%02d",local->tm_mday,local->tm_hour,local->tm_min);
  FILE *fp;
  fp = fopen(str,"w+");
  fprintf(fp,"Original stop position:%ld\n\
Suggest use \"make xxx WAVE=%ld\" to open waveform\n",total_wave_dump,suggest_savewave);

}

void Waveform::exit_waveform(){
  if(wavebegin==0){
    suggest_savewave=1;
    if(total_wave_dump>=10000){
      if(DEADLOCK_TIME==0){
        suggest_savewave=(total_wave_dump-10000)/10000*10000;
      }else{
        if(total_wave_dump>=DEADLOCK_TIME*3){
          suggest_savewave=(total_wave_dump-DEADLOCK_TIME*3)/DEADLOCK_TIME*DEADLOCK_TIME;
        }
      }
    }
    Log("Suggest to open waveform at %ld",suggest_savewave);
    save_suggest_dump_time();
    //NOTE:另外创建一个最后波形的备份文件在build目录下，包含时间，和停止位置（并且用时间命名防止覆盖）
    printf_red("No Dump file because waveform is close\n");
    printf_red("Use \"make xxx WAVE={$time}\" to open waveform at $time\n");
  }else{
    tfp->close();
    delete tfp;
    printf_green("The Dump file has been saved at npc/dump.{fst,vcd}\n");
  }
}
