#include "../include/npc_common.h"
#include "../include/npc_verilator.h"
#include "../include/npc/npc_reg.h"
#include "../include/difftest/difftest.h"

#define gpr(idx) (difftest->get_dut_gpr(idx))

const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

void reg_dut_display(){
  difftest->display();
}

word_t isa_reg_str2val(const char *s, bool *success) {
  int idx=0;
  char str[10];
  strcpy(str,s+1); //去除最左边的$
  if(strcmp(str,"pc")==0) return difftest->get_dut_pc(); //实现断点
  for(int i=0;i<32;i++){
    if(strcmp(regs[i],str)==0){
      idx=i;
      break;
    }
    if(i==31) *success=false;
  }
  return gpr(idx);
}
