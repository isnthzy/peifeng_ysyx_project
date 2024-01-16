#include "../include/npc_common.h"
#include "../include/npc_verilator.h"
#include "../include/npc_conf.h"
extern CPU_state cpu;

void cpy_reg() {
  cpu.gpr[0]=0;
  cpu.gpr[1]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_1;
  cpu.gpr[2]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_2;
  cpu.gpr[3]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_3;
  cpu.gpr[4]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_4;
  cpu.gpr[5]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_5;
  cpu.gpr[6]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_6;
  cpu.gpr[7]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_7;
  cpu.gpr[8]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_8;
  cpu.gpr[9]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_9;
  cpu.gpr[10]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_10;
  cpu.gpr[11]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_11;
  cpu.gpr[12]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_12;
  cpu.gpr[13]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_13;
  cpu.gpr[14]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_14;
  cpu.gpr[15]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_15;
  cpu.gpr[16]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_16;
  cpu.gpr[17]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_17;
  cpu.gpr[18]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_18;
  cpu.gpr[19]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_19;
  cpu.gpr[20]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_20;
  cpu.gpr[21]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_21;
  cpu.gpr[22]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_22;
  cpu.gpr[23]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_23;
  cpu.gpr[24]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_24;
  cpu.gpr[25]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_25;
  cpu.gpr[26]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_26;
  cpu.gpr[27]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_27;
  cpu.gpr[28]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_28;
  cpu.gpr[29]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_29;
  cpu.gpr[30]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_30;
  cpu.gpr[31]=top->rootp->SimTop__DOT__ID_stage__DOT__Regfile__DOT__rf_31;

}


int check_reg_idx(int idx) {
  assert(idx >= 0 && idx <32);
  return idx;
}



const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

void reg_ref_display(CPU_state *ref_r){
  int i;
  printf("name   value   name   value   name   value   name   value\n");
  for(i=0;i<32;i+=4){
    printf("%3s 0x%08x %3s 0x%08x %3s 0x%08x %3s 0x%08x\n",\
    regs[i],ref_r->gpr[i],regs[i+1],ref_r->gpr[i+1],regs[i+2],ref_r->gpr[i+2],regs[i+3],ref_r->gpr[i+3]);
  }
}

void reg_dut_display(){
  int i;
  printf("name   value   name   value   name   value   name   value\n");
  for(i=0;i<32;i+=4){
    printf("%3s 0x%08x %3s 0x%08x %3s 0x%08x %3s 0x%08x\n",\
    regs[i],gpr(i),regs[i+1],gpr(i+1),regs[i+2],gpr(i+2),regs[i+3],gpr(i+3));
  }
  
}
#ifdef CONFIG_DIFFTEST
extern CPU_state* ref_t; 
#endif
void reg_display(){
  #ifdef CONFIG_DIFFTEST
  puts("----------------------------ref----------------------------");
  reg_ref_display(ref_t);
  #endif
  puts("----------------------------dut----------------------------");
  reg_dut_display();
}
word_t isa_reg_str2val(const char *s, bool *success) {
  int idx=0;
  char str[10];
  strcpy(str,s+1); //去除最左边的$
  if(strcmp(str,"pc")==0) return cpu.pc; //实现断点
  for(int i=0;i<32;i++){
    if(strcmp(regs[i],str)==0){
      idx=i;
      break;
    }
    if(i==31) *success=false;
  }
  return gpr(idx);
}
