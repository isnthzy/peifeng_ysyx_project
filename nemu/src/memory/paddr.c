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
      sprintf(mtrace_logbuf,"pc:0x%08x addr:0x%x rdata:0x%08x,len:%d",cpu.pc,addr,pmem_read(addr, len),len);
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
  sprintf(mtrace_logbuf,"pc:0x%08x addr:0x%x wdata:0x%08x len:%d",cpu.pc,addr,data,len);
  enqueueIRingBuffer(&mtrace_buffer,mtrace_logbuf);
  #endif
  if (likely(in_pmem(addr))) { pmem_write(addr, len, data); return; }
  IFDEF(CONFIG_DEVICE, mmio_write(addr, len, data); return);
  #ifndef CONFIG_TARGET_SHARE
    out_of_bound(addr);
  #endif
}

#ifdef CONFIG_TARGET_SHARE //NOTE:只有作为.so时才提供此功能
//store和load队列用来记录load和store，易于进行访存diff
#define STORE_COMMIT_QUEUE_SIZE 16
#define LOAD_COMMIT_QUEUE_SIZE 16
store_commit_t store_commit_queue[STORE_COMMIT_QUEUE_SIZE]; 
load_commit_t  load_commit_queue[LOAD_COMMIT_QUEUE_SIZE]; 
//NOTE:绝对不会超过16个store&load,哪有16发访存啊
int st_head=0, st_tail=0, ld_head=0, ld_tail=0;

void store_commit_queue_push(paddr_t addr,word_t data,int len){
  static bool overflow=false;
  if(overflow){
    return;
  }
  store_commit_t st_commit=store_commit_queue[st_tail];
  if(st_commit.valid){
    printf("[NEMU] [warning]store commit queue overflow,store commit disabled");
  }
  st_commit.valid=true;
  st_commit.addr=addr;
  st_commit.data=data;
  st_commit.len =len;
  st_commit.atpc=cpu.lastpc;
  //随便存一下，都是未对齐的
  st_tail=(st_tail+1)%STORE_COMMIT_QUEUE_SIZE;
}
static store_commit_t* store_commit_queue_pop(){
  store_commit_t* st_commit=store_commit_queue+st_head;
  if(!st_commit->valid){
    return NULL;
  }
  st_head=(st_head+1)%STORE_COMMIT_QUEUE_SIZE;
  return st_commit;
}

bool check_store_commit(paddr_t addr,word_t data,int len){
  store_commit_t* st_commit=store_commit_queue_pop();
  if(st_commit==NULL){
    printf("[NEMU] [warning]store commit queue empty");
    return false;
  }
  if(st_commit->addr!=addr||st_commit->data!=data||st_commit->len!=len){
     printf("ref different at pc = 0x%08x, paddr = "FMT_PADDR", data = "FMT_WORD",\n", 
             st_commit->atpc, st_commit->addr, st_commit->data);
    return false;
  }else{
    return true;
  }
}


//NOTE:load可能有外设操作，因此不data进行对比，对比地址和访问类型
void load_commit_queue_push(paddr_t addr,word_t data,int type){
  static bool overflow=false;
  if(overflow){
    return;
  }
  load_commit_t ld_commit=load_commit_queue[ld_tail];
  if(ld_commit.valid){
    printf("[NEMU] [warning]load commit queue overflow,load commit disabled");
  }
  ld_commit.valid=true;
  ld_commit.addr=addr;
  ld_commit.data=data;
  ld_commit.type=type;
  ld_commit.atpc=cpu.lastpc;
  //随便存一下，都是未对齐的
  ld_tail=(ld_tail+1)%LOAD_COMMIT_QUEUE_SIZE;
}
static load_commit_t* load_commit_queue_pop(){
  load_commit_t* ld_commit=load_commit_queue+ld_head;
  if(!ld_commit->valid){
    return NULL;
  }
  ld_head=(ld_head+1)%LOAD_COMMIT_QUEUE_SIZE;
  return ld_commit;
}

bool check_load_commit(paddr_t addr,int type){
  load_commit_t* ld_commit=load_commit_queue_pop();
  if(ld_commit==NULL){
    printf("[NEMU] [warning]store commit queue empty");
    return false;
  }
  if(ld_commit->addr!=addr||ld_commit->type!=type){
     printf("ref different at pc = 0x%08x, paddr = "FMT_PADDR", data = "FMT_WORD"\n", 
             ld_commit->atpc, ld_commit->addr, ld_commit->data);
    return false;
  }else{
    return true;
  }
}
#endif