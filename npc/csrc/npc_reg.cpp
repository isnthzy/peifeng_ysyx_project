#include "include/npc_common.h"
#include "include/npc_verilator.h"
  uint32_t rf_array[32] = {
    top->rootp->SimTop__DOT__RegFile__DOT__rf_0,
    top->rootp->SimTop__DOT__RegFile__DOT__rf_1,
    top->rootp->SimTop__DOT__RegFile__DOT__rf_2,
    top->rootp->SimTop__DOT__RegFile__DOT__rf_3,
    top->rootp->SimTop__DOT__RegFile__DOT__rf_31
  };
uint32_t read_reg_idx(int idx) {
  // uint32_t rf_tmp;
  if (idx >= 0 && idx < 32) {
    return rf_array[idx];
  } else {
    // 处理索引无效的情况，可以返回默认值或者抛出错误
    // 这里示范返回 0，你可以根据实际需求进行修改
    return 0;
  }
  // if(idx==0) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_0;
  // else if(idx==1)  rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_1;
  // else if(idx==2)  rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_2;
  // else if(idx==3)  rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_3;
  // else if(idx==4)  rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_4;
  // else if(idx==5)  rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_5;
  // else if(idx==6)  rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_6;
  // else if(idx==7)  rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_7;
  // else if(idx==8)  rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_8;
  // else if(idx==9)  rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_9;
  // else if(idx==10) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_10;
  // else if(idx==11) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_11;
  // else if(idx==12) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_12;
  // else if(idx==13) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_13;
  // else if(idx==14) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_14;
  // else if(idx==15) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_15;
  // else if(idx==16) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_16;
  // else if(idx==17) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_17;
  // else if(idx==18) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_18;
  // else if(idx==19) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_19;
  // else if(idx==20) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_20;
  // else if(idx==21) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_21;
  // else if(idx==22) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_22;
  // else if(idx==23) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_23;
  // else if(idx==24) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_24;
  // else if(idx==25) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_25;
  // else if(idx==26) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_26;
  // else if(idx==27) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_27;
  // else if(idx==28) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_28;
  // else if(idx==29) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_29;
  // else if(idx==30) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_30;
  // else if(idx==31) rf_tmp=top->rootp->SimTop__DOT__RegFile__DOT__rf_31;
  // return rf_tmp;
}


static inline int check_reg_idx(int idx) {
  assert(idx >= 0 && idx <32);
  return idx;
}



const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

void reg_display(){
  int i;
  printf("name   value   name   value   name   value   name   value\n");
  for(i=0;i<32;i+=4){
    printf("%3s 0x%08x %3s 0x%08x %3s 0x%08x %3s 0x%08x\n",\
    regs[i],gpr(i),regs[i+1],gpr(i+1),regs[i+2],gpr(i+2),regs[i+3],gpr(i+3));
  }
  
}

word_t isa_reg_str2val(const char *s, bool *success) {
  int idx=0;
  char str[10];
  strcpy(str,s+1); //去除最左边的$
  if(strcmp(str,"pc")==0) return top->io_pc; //实现断点
  for(int i=0;i<32;i++){
    if(strcmp(regs[i],str)==0){
      idx=i;
      break;
    }
    if(i==31) *success=false;
  }
  return gpr(idx);
}
