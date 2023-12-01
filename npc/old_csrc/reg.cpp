#include "npc_common.h"
#include "npc_reg.h"
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

// word_t isa_reg_str2val(const char *s, bool *success) {
//   int idx=0;
//   char str[10];
//   strcpy(str,s+1); //去除最左边的$
//   if(strcmp(str,"pc")==0) return cpu.pc; //实现断点
//   for(int i=0;i<32;i++){
//     if(strcmp(regs[i],str)==0){
//       idx=i;
//       break;
//     }
//     if(i==31) *success=false;
//   }
//   return gpr(idx);
// }
