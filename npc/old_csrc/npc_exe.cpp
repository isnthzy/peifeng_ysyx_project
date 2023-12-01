#include <npc_common.h>
#define MAX_INST_TO_PRINT 21
static bool g_print_step = false;

void step_and_dump_wave();

static void npc_execute(uint64_t n) {
  for (;n > 0; n --) {
    top->clock=1;
    top->io_inst=pmem_read(top->io_pc,4);
    step_and_dump_wave();

    top->clock=0;
    step_and_dump_wave();
    if (npc_state.state != NPC_RUNNING) break;
  }
}


void npc_exev(int step){
  switch (npc_state.state) {
    case NPC_END: case NPC_ABORT:
      printf("Program execution has ended. To restart the program, exit NPC and run again.\n");
      return;
    default: npc_state.state = NPC_RUNNING;
  }

  npc_execute(step);

  switch (npc_state.state) {
    case NPC_RUNNING: npc_state.state = NPC_STOP; break;
    case NPC_END: case NPC_ABORT:
      // if(npc_state.state==NPC_ABORT||npc_state.halt_ret!=0) putIringbuf();
      Log("npc: %s at pc = " FMT_WORD,
          (npc_state.state == NPC_ABORT ? ANSI_FMT("ABORT", ANSI_FG_RED):
           (npc_state.halt_ret == 0 ? ANSI_FMT("HIT GOOD TRAP", ANSI_FG_GREEN):
            ANSI_FMT("HIT BAD TRAP", ANSI_FG_RED))),
          npc_state.halt_pc);
      // fall through
    case NPC_QUIT: statistic();
  }

}
