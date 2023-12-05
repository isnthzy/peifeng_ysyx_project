#include "include/npc_common.h"
#include "include/npc_verilator.h"
void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
#define MAX_INST_TO_PRINT 21
void reg_display();
uint64_t get_time();
static bool g_print_step = false;
static uint64_t g_timer = 0; // unit: us
uint64_t g_nr_guest_inst;

void step_and_dump_wave(){
  top->eval();
  contextp->timeInc(1); //时间+1
  tfp->dump(contextp->time()); //使用时间
}

//dpi-c
extern void sim_break(int pc,int ret_reg){
  npc_state.halt_ret=ret_reg;
  npc_state.halt_pc=pc;
  npc_state.state=NPC_END;
}
extern void inv_break(int pc){
  npc_state.halt_pc=pc;
  npc_state.state=NPC_ABORT;
}
//dpi-c

static void statistic() {
  IFNDEF(CONFIG_TARGET_AM, setlocale(LC_NUMERIC, ""));
#define NUMBERIC_FMT MUXDEF(CONFIG_TARGET_AM, "%", "%'") PRIu64
  Log("host time spent = " NUMBERIC_FMT " us", g_timer);
  Log("total guest instructions = " NUMBERIC_FMT, g_nr_guest_inst);
  if (g_timer > 0) Log("simulation frequency = " NUMBERIC_FMT " inst/s", g_nr_guest_inst * 1000000 / g_timer);
  else Log("Finish running in less than 1 us and can not calculate the simulation frequency");
}

void assert_fail_msg() {
  reg_display();
  statistic();
}

static void npc_execute(uint64_t n) {
  for (;n > 0; n --) {
    top->clock=1;
    // printf("%x\n",top->io_pc);
    top->io_inst=paddr_read(top->io_pc,4);

  #ifdef CONFIG_ITRACE
    g_nr_guest_inst++; //记录总共执行了多少步
    static char logbuf[128];
    static char tmp_dis[64];
    static word_t tmp_inst;
    tmp_inst=top->io_inst;
    disassemble(tmp_dis, sizeof(tmp_dis),top->io_pc, (uint8_t*)&tmp_inst,4);
    sprintf(logbuf,"0x%08x: %08x\t%s\n",top->io_pc,tmp_inst,tmp_dis);
    log_write("%s",logbuf);
    printf("%s",logbuf);
  #endif

    step_and_dump_wave(); //step_and_dump_wave();要放对位置，因为放错位置排查好几个小时
    /*------------------------分割线每个npc_execute其实是clk变化两次，上边变化一次，下边也变化一次*/

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

  uint64_t timer_start = get_time();
  npc_execute(step);
  uint64_t timer_end = get_time();
  g_timer += timer_end - timer_start;

  switch (npc_state.state) {
    case NPC_RUNNING: npc_state.state = NPC_STOP; break;
    case NPC_END: case NPC_ABORT:
      // if(npc_state.state==NPC_ABORT||npc_state.halt_ret!=0) putIringbuf();
      // if(npc_state.state == NPC_ABORT){
      //   Log("npc: \033[1;31mABORT\033[0m at pc = 0x%08x",npc_state.halt_pc);
      // }else if(npc_state.state == NPC_END){
      //   if(npc_state.halt_ret==0) Log("npc: \033[1;32mHIT GOOD TRAP\033[0m at pc = 0x%08x",npc_state.halt_pc);
      //   else Log("npc: \033[1;31mHIT BAD TRAP\033[0m at pc = 0x%08x",npc_state.halt_pc);
      // }
      Log("npc: %s at pc = " FMT_WORD,
          (npc_state.state == NPC_ABORT ? ANSI_FMT("ABORT", ANSI_FG_RED):
           (npc_state.halt_ret == 0 ? ANSI_FMT("HIT GOOD TRAP", ANSI_FG_GREEN):
            ANSI_FMT("HIT BAD TRAP", ANSI_FG_RED))),
          npc_state.halt_pc);
    case NPC_QUIT: statistic();
  }

}
