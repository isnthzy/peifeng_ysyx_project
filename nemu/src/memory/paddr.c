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

#include <memory/host.h>
#include <memory/paddr.h>
#include <device/mmio.h>
#include <isa.h>
#include "../cpu/iringbuf.h"
extern IRingBuffer iring_buffer;
extern IRingBuffer mtrace_buffer;
#if   defined(CONFIG_PMEM_MALLOC)
static uint8_t *pmem = NULL;
#else // CONFIG_PMEM_GARRAY
static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN = {};
#endif

uint8_t* guest_to_host(paddr_t paddr) { return pmem + paddr - CONFIG_MBASE; }
paddr_t host_to_guest(uint8_t *haddr) { return haddr - pmem + CONFIG_MBASE; }

static word_t pmem_read(paddr_t addr, int len) {
  word_t ret = host_read(guest_to_host(addr), len);
  return ret;
}

static void pmem_write(paddr_t addr, int len, word_t data) {
  host_write(guest_to_host(addr), len, data);
}

extern void iputIringbuf();
extern void dputIringbuf();
void mputIringbuf(){
  while(!isIRingBufferEmpty(&mtrace_buffer)){
    char pop_iringbufdata[100];
    dequeueIRingBuffer(&mtrace_buffer,pop_iringbufdata);
    if(mtrace_buffer.num==0) wLog("[mtrace]-->%s",pop_iringbufdata);
    else wLog("[mtrace]   %s",pop_iringbufdata);
  }
}

#ifndef CONFIG_TARGET_SHARE
static void out_of_bound(paddr_t addr) {
  iputIringbuf();
  mputIringbuf();
  dputIringbuf();
  panic("(nemu)address = " FMT_PADDR " is out of bound of pmem [" FMT_PADDR ", " FMT_PADDR "] at pc = " FMT_WORD,
      addr, PMEM_LEFT, PMEM_RIGHT, cpu.pc);
}
#endif

void init_mem() {
#if   defined(CONFIG_PMEM_MALLOC)
  pmem = malloc(CONFIG_MSIZE);
  assert(pmem);
#endif
  IFDEF(CONFIG_MEM_RANDOM, memset(pmem, rand(), CONFIG_MSIZE));
  Log("physical memory area [" FMT_PADDR ", " FMT_PADDR "]", PMEM_LEFT, PMEM_RIGHT);
}

word_t paddr_read(paddr_t addr, int len,int model) {
  #ifdef CONFIG_MTRACE //警惕切换riscv64会造成的段错误
  if(model==1){
    if(likely(in_pmem(addr))){
      char mtrace_logbuf[120];
      sprintf(mtrace_logbuf,"pc:0x%08x addr:0x%x rdata:0x%08x",cpu.pc,addr,pmem_read(addr, len));
      enqueueIRingBuffer(&mtrace_buffer,mtrace_logbuf);
    }
  }
  #endif
  if (likely(in_pmem(addr))) return pmem_read(addr, len);
  IFDEF(CONFIG_DEVICE, return mmio_read(addr, len););
  #ifndef CONFIG_TARGET_SHARE
    out_of_bound(addr);
  #endif
  // IFNDEF(CONFIG_TARGET_SHARE,out_of_bound(addr));
  //如果没有定义difftest模式就用out of bound检查越界
  return 0;
}

void paddr_write(paddr_t addr, int len, word_t data) {
  #ifdef CONFIG_MTRACE
  char mtrace_logbuf[120];
  sprintf(mtrace_logbuf,"pc:0x%08x addr:0x%x wdata:0x%08x",cpu.pc,addr,data);
  enqueueIRingBuffer(&mtrace_buffer,mtrace_logbuf);
  #endif
  if (likely(in_pmem(addr))) { pmem_write(addr, len, data); return; }
  IFDEF(CONFIG_DEVICE, mmio_write(addr, len, data); return);
  #ifndef CONFIG_TARGET_SHARE
    out_of_bound(addr);
  #endif
}
