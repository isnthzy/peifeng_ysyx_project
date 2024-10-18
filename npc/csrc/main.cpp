#include <stdint.h>
#include <stdlib.h>
#include "include/npc_common.h"
#include "include/npc_verilator.h"
#include "include/difftest/difftest.h"
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
NPCState npc_state = { .state = NPC_STOP };

void sim_init(){
  contextp = new VerilatedContext;
  top = new TOP_MODULE_NAME;
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
void sim_exit(){
  delete top;
  difftest->exit_difftest(); //NOTE:退出difftest收回内存
  delete difftest;
  #ifdef CONFIG_WAVEFORM
  tfp->close();
  printf_green("The Dump file has been saved at npc/dump.{fst,vcd}\n");
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
