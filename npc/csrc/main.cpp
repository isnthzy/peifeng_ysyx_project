#include <stdint.h>
#include <stdlib.h>
#include "include/npc_common.h"
#include "include/npc_verilator.h"
#include "include/difftest/difftest.h"
void init_monitor(int, char *[]);
void sdb_mainloop();
VerilatedContext* contextp = NULL;
#ifdef TRACE_FST
VerilatedFstC* tfp = NULL;
#else
VerilatedVcdC* tfp = NULL;
#endif
VSimTop* top;
bool difftest_flag = false;
NPCState npc_state = { .state = NPC_STOP };

void sim_exit(){
  delete top;
  difftest->exit_difftest(); //NOTE:退出difftest收回内存
  delete difftest;
  #ifdef CONFIG_WAVEFORM
  tfp->close();
  printf_green("The Dump file has been saved at npc/dump.{fst,vcd}\n");
  #endif
}
int is_exit_status_bad() {
  int good = (npc_state.state == NPC_SUCCESS_END ) ||
    (npc_state.state == NPC_QUIT);
  sim_exit();
  return !good;
}

int main(int argc, char *argv[]) {
  Verilated::commandArgs(argc, argv);
  init_monitor(argc, argv);

  sdb_mainloop();

  return is_exit_status_bad();
}
