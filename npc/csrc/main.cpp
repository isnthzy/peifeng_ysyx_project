#include <stdint.h>
#include <stdlib.h>
#include "include/npc_common.h"
#include "include/npc_verilator.h"
#include "include/difftest/difftest.h"
#include "include/npc/npc_waveform.h"
void init_monitor(int, char *[]);
void sdb_mainloop();
void exit_waveform();
VerilatedContext* contextp = NULL;
TOP_MODULE_NAME* top;

#ifdef CONFIG_NVBOARD
#include <nvboard.h>
void nvboard_bind_all_pins(TOP_MODULE_NAME* top);
#endif
bool difftest_flag = false;
uint64_t wavebegin=0;
NPCState npc_state = { .state = NPC_STOP };


void sim_init(){
  contextp = new VerilatedContext;
  top = new TOP_MODULE_NAME;
}
void sim_exit(){
  delete top;
  difftest->exit_difftest(); //NOTE:退出difftest收回内存
#ifdef CONFIG_WAVEFORM
  waveform->exit_waveform(); //NOTE:退出波形记录
#else 
  printf_red("No Dump file because waveform is close\n");
#endif
  delete difftest;
  delete waveform;
  delete contextp;
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
