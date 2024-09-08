#include "include/npc_common.h"
#include "include/util/ftrace.h"
#include "include/npc_verilator.h"
#include "include/util/iringbuf.h"
#include "include/difftest/difftest.h"
#include "include/npc/npc_reg.h"
#include "include/npc/npc_device.h"
#include "include/npc/npc_exe.h"
#define MAX_INST_TO_PRINT 10

// CPU_state cpu = { .pc=RESET_VECTOR , .mstatus=0x1800};//解锁新用法
// CPU_info cpu_info={};
extern bool ftrace_flag;
extern bool difftest_flag;
bool g_print_step = false;

static uint64_t g_timer = 0; // unit: us
uint64_t g_nr_guest_inst; //可以复用作为指令计数器，记录指令总共走了多少步

void step_and_dump_wave(){
  top->eval();
  contextp->timeInc(1); //时间+1
#ifdef TRACE_VCD
  tfp->dump(contextp->time()); //使用时间
#endif 
}



// //----------------------------dpi-c----------------------------
// extern "C" void sim_break(int pc,int ret_reg_data){
//   npc_state.halt_ret=ret_reg_data;
//   npc_state.halt_pc=pc;
//   npc_state.state=NPC_END;
// }
// extern "C" void inv_break(int pc){
//   npc_state.halt_pc=pc;
//   npc_state.state=NPC_ABORT;
// }

// extern "C" void cpu_use_func(int pc,int nextpc,svBit is_ret,svBit is_jal,svBit is_rd0){
//   //调用cpu_use_func后，is_jal=1 jal,is_jal=0 jalr
//   #ifdef CONFIG_FTRACE
//   if(ftrace_flag){
//     if(is_jal){ //jal指令
//       func_call(pc,nextpc,false);
//     }else{   //jalr指令
//       if(is_ret){
//         func_ret(pc);
//       }else if(is_rd0){ //jalr是跳转,jr不是(jr被编译器优化为尾调用)
//         func_call(pc,nextpc,true);
//       }else{
//         func_call(pc,nextpc,false);
//       }
//     }
//   }
//   #endif
// }

// extern "C" void get_info(int pc,int nextpc,int inst,svBit dpi_valid){
//   // printf("pc: %x\n",pc);
//   if(dpi_valid){
//     cpu.pc=pc;
//     cpu_info.nextpc=nextpc;
//     cpu_info.inst=inst;
//   }
//   cpu_info.valid=dpi_valid;
// }

// extern "C" void prt_debug(const svBitVecVal* debug_1,int debug_2){
//   printf("debug_1: %x debug_2: %x\n",*debug_1,debug_2);
// }

// #define MTVEC 0x305
// #define MSTATUS 0x300
// #define MEPC 0x341
// #define MCAUSE 0x342
// extern "C" void sync_csrfile_regs(int waddr,int wdata){
//   switch (waddr)
//   {
//   case MTVEC: cpu.mtvec=wdata; break;
//   case MSTATUS: cpu.mstatus=wdata; break;
//   case MEPC: cpu.mepc=wdata; break;
//   case MCAUSE: cpu.mcause=wdata; break;
//   default:
//     panic("unknown csr address");
//     break;
//   }
// }

// extern "C" void sync_csr_exception_regs(int mcause_in,int pc_wb){
//   cpu.mcause=mcause_in;
//   cpu.mepc=pc_wb;
// }

// extern "C" void Csr_assert(){
//   panic("csr寄存器异常读写");
// }
// //----------------------------dpi-c----------------------------


void npc_quit(){
  reg_dut_display();
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


// static void trace_and_difftest(){
//   g_nr_guest_inst++; //记录总共执行了多少步
//   #ifdef CONFIG_DIFFTEST
//   static bool first_diff=true;
//   if(difftest_flag){
//     /*第一次不进行diff,因为nemu的寄存器写入是瞬间写，npc是延迟一拍后写
//     因此diff时机是npc执行结束了，进入下一排执行了，reg能取出来了，进行diff*/
//     if(!first_diff) difftest_step(cpu.pc,cpu_info.nextpc);
//     first_diff=false;
//   }
//   #endif

//   static char logbuf[128];
//   static char tmp_dis[64];
//   #ifdef CONFIG_TRACE
//   static word_t tmp_inst;
//   tmp_inst=cpu_info.inst;
//   disassemble(tmp_dis, sizeof(tmp_dis),cpu.pc, (uint8_t*)&tmp_inst,4);
//   sprintf(logbuf,"[%ld]\t0x%08x: %08x\t%s",g_nr_guest_inst,cpu.pc,tmp_inst,tmp_dis);
//   #ifdef CONFIG_ITRACE
//   log_write("%s\n",logbuf);
//   enqueueIRingBuffer(&iring_buffer,logbuf); //入队环形缓冲区
//   #endif
//   wp_trace(logbuf);
//   if (g_print_step) { IFDEF(CONFIG_ITRACE,printf("%s\n",logbuf)); }
//   #endif
// }


static void npc_execute(uint64_t n) {
  for (;n > 0; n --) {
    int state = 0;
    do{
      top->clock=1;
      step_and_dump_wave(); //NOTE:要放对位置，因为放错位置排查好几个小时
      state=difftest->diff_step();
      //NOTE:每个npc_execute其实是clk变化两次，上边变化一次，下边也变化一次
      top->clock=0;
      step_and_dump_wave();
    }while(state==NPC_NOCOMMIT);
    npc_state.state=state;
    if (npc_state.state != NPC_RUNNING) return;
  }
  // if(g_nr_guest_inst>CONFIG_MAX_EXE_INST){
  //   panic("Too many instructions(Suspected to be in a traploop)");
  // }
}

void init_traces(){
  #ifdef CONFIG_TRACE
  initializeIRingBuffer(&iring_buffer ,ITRACE_LOGBUF_SIZE);
  initializeIRingBuffer(&mtrace_buffer,MTRACE_LOGBUF_SIZE);
  #endif
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
      if(npc_state.state==NPC_ABORT||npc_state.state!=NPC_ERROR_END){
        IFDEF(CONFIG_ITRACE, putIringbuf()); 
        IFDEF(CONFIG_MTRACE, mputIringbuf()); 
      }
      Log("npc: %s at pc = " FMT_WORD,
          (npc_state.state == NPC_ABORT ? ANSI_FMT("ABORT", ANSI_FG_RED):
           (npc_state.state == NPC_SUCCESS_END ? ANSI_FMT("HIT GOOD TRAP", ANSI_FG_CYAN):
            ANSI_FMT("HIT BAD TRAP", ANSI_FG_RED))),
          npc_state.halt_pc);
    case NPC_QUIT: statistic();
  }

}
