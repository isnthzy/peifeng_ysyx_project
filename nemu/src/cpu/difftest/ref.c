/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <isa.h>
#include <cpu/cpu.h>
#include <difftest-def.h>
#include <memory/paddr.h>
void cpu_exec(uint64_t n,bool is_ref);
extern char *regs[];
__EXPORT void difftest_memcpy(paddr_t addr, void *buf, size_t n, bool direction) {
  if(direction == DIFFTEST_TO_REF){ //DIFFTEST_TO_REF,target is ref
    memcpy(guest_to_host(addr), buf, n);
  }else{ //DIFFTEST_TO_DUT,target is dut
    assert(0);
  }
}
//`direction`为`DIFFTEST_TO_DUT`时, 获取REF的寄存器状态到`dut`
//`direction`为`DIFFTEST_TO_REF`时, 设置REF的寄存器状态为`dut`
__EXPORT void difftest_regcpy(void *dut, bool direction) {
  NPC_state* dut_t=dut; 
  if (direction == DIFFTEST_TO_REF) {
    for(int i=0;i<MUXDEF(CONFIG_RVE, 16, 32);i++){
      cpu.gpr[i]=dut_t->regs.gpr[i];
    }
    cpu.pc=dut_t->base.pc;
    cpu.mstatus=dut_t->csr.mstatus;
    cpu.mepc=dut_t->csr.mepc;
    cpu.mtvec=dut_t->csr.mtvec;
    cpu.mcause=dut_t->csr.mcause;

  }else{
    for(int i=0;i<MUXDEF(CONFIG_RVE, 16, 32);i++){
      dut_t->regs.gpr[i]=cpu.gpr[i];
    }
    dut_t->base.pc=cpu.pc;
    dut_t->csr.mstatus=cpu.mstatus;
    dut_t->csr.mepc=cpu.mepc;
    dut_t->csr.mtvec=cpu.mtvec;
    dut_t->csr.mcause=cpu.mcause;
  }
  // assert(0);
}

__EXPORT void difftest_exec(uint64_t n) {
  cpu_exec(n,DIFFTEST_TO_REF);
  // assert(0);
}

__EXPORT void difftest_raise_intr(word_t NO) {
  // assert(0);
}

__EXPORT void difftest_ref_reg_display() {
  fflush(NULL);
  puts("\n=================================  REF  Regs  =================================");
  for (int i = 0; i < MUXDEF(CONFIG_RVE, 16, 32); i += 4) {
    printf("%s(r%2d): 0x%08x %s(r%2d): 0x%08x %s(r%2d): 0x%08x %s(r%2d): 0x%08x \n", 
      regs[i]  , i  , cpu.gpr[i]  ,regs[i+1], i+1, cpu.gpr[i+1],
      regs[i+2], i+2, cpu.gpr[i+2],regs[i+3], i+3, cpu.gpr[i+3]);
  }
  printf("pc: 0x%08x inst: 0x%08x \n", cpu.pc, 0);
  printf("MSTATUS: 0x%08x, MTVEC: 0x%08x, MEPC: 0x%08x \n", cpu.mstatus, cpu.mtvec, cpu.mepc);
  printf(" MCAUSE: 0x%08x \n", cpu.mcause);
  fflush(NULL);
}

__EXPORT void difftest_init(int port) {
  void init_mem();
  init_mem();
  /* Perform ISA dependent initialization. */
  init_isa();
}
