#include "../include/npc_common.h"
#include "../include/npc_verilator.h"
#include "../include/iringbuf.h"
word_t paddr_read(paddr_t addr, int len,int model);
void paddr_write(paddr_t addr, int len, word_t data);
extern CPU_state cpu;
static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN={};
// static uint8_t pmem[CONFIG_MSIZE];
extern IRingBuffer mtrace_buffer;

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

extern void putIringbuf();
void mputIringbuf(){
  while(!isIRingBufferEmpty(&mtrace_buffer)){
    char pop_iringbufdata[100];
    dequeueIRingBuffer(&mtrace_buffer,pop_iringbufdata);
    if(mtrace_buffer.num==0) wLog("[mtrace]-->%s",pop_iringbufdata);
    else wLog("[mtrace]   %s",pop_iringbufdata);
  }
}

static void out_of_bound(paddr_t addr) {
  IFDEF(CONFIG_ITRACE,putIringbuf()); 
  panic("(npc)address = " FMT_PADDR " is out of bound of pmem [" FMT_PADDR ", " FMT_PADDR "] at pc = " FMT_WORD,
      addr, PMEM_LEFT, PMEM_RIGHT,cpu.pc);
}
//----------------------------dpi-c----------------------------
extern "C" void get_inst(int raddr, int *rdata) {
  *rdata=paddr_read(raddr,4,0);
  cpu.inst=*rdata;
  // 总是读取地址为`raddr & ~0x3u`的4字节返回给`rdata`
}
extern "C" void pmem_read(int raddr, int *rdata) {
  *rdata=paddr_read(raddr,4,1);
  // 总是读取地址为`raddr & ~0x3u`的4字节返回给`rdata`
}
extern "C" void pmem_write(int waddr, int wdata, char wmask) {
  // waddr=waddr & ~0x3u;
  if(wmask==0x1) paddr_write(waddr,1,wdata);
  else if(wmask==0x3) paddr_write(waddr,2,wdata);
  else if(wmask==0xf) paddr_write(waddr,4,wdata);
  // 总是往地址为`waddr & ~0x3u`的4字节按写掩码`wmask`写入`wdata`
  // `wmask`中每比特表示`wdata`中1个字节的掩码,
  // 如`wmask = 0x3`代表只写入最低2个字节, 内存中的其它字节保持不变
}
//----------------------------dpi-c----------------------------

word_t paddr_read(paddr_t addr, int len,int model) {
  word_t pmem_rdata;
  if (likely(in_pmem(addr))) pmem_rdata=pmem_read(addr,4);
  #ifdef CONFIG_MTRACE //警惕切换riscv64会造成的段错误
  if(model==1){
    if(likely(in_pmem(addr))){
      char mtrace_logbuf[120];
      sprintf(mtrace_logbuf,"pc:0x%08x addr:0x%x rdata:0x%08x",cpu.nextpc,addr,pmem_rdata);
      enqueueIRingBuffer(&mtrace_buffer,mtrace_logbuf);
      printf("%s\n",mtrace_logbuf);
    }
  }
  #endif
  if (likely(in_pmem(addr))) return pmem_rdata;
  // printf("222222222222222 %x\n",addr);
  out_of_bound(addr);
  return 0;
}

void paddr_write(paddr_t addr, int len, word_t data) {
  #ifdef CONFIG_MTRACE
  char mtrace_logbuf[120];
  sprintf(mtrace_logbuf,"pc:0x%08x addr:0x%x wdata:0x%08x len:%d",cpu.nextpc,addr,data,len);
  enqueueIRingBuffer(&mtrace_buffer,mtrace_logbuf);
  printf("%s\n",mtrace_logbuf);
  #endif
  if (likely(in_pmem(addr))) { pmem_write(addr, len, data); return; }
  
  // if(addr==0xa00003f8){putchar('c'); return;}
  out_of_bound(addr);
}
