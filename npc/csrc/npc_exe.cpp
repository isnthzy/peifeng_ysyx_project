#include "include/npc_common.h"
#include "include/util/ftrace.h"
#include "include/npc_verilator.h"
#include "include/util/iringbuf.h"
#include "include/difftest/difftest.h"
#include "include/npc/npc_reg.h"
#include "include/npc/npc_device.h"
#include "include/npc/npc_exe.h"
#ifdef CONFIG_NVBOARD
#include <nvboard.h>
#endif
#define MAX_INST_TO_PRINT 10

extern bool ftrace_flag;
extern bool difftest_flag;
bool g_print_step = false;

static uint64_t g_timer = 0; // unit: us
static uint64_t g_clock_cnt = 0;
uint64_t g_nr_guest_inst; //可以复用作为指令计数器，记录指令总共走了多少步

void step_and_dump_wave(){
  top->eval();
  contextp->timeInc(1); //时间+1
#ifdef CONFIG_WAVEFORM
  tfp->dump(contextp->time()); //使用时间
#endif 
}

void npc_quit(){
  reg_dut_display();
  npc_state.state=NPC_QUIT;
}

static void statistic() {
  IFNDEF(CONFIG_TARGET_AM, setlocale(LC_NUMERIC, ""));
#define NUMBERIC_FMT MUXDEF(CONFIG_TARGET_AM, "%", "%'") PRIu64
  double ipc = g_nr_guest_inst / g_clock_cnt;
  Log("npc ipc = %.4f", ipc);
  Log("host time spent = " NUMBERIC_FMT " us", g_timer);
  Log("total guest instructions = " NUMBERIC_FMT, g_nr_guest_inst);
  if (g_timer > 0) Log("simulation frequency = " NUMBERIC_FMT " inst/s", g_nr_guest_inst * 1000000 / g_timer);
  else Log("Finish running in less than 1 us and can not calculate the simulation frequency");
}

void putIringbuf(){
  while(!isIRingBufferEmpty(&iring_buffer)){
    char pop_iringbufdata[100];
    dequeueIRingBuffer(&iring_buffer,pop_iringbufdata);
    if(iring_buffer.num==0) printf_green("[itrace]-->%s\n",pop_iringbufdata);
    else printf("[itrace]   %s\n",pop_iringbufdata);

  }
}

static void npc_execute(uint64_t n) {
  for (;n > 0; n --) {
    int state = 0;
    do{
      g_clock_cnt++; //从reset后开始计数
      top->clock=1;
      step_and_dump_wave(); //NOTE:要放对位置，因为放错位置排查好几个小时
      state=difftest->diff_step();
      //NOTE:每个npc_execute其实是clk变化两次，上边变化一次，下边也变化一次
      top->clock=0;
      step_and_dump_wave();
#ifdef CONFIG_NVBOARD
      nvboard_update();
#endif
    }while(state==NPC_NOCOMMIT);
    npc_state.state=state;
    if (npc_state.state != NPC_RUNNING) return;
  }
}

void init_traces(){
  #ifdef CONFIG_TRACE
  initializeIRingBuffer(&iring_buffer ,ITRACE_LOGBUF_SIZE);
  initializeIRingBuffer(&mtrace_buffer,MTRACE_LOGBUF_SIZE);
  #endif
}

void put_traces(){
  IFDEF(CONFIG_ITRACE, putIringbuf()); 
  IFDEF(CONFIG_MTRACE, mputIringbuf()); 
}

void npc_exev(uint64_t step){ //之所以不用int因为int是有符号的，批处理传入-1就是-1，无法达到效果
  g_print_step = (step<MAX_INST_TO_PRINT);
  switch (npc_state.state) {
    case NPC_SUCCESS_END: case NPC_ERROR_END: case NPC_ABORT: case NPC_QUIT:
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
    case NPC_SUCCESS_END: case NPC_ERROR_END: case NPC_ABORT:
      if(npc_state.state==NPC_ABORT||npc_state.state==NPC_ERROR_END){
        put_traces();
      }
      Log("npc: %s at pc = " FMT_WORD,
          (npc_state.state == NPC_ABORT ? ANSI_FMT("ABORT", ANSI_FG_RED):
           (npc_state.state == NPC_SUCCESS_END ? ANSI_FMT("HIT GOOD TRAP", ANSI_FG_CYAN):
            ANSI_FMT("HIT BAD TRAP", ANSI_FG_RED))),
          npc_state.halt_pc);
    case NPC_QUIT: statistic();
  }

}
