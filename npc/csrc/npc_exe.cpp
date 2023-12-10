#include "include/npc_common.h"
#include "include/ftrace.h"
#include "include/npc_verilator.h"
#include "include/iringbuf.h"
#include "include/difftest.h"
void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
#define MAX_INST_TO_PRINT 10
void reg_display();
void cpy_reg();
uint64_t get_time();
// CPU_state cpu;
CPU_state cpu = { .pc=RESET_VECTOR};//解锁新用法
extern bool ftrace_flag;
extern bool difftest_flag;
static bool g_print_step = false;
static uint64_t g_timer = 0; // unit: us
uint64_t g_nr_guest_inst;
IRingBuffer iring_buffer;

void step_and_dump_wave(){
  top->eval();
  contextp->timeInc(1); //时间+1
  tfp->dump(contextp->time()); //使用时间
}

//----------------------------dpi-c----------------------------
extern "C" void sim_break(int pc,int ret_reg){
  npc_state.halt_ret=ret_reg;
  npc_state.halt_pc=pc;
  npc_state.state=NPC_END;
}
extern "C" void inv_break(int pc){
  printf("????");
  npc_state.halt_pc=pc;
  npc_state.state=NPC_ABORT;
}

extern "C" void cpu_use_func(int pc,int nextpc,int inst,svBit is_jal,int rd){
  //调用cpu_use_func后，is_jal=1 jal,is_jal=0 jalr
  #ifdef CONFIG_FTRACE
  if(ftrace_flag){
    if(is_jal){ //jal指令
      if(rd==1) func_call(pc,nextpc,false);
    }else{   //jalr指令
      if(inst==0x00008067){
        func_ret(pc);
      }else if(rd==1){ //jalr是跳转,jr不是(jr被编译器优化为尾调用)
        func_call(pc,nextpc,false);
      }else if(rd==0){
        func_call(pc,nextpc,true);
      }
    }
  }
  #endif
}

extern "C" void get_pc(int nextpc){
  // printf("pc: %x\n",pc);
  cpu.pc=nextpc;
}

//----------------------------dpi-c----------------------------

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

void putIringbuf(){
  while(!isIRingBufferEmpty(&iring_buffer)){
    char pop_iringbufdata[100];
    dequeueIRingBuffer(&iring_buffer,pop_iringbufdata);
    if(iring_buffer.size==0) wLog("-->%s",pop_iringbufdata);
    else wLog("   %s",pop_iringbufdata);

  }
}

static void trace_and_difftest(word_t this_pc,word_t next_pc){
  g_nr_guest_inst++; //记录总共执行了多少步
  
  // cpu.pc=this_pc;
  if(difftest_flag) difftest_step(cpu.pc,next_pc);

  static char logbuf[128];
  static char tmp_dis[64];
  static word_t tmp_inst;
  tmp_inst=cpu.inst;
  disassemble(tmp_dis, sizeof(tmp_dis),this_pc, (uint8_t*)&tmp_inst,4);
  sprintf(logbuf,"0x%08x: %08x\t%s",this_pc,tmp_inst,tmp_dis);
  #ifdef CONFIG_ITRACE
  log_write("%s\n",logbuf);
  enqueueIRingBuffer(&iring_buffer,logbuf); //入队环形缓冲区
  #endif
  if (g_print_step) { IFDEF(CONFIG_ITRACE,printf("%s\n",logbuf)); }
}

static void npc_execute(uint64_t n) {
  for (;n > 0; n --) {
    top->clock=1;
    // printf("%x\n",top->io_pc);
    // top->io_inst=paddr_read(top->io_pc,4);
    static word_t this_pc;
    static word_t next_pc;
    
    step_and_dump_wave(); //step_and_dump_wave();要放对位置，因为放错位置排查好几个小时

    this_pc=cpu.pc;
    next_pc=cpu.nextpc;

    trace_and_difftest(this_pc,next_pc);
    /*------------------------分割线每个npc_execute其实是clk变化两次，上边变化一次，下边也变化一次*/

    top->clock=0;
    step_and_dump_wave();
    cpy_reg();
    if (npc_state.state != NPC_RUNNING) break;
  }
}

bool init_iringbuf_f=false;
void npc_exev(uint64_t step){ //之所以不用int因为int是有符号的，批处理传入-1就是-1，无法达到效果
  if(!init_iringbuf_f){
    init_iringbuf_f=true;
    initializeIRingBuffer(&iring_buffer);
  } //初始化iringbuffer,只初始化一次
  g_print_step = (step<MAX_INST_TO_PRINT);
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
      if(npc_state.state==NPC_ABORT||npc_state.halt_ret!=0) IFDEF(CONFIG_ITRACE,putIringbuf()); 
      Log("npc: %s at pc = " FMT_WORD,
          (npc_state.state == NPC_ABORT ? ANSI_FMT("ABORT", ANSI_FG_RED):
           (npc_state.halt_ret == 0 ? ANSI_FMT("HIT GOOD TRAP", ANSI_FG_CYAN):
            ANSI_FMT("HIT BAD TRAP", ANSI_FG_RED))),
          npc_state.halt_pc);
    case NPC_QUIT: statistic();
  }

}
