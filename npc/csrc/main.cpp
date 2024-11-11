#include <stdint.h>
#include <stdlib.h>
#include "include/npc_common.h"
#include "include/npc_verilator.h"
#include "include/difftest/difftest.h"
#include "include/util/debug.h"
void init_monitor(int, char *[]);
void sdb_mainloop();
VerilatedContext* contextp = NULL;
TOP_MODULE_NAME* top;
#ifdef TRACE_FST
VerilatedFstC* tfp = NULL;
#else
VerilatedVcdC* tfp = NULL;
#endif
#ifdef CONFIG_NVBOARD
#include <nvboard.h>
void nvboard_bind_all_pins(TOP_MODULE_NAME* top);
#endif
bool difftest_flag = false;
uint64_t total_wave_dump = 0;
uint64_t wavebegin=0;
NPCState npc_state = { .state = NPC_STOP };

void init_waveform(){
#ifdef CONFIG_WAVEFORM
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
#endif
  //使用make sim生成的dump.vcd在npc/
  //SimTop+*.bin生成的dump.vcd在npc/build
}

void sim_init(){
  contextp = new VerilatedContext;
  top = new TOP_MODULE_NAME;
}
void sim_exit(){
  delete top;
  difftest->exit_difftest(); //NOTE:退出difftest收回内存
  delete difftest;
  #ifdef CONFIG_WAVEFORM
  if(wavebegin==0){
    long int suggest_savewave=1;
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
    printf_red("No Dump file because waveform is close\n");
    printf_red("Use \"make xxx WAVE={$time}\" to open waveform at $time\n");
  }else{
    tfp->close();
    printf_green("The Dump file has been saved at npc/dump.{fst,vcd}\n");
  }
  #else
  printf_red("No Dump file because waveform is close\n");
  #endif
}

int is_exit_status_bad() {
  int good = (npc_state.state == NPC_SUCCESS_END ) ||
    (npc_state.state == NPC_QUIT);
  sim_exit();
  IFDEF(CONFIG_NVBOARD, nvboard_quit();)
  return !good;
}

int main(int argc, char *argv[]) {
  Verilated::commandArgs(argc, argv);
  sim_init();
  //初始化verilator仿真文件

#ifdef CONFIG_NVBOARD
  nvboard_bind_all_pins(top);

  nvboard_init();
#endif

  init_monitor(argc, argv);

  sdb_mainloop();

  return is_exit_status_bad();
}
