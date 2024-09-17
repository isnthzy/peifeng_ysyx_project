#include "../include/npc_common.h"
#include "../include/npc_verilator.h"
#include "../include/util/iringbuf.h"
#include "../include/npc/npc_memory.h"
#include "../include/npc/npc_device.h"
// extern CPU_state cpu;
// extern CPU_info cpu_info;
static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN={};
// static uint8_t pmem[CONFIG_MSIZE];


void init_mem() {
  IFDEF(CONFIG_MEM_RANDOM, memset(pmem, rand(), CONFIG_MSIZE));
  Log("physical memory area [" FMT_PADDR ", " FMT_PADDR "]", PMEM_LEFT, PMEM_RIGHT);
}

static inline uint32_t host_read(void *addr, int len) {
  switch (len) {
    case 1: return *(uint8_t  *)addr;
    case 2: return *(uint16_t *)addr;
    case 4: return *(uint32_t *)addr;
    default:  return 0;
  }
}
static inline void host_write(void *addr, int len, uint32_t data) {
  switch (len) {
    case 1: *(uint8_t  *)addr = data; return;
    case 2: *(uint16_t *)addr = data; return;
    case 4: *(uint32_t *)addr = data; return;
    default: return;
  }
}

static inline bool in_pmem(paddr_t addr) {
  return addr - CONFIG_MBASE < CONFIG_MSIZE;
}

uint8_t* guest_to_host(paddr_t paddr) { return pmem + paddr - CONFIG_MBASE; }
paddr_t host_to_guest(uint8_t *haddr) { return haddr - pmem + CONFIG_MBASE; }

word_t pmem_read(paddr_t addr, int len) {
  word_t ret = host_read(guest_to_host(addr), len);
  return ret;
}

void pmem_write(paddr_t addr, int len, word_t data) {
  host_write(guest_to_host(addr), len, data);
}

void mputIringbuf(){
  while(!isIRingBufferEmpty(&mtrace_buffer)){
    char pop_iringbufdata[100];
    dequeueIRingBuffer(&mtrace_buffer,pop_iringbufdata);
    if(mtrace_buffer.num==0) wLog("[mtrace]-->%s",pop_iringbufdata);
    else wLog("[mtrace]   %s",pop_iringbufdata);
  }
}

void out_of_bound(paddr_t addr) {
  IFDEF(CONFIG_ITRACE,putIringbuf()); 
  panic("(npc)address = " FMT_PADDR " is out of bound of pmem [" FMT_PADDR ", " FMT_PADDR "]",
      addr, PMEM_LEFT, PMEM_RIGHT);
}
//----------------------------dpi-c----------------------------

extern "C" int pmem_read(int raddr) {
  word_t rdata=paddr_read(raddr,4);
  // printf("raddr:%08x rdata:%08x\n",raddr,rdata);
  return rdata;
  // 总是读取地址为`raddr & ~0x3u`的4字节返回给`rdata`
}
extern "C" void pmem_write(int waddr, int wdata, char wmask) {
  // int st_addr = waddr + ((wmask & 0x2) >> 1) + ((wmask & 0x4) >> 2) * 2 + ((wmask & 0x8) >> 3) * 3;
  // int st_len = (wmask & 0x1) + ((wmask & 0x2) >> 1) + ((wmask & 0x4) >> 2) + ((wmask & 0x8) >> 3);
  // int st_data = (wdata >> (8 * ((wmask & 0x2) >> 1 + (wmask & 0x4) >> 2 * 2 + (wmask & 0x8) >> 3 * 3))) & ((1 << (st_len * 8)) - 1);
  //用了笨方法枚举，暂时没想到什么合适的办法
  int st_addr=0;
  int st_len=0;
  int st_data=0;
  switch (wmask)
  {
  //lb
  case 0x1: 
    st_addr=waddr;
    st_data=wdata&0xff;
    st_len=1; break;
  case 0x2:
    st_addr=waddr+1;
    st_data=(wdata>>8)&0xff;
    st_len=1; break;
  case 0x4:
    st_addr=waddr+2;
    st_data=(wdata>>16)&0xff;
    st_len=1; break;
  case 0x8:
    st_addr=waddr+3;
    st_data=(wdata>>24)&0xff;
    st_len=1; break;
  //lh
  case 0x3:
    st_addr=waddr;
    st_data=wdata&0xffff;
    st_len=2; break;
  case 0xc:
    st_addr=waddr+2;
    st_data=(wdata>>16)&0xffff;
    st_len=2; break;
  //lw
  case 0xf:
    st_addr=waddr;
    st_data=wdata;
    st_len=4; break;
  default:
    panic("load error\n");
    break;
  }
  // printf("waddr:%08x wdata:%08x wlen:%08x\n",st_addr,st_data,st_len);
  paddr_write(st_addr,st_len,st_data);
  // 总是往地址为`waddr & ~0x3u`的4字节按写掩码`wmask`写入`wdata`
  // `wmask`中每比特表示`wdata`中1个字节的掩码,
  // 如`wmask = 0x3`代表只写入最低2个字节, 内存中的其它字节保持不变
}

extern "C" void flash_read(int32_t addr, int32_t *data) { assert(0); }
extern "C" void mrom_read(int32_t addr, int32_t *data) { 
  // word_t data=paddr_read(addr,4);
  *data=0x00100073;
  printf("%x %p",addr,data);
}

//----------------------------dpi-c----------------------------


void mtrace_store(int pc,int addr,int data,int len){
  #ifdef CONFIG_MTRACE
  char mtrace_logbuf[120];
  sprintf(mtrace_logbuf,"[store]pc:0x%08x addr:0x%x wdata:0x%08x len:%d",pc,addr,data,len);
  enqueueIRingBuffer(&mtrace_buffer,mtrace_logbuf);
  #endif
}
void mtrace_load (int pc,int addr,int data,int len){
  #ifdef CONFIG_MTRACE //警惕切换riscv64会造成的段错误
  char mtrace_logbuf[120];
  sprintf(mtrace_logbuf,"[load ]pc:0x%08x addr:0x%x rdata:0x%08x len:%d",pc,addr,data,len);
  enqueueIRingBuffer(&mtrace_buffer,mtrace_logbuf);
  #endif
}
static uint64_t read_cnt=0;
word_t paddr_read(paddr_t addr, int len) {

  word_t pmem_rdata;
  if (likely(in_pmem(addr))) pmem_rdata=pmem_read(addr,4);

  if (likely(in_pmem(addr))) return pmem_rdata;
  if(addr>=0xa0000000){
    return device_read(addr,len);
  }
  out_of_bound(addr);
  return 0;
}
static uint64_t write_cnt=0;
void paddr_write(paddr_t addr, int len, word_t data) {
  if (likely(in_pmem(addr))) { pmem_write(addr, len, data); return; }
  if(addr>=0xa0000000){
    device_write(addr,len,data);
    return;
  }
  
  out_of_bound(addr);
}
