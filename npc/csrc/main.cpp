#include <stdint.h>
#include <stdlib.h>
#include "include/npc_common.h"
#include "include/npc_verilator.h"
#include "include/difftest/difftest.h"
void init_monitor(int, char *[]);
void sdb_mainloop();
VerilatedContext* contextp = NULL;
VerilatedVcdC* tfp = NULL;
VSimTop* top;
bool difftest_flag = false;
NPCState npc_state = { .state = NPC_STOP };

void sim_exit(){
  delete top;
  delete difftest;
  #ifdef TRACE_VCD
  tfp->close();
  #endif
}
int is_exit_status_bad() {
  int good = (npc_state.state == NPC_SUCCESS_END ) ||
    (npc_state.state == NPC_QUIT);
  sim_exit();
  return !good;
}

int main(int argc, char *argv[]) {
  init_monitor(argc, argv);

  sdb_mainloop();

  return is_exit_status_bad();
}
