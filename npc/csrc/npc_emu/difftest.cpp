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

#include <dlfcn.h>
#include "../include/npc_common.h"
#define DIFFTEST_TO_REF 1
#define DIFFTEST_TO_DUT 0
#define DIFF_CHECK(addr1, addr2, atpc,name) if(addr1!=addr2){\
  wLog("The %s is different\nref:0x%08x dut:0x%08x",name,addr1,addr2); \
  wLog("at pc:0x%08x",atpc); \
  return false;\
}
uint8_t* guest_to_host(paddr_t paddr);
void reg_dut_display();
int check_reg_idx(int idx);
extern CPU_state cpu;
extern const char *regs[];
void (*ref_difftest_memcpy)(paddr_t addr, void *buf, size_t n, bool direction) = NULL;
void (*ref_difftest_regcpy)(void *dut, bool direction) = NULL;
void (*ref_difftest_exec)(uint64_t n) = NULL;
void (*ref_difftest_raise_intr)(uint64_t NO) = NULL;
#ifdef CONFIG_DIFFTEST
bool isa_difftest_checkregs(CPU_state *ref_r,vaddr_t pc,vaddr_t npc){
  for(int i=0;i<MUXDEF(CONFIG_RVE, 16, 32);i++){
    if(ref_r->gpr[i]!=gpr(i)){
          /*真机的pc要慢一拍,因为nemu的寄存器写入是瞬间写，npc是延迟一拍后写
          所以要用pc指示相应的reg不同*/
      wLog("The reg:%s(rf_%d) is different\nref:0x%08x dut:0x%08x",regs[i],i,ref_r->gpr[i],gpr(i));
      wLog("at pc:0x%08x",pc);
      return false;
    }
  }
  DIFF_CHECK(ref_r->pc,pc, pc,"pc");
  DIFF_CHECK(ref_r->mtvec,cpu.mtvec, pc,"mtvec");
  DIFF_CHECK(ref_r->mepc ,cpu.mepc , pc,"mepc ");
  DIFF_CHECK(ref_r->mstatus,cpu.mstatus, pc,"mstatus"); //mret实现不完整
  DIFF_CHECK(ref_r->mcause ,cpu.mcause , pc,"mcause");
  return true;
}


static bool is_skip_ref = false;
static int skip_dut_nr_inst = 0;

// this is used to let ref skip instructions which
// can not produce consistent behavior with NEMU
void difftest_skip_ref() {
  is_skip_ref = true;
  // // If such an instruction is one of the instruction packing in QEMU
  // // (see below), we end the process of catching up with QEMU's pc to
  // // keep the consistent behavior in our best.
  // // Note that this is still not perfect: if the packed instructions
  // // already write some memory, and the incoming instruction in NEMU
  // // will load that memory, we will encounter false negative. But such
  // // situation is infrequent.
  skip_dut_nr_inst = 0;
}

// this is used to deal with instruction packing in QEMU.
// Sometimes letting QEMU step once will execute multiple instructions.
// We should skip checking until NEMU's pc catches up with QEMU's pc.
// The semantic is
//   Let REF run `nr_ref` instructions first.
//   We expect that DUT will catch up with REF within `nr_dut` instructions.
void difftest_skip_dut(int nr_ref, int nr_dut) {
  skip_dut_nr_inst += nr_dut;

  while (nr_ref -- > 0) {
    ref_difftest_exec(1);
  }
}

void init_difftest(char *ref_so_file, long img_size, int port) {
  assert(ref_so_file != NULL);

  void *handle;
  handle = dlopen(ref_so_file, RTLD_LAZY);
  assert(handle);

  ref_difftest_memcpy = (void (*)(paddr_t, void*, size_t, bool))dlsym(handle, "difftest_memcpy");
  assert(ref_difftest_memcpy);

  ref_difftest_regcpy = (void (*)(void *, bool))dlsym(handle, "difftest_regcpy");
  assert(ref_difftest_regcpy);

  ref_difftest_exec =  (void (*)(uint64_t))dlsym(handle, "difftest_exec");
  assert(ref_difftest_exec);

  // ref_difftest_raise_intr = dlsym(handle, "difftest_raise_intr");
  // assert(ref_difftest_raise_intr);

  void (*ref_difftest_init)(int) = (void (*)(int))dlsym(handle, "difftest_init");
  assert(ref_difftest_init);

  Log("Differential testing: %s", ANSI_FMT("ON", ANSI_FG_GREEN));
  Log("The result of every instruction will be compared with %s. "
      "This will help you a lot for debugging, but also significantly reduce the performance. "
      "If it is not necessary, you can turn it off in menuconfig.", ref_so_file);

  ref_difftest_init(port);
  ref_difftest_memcpy(RESET_VECTOR, guest_to_host(RESET_VECTOR), img_size, DIFFTEST_TO_REF);
  ref_difftest_regcpy(&cpu, DIFFTEST_TO_REF);
}
void reg_ref_display(CPU_state *ref_r){
  printf("ref->mstatus:0x%08x\nref->mepc   :0x%08x\nref->mtvec  :0x%08x\nref->mcause :0x%08x\n",\
  ref_r->mstatus,ref_r->mepc,ref_r->mtvec,ref_r->mcause);
  int i;
  printf("name   value   name   value   name   value   name   value\n");
  for(i=0;i<MUXDEF(CONFIG_RVE, 16, 32);i+=4){
    printf("%3s 0x%08x %3s 0x%08x %3s 0x%08x %3s 0x%08x\n",\
    regs[i],ref_r->gpr[i],regs[i+1],ref_r->gpr[i+1],regs[i+2],ref_r->gpr[i+2],regs[i+3],ref_r->gpr[i+3]);
  }
  // printf("pc:%x\n",ref_r->pc);
}
static void checkregs(CPU_state *ref, vaddr_t pc,vaddr_t npc) {
  if (!isa_difftest_checkregs(ref, pc, npc)) {
    npc_state.state = NPC_ABORT;
    npc_state.halt_pc = npc;
    puts("----------------------------ref----------------------------");
    reg_ref_display(ref);
    puts("----------------------------dut----------------------------");
    reg_dut_display();
  }
}

void difftest_step(vaddr_t pc, vaddr_t npc) {
  CPU_state ref_r;

  if (skip_dut_nr_inst > 0) {
    ref_difftest_regcpy(&ref_r, DIFFTEST_TO_DUT);
    if (ref_r.pc == npc) {
      skip_dut_nr_inst = 0;
      checkregs(&ref_r, pc, npc);
      return;
    }
    skip_dut_nr_inst --;
    if (skip_dut_nr_inst == 0)
      panic("can not catch up with ref.pc = " FMT_WORD " at pc = " FMT_WORD, ref_r.pc, npc);
    return;
  }

  if (is_skip_ref) {
    // to skip the checking of an instruction, just copy the reg state to reference design
    ref_difftest_regcpy(&cpu, DIFFTEST_TO_REF);
    is_skip_ref = false;
    return;
  }

  ref_difftest_exec(1);
  ref_difftest_regcpy(&ref_r, DIFFTEST_TO_DUT);
  checkregs(&ref_r, pc, npc);
}
#endif
#ifndef CONFIG_DIFFTEST
void init_difftest(char *ref_so_file, long img_size, int port) {}
#endif