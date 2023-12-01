#include <stdint.h>
#include <stdlib.h>
#include "include/npc_common.h"
#include "include/npc_verilator.h"
void init_monitor(int, char *[]);
void sdb_mainloop();
extern VerilatedContext* contextp;
extern VerilatedVcdC* tfp;
extern VSimTop* top;
NPCState npc_state = { .state = NPC_STOP };

void sim_exit(){
  delete top;
  tfp->close();
}
int is_exit_status_bad() {
  int good = (npc_state.state == NPC_END && npc_state.halt_ret == 0) ||
    (npc_state.state == NPC_QUIT);
  sim_exit();
  return !good;
}

int main(int argc, char *argv[]) {
  init_monitor(argc, argv);

  sdb_mainloop();

  return is_exit_status_bad();
}
