#include "../include/npc_common.h"
#include "../include/npc_verilator.h"
extern CPU_state cpu;
static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN={};
extern void putIringbuf();
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

static void out_of_bound(paddr_t addr) {
  IFDEF(CONFIG_ITRACE,putIringbuf()); 
  panic("(npc)address = " FMT_PADDR " is out of bound of pmem [" FMT_PADDR ", " FMT_PADDR "] at pc = " FMT_WORD,
      addr, PMEM_LEFT, PMEM_RIGHT,cpu.pc);
}

word_t pmem_read(paddr_t addr, int len) {
  word_t ret = host_read(guest_to_host(addr), len);
  return ret;
}

void pmem_write(paddr_t addr, int len, word_t data) {
  host_write(guest_to_host(addr), len, data);
}

//----------------------------dpi-c----------------------------
extern "C" void pmem_read(int raddr, int *rdata) {
  *rdata=paddr_read(raddr,4);
  cpu.inst=paddr_read(raddr,4);
  // 总是读取地址为`raddr & ~0x3u`的4字节返回给`rdata`
}
extern "C" void pmem_write(int waddr, int wdata, char wmask) {
  // 总是往地址为`waddr & ~0x3u`的4字节按写掩码`wmask`写入`wdata`
  // `wmask`中每比特表示`wdata`中1个字节的掩码,
  // 如`wmask = 0x3`代表只写入最低2个字节, 内存中的其它字节保持不变
}
//----------------------------dpi-c----------------------------

word_t paddr_read(paddr_t addr, int len) {
  #ifdef CONFIG_MTRACE
  // if(model==1) Log(" r: 0x%x data:0x%08x",addr,pmem_read(addr, len));
  #endif
  if (likely(in_pmem(addr))) return pmem_read(addr, len);
  // IFDEF(CONFIG_DEVICE, return mmio_read(addr, len));
  out_of_bound(addr);
  return 0;
}

void paddr_write(paddr_t addr, int len, word_t data) {
  #ifdef CONFIG_MTRACE
  Log("w: 0x%x data:0x%08x",addr,data);
  #endif
  if (likely(in_pmem(addr))) { pmem_write(addr, len, data); return; }
  // IFDEF(CONFIG_DEVICE, mmio_write(addr, len, data); return);
  out_of_bound(addr);
}
