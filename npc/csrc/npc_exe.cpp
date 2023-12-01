#include "include/npc_common.h"
#include "include/npc_verilator.h"
extern VerilatedContext* contextp;
extern VerilatedVcdC* tfp;
extern VSimTop* top;
#define MAX_INST_TO_PRINT 21
void reg_display();
static bool g_print_step = false;

void step_and_dump_wave(){
  top->eval();
  contextp->timeInc(1); //时间+1
  tfp->dump(contextp->time()); //使用时间
}

extern void sim_break(int pc,int ret_reg){
  npc_state.halt_ret=ret_reg;
  npc_state.halt_pc=pc;
  npc_state.state=NPC_END;
}
extern void inv_break(int pc){
  npc_state.halt_pc=pc;
  npc_state.state=NPC_ABORT;
}

void assert_fail_msg() {
  reg_display();
  // statistic();
}

static void npc_execute(uint64_t n) {
  for (;n > 0; n --) {
    top->clock=1;
    printf("%x\n",top->io_pc);
    // top->io_inst=pmem_read(top->io_pc,4);
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
      if(npc_state.state == NPC_ABORT){
        Log("npc: \033[1;31mABORT\033[0m at pc = %d",npc_state.halt_pc);
      }else if(npc_state.state == NPC_END){
        if(npc_state.halt_ret==0) Log("npc: \033[1;32mHIT GOOD TRAP\033[0m at pc = %d",npc_state.halt_pc);
        else Log("npc: \033[1;31mHIT BAD TRAP\033[0m at pc = %d",npc_state.halt_pc);
      }
      // Log("npc: %s at pc = " FMT_WORD,
      //     (npc_state.state == NPC_ABORT ? ANSI_FMT("ABORT", ANSI_FG_RED):
      //      (npc_state.halt_ret == 0 ? ANSI_FMT("HIT GOOD TRAP", ANSI_FG_GREEN):
      //       ANSI_FMT("HIT BAD TRAP", ANSI_FG_RED))),
      //     npc_state.halt_pc);
      // fall through
    // case NPC_QUIT: statistic();
  }

}
