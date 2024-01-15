#include "include/npc_common.h"
#include "include/ftrace.h"
#include "include/npc_verilator.h"
#include "include/iringbuf.h"
#include "include/difftest.h"
void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
extern void wp_trace(char *decodelog);
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

uint64_t g_nr_guest_inst; //可以复用作为指令计数器，记录指令总共走了多少步
IRingBuffer iring_buffer;
IRingBuffer mtrace_buffer;
extern void mputIringbuf();

void step_and_dump_wave(){
  top->eval();
  contextp->timeInc(1); //时间+1
  tfp->dump(contextp->time()); //使用时间
}

//----------------------------dpi-c----------------------------
extern "C" void sim_break(int nextpc,int ret_reg){
  npc_state.halt_ret=ret_reg;
  npc_state.halt_pc=nextpc;
  npc_state.state=NPC_END;
}
extern "C" void inv_break(int nextpc){
  npc_state.halt_pc=nextpc;
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

extern "C" void get_pc(int pc,int nextpc){
  // printf("pc: %x\n",pc);
  cpu.pc=pc;
  cpu.nextpc=nextpc;
}

extern "C" void prt_debug(const svBitVecVal* debug_1,int debug_2){
  printf("debug_1: %x debug_2: %x\n",*debug_1,debug_2);
}

//----------------------------dpi-c----------------------------
void npc_quit(){
  reg_display();
  npc_state.halt_pc=cpu.pc;
  npc_state.state=NPC_QUIT;
}
// void assert_fail_msg() {
  
//   statistic();
// }


static void statistic() {
  IFNDEF(CONFIG_TARGET_AM, setlocale(LC_NUMERIC, ""));
#define NUMBERIC_FMT MUXDEF(CONFIG_TARGET_AM, "%", "%'") PRIu64
  Log("host time spent = " NUMBERIC_FMT " us", g_timer);
  Log("total guest instructions = " NUMBERIC_FMT, g_nr_guest_inst);
  if (g_timer > 0) Log("simulation frequency = " NUMBERIC_FMT " inst/s", g_nr_guest_inst * 1000000 / g_timer);
  else Log("Finish running in less than 1 us and can not calculate the simulation frequency");
}

void putIringbuf(){
  while(!isIRingBufferEmpty(&iring_buffer)){
    char pop_iringbufdata[100];
    dequeueIRingBuffer(&iring_buffer,pop_iringbufdata);
    if(iring_buffer.num==0) wLog("[itrace]-->%s",pop_iringbufdata);
    else wLog("[itrace]   %s",pop_iringbufdata);

  }
}

static bool first_diff=true;
static void trace_and_difftest(word_t this_pc,word_t next_pc){
  g_nr_guest_inst++; //记录总共执行了多少步
  #ifdef CONFIG_DIFFTEST
  if(difftest_flag){
    /*第一次不进行diff,因为nemu的寄存器写入是瞬间写，npc是延迟一拍后写
    因此diff时机是npc执行结束了，进入下一排执行了，reg能取出来了，进行diff*/
    if(!first_diff) difftest_step(cpu.pc,cpu.nextpc);
    first_diff=false;
  }
  #endif
  // cpu.pc=this_pc;
  
  static char logbuf[128];
  static char tmp_dis[64];
  static word_t tmp_inst;
  tmp_inst=cpu.inst;
  disassemble(tmp_dis, sizeof(tmp_dis),next_pc, (uint8_t*)&tmp_inst,4);
  sprintf(logbuf,"[%ld]\t0x%08x: %08x\t%s",g_nr_guest_inst,next_pc,tmp_inst,tmp_dis);
  #ifdef CONFIG_ITRACE
  log_write("%s\n",logbuf);
  enqueueIRingBuffer(&iring_buffer,logbuf); //入队环形缓冲区
  #endif
  wp_trace(logbuf);
  if (g_print_step) { IFDEF(CONFIG_ITRACE,printf("%s\n",logbuf)); }
}


static void npc_execute(uint64_t n) {
  for (;n > 0; n --) {
    top->clock=1;

    step_and_dump_wave(); //step_and_dump_wave();要放对位置，因为放错位置排查好几个小时
    cpy_reg();
    trace_and_difftest(cpu.pc,cpu.nextpc);
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
    initializeIRingBuffer(&iring_buffer ,ITRACE_LOGBUF_SIZE);
    initializeIRingBuffer(&mtrace_buffer,MTRACE_LOGBUF_SIZE);
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
      if(npc_state.state==NPC_ABORT||npc_state.halt_ret!=0){
        IFDEF(CONFIG_ITRACE, putIringbuf()); 
        IFDEF(CONFIG_MTRACE, mputIringbuf()); 
      }
      Log("npc: %s at pc = " FMT_WORD,
          (npc_state.state == NPC_ABORT ? ANSI_FMT("ABORT", ANSI_FG_RED):
           (npc_state.halt_ret == 0 ? ANSI_FMT("HIT GOOD TRAP", ANSI_FG_CYAN):
            ANSI_FMT("HIT BAD TRAP", ANSI_FG_RED))),
          cpu.nextpc);
    case NPC_QUIT: statistic();
  }

}
