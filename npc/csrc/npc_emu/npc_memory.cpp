#include "../include/npc_common.h"
#include "../include/npc_verilator.h"
#include "../include/util/iringbuf.h"
#include "../include/npc/npc_memory.h"
#include "../include/npc/npc_device.h"
// extern CPU_state cpu;
// extern CPU_info cpu_info;
static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN={};
static uint8_t mrom[CONFIG_MSIZE] PG_ALIGN={};
static uint8_t flash_ram[CONFIG_SOC_FLASH_SIZE] PG_ALIGN = {};
static uint8_t psram[CONFIG_SOC_PSRAM_SIZE] PG_ALIGN = {};

static inline int in_soc_device(paddr_t addr) {
  int in_device_num=0;
  if(addr - CONFIG_MBASE < CONFIG_MSIZE) in_device_num=SOC_PMEM;
  if(addr - CONFIG_SOC_MROM_BASE  < CONFIG_SOC_MROM_SIZE) in_device_num=SOC_DEVICE_MROM;
  if(addr - CONFIG_SOC_FLASH_BASE < CONFIG_SOC_FLASH_SIZE) in_device_num=SOC_DEVICE_FLASH;
  if(addr - CONFIG_SOC_PSRAM_BASE < CONFIG_SOC_PSRAM_SIZE) in_device_num=SOC_DEVICE_PSRAM;
  return in_device_num;
}

// void init_flash_ram(){
//   const uint8_t falsh_defaultImg [] = {
//     'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p',
//     'q','r','s','t','u','v','w','x','y','z','0','1','2','3','4','5',
//   }; 
//   memcpy(guest_to_host(CONFIG_SOC_FLASH_BASE), falsh_defaultImg, sizeof(falsh_defaultImg));
// }

void init_mem() {
  IFDEF(CONFIG_MEM_RANDOM, memset(pmem, rand(), CONFIG_MSIZE));
  // Log("physical memory area [" FMT_PADDR ", " FMT_PADDR "]", PMEM_LEFT, PMEM_RIGHT);

  // init_flash_ram();
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
  if(in_soc_device(addr)) return true;
  else return false;
}

uint8_t* guest_to_host(paddr_t paddr) {
  uint8_t *ret;
  switch (in_soc_device(paddr)){
    case SOC_DEVICE_ERROR:
      panic("SOC_DEVICE_ERROR: %d", paddr);
      break;
    case SOC_PMEM:
      ret = pmem + paddr - CONFIG_MBASE;
      break;
    case SOC_DEVICE_MROM:
      ret = pmem + paddr - CONFIG_SOC_MROM_BASE;
      break;
    case SOC_DEVICE_FLASH:
      ret = flash_ram + paddr - CONFIG_SOC_FLASH_BASE;
      break;
    case SOC_DEVICE_PSRAM:
      ret = psram + paddr - CONFIG_SOC_PSRAM_BASE;
      break;
    default:
      panic("unknown device 0x%08x", paddr);
  }
  return ret; 
}
// paddr_t host_to_guest(uint8_t *haddr) { 
//   return haddr - pmem + CONFIG_MBASE; 
// }

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

void mem_write_wapper(int waddr, int wdata, char wmask){
  // int st_len = (wmask & 0x1) + ((wmask & 0x2) >> 1) + ((wmask & 0x4) >> 2) + ((wmask & 0x8) >> 3);
  // int st_data = (wdata >> (8 * ((wmask & 0x2) >> 1 + (wmask & 0x4) >> 2 * 2 + (wmask & 0x8) >> 3 * 3))) & ((1 << (st_len * 8)) - 1);
  //用了笨方法枚举，暂时没想到什么合适的办法
  int st_addr=waddr; //不需要进行对齐
  int st_len=0;
  int st_data=0;
  switch (wmask)
  {
  //lb
  case 0x1: 
    st_data=wdata&0xff;
    st_len=1; break;
  case 0x2:
    st_data=(wdata>>8)&0xff;
    st_len=1; break;
  case 0x4:
    st_data=(wdata>>16)&0xff;
    st_len=1; break;
  case 0x8:
    st_data=(wdata>>24)&0xff;
    st_len=1; break;
  //lh
  case 0x3:
    st_data=wdata&0xffff;
    st_len=2; break;
  case 0xc:
    st_data=(wdata>>16)&0xffff;
    st_len=2; break;
  //lw
  case 0xf:
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
//----------------------------dpi-c----------------------------

extern "C" int pmem_read(int raddr) {
  int ld_addr = raddr & ~0x3u;
  word_t ld_rdata=paddr_read(ld_addr,4);
  return ld_rdata;
  // 总是读取地址为`raddr & ~0x3u`的4字节返回给`rdata`
}
extern "C" void pmem_write(int waddr, int wdata, char wmask) {
  mem_write_wapper(waddr,wdata,wmask);
}

extern "C" void flash_read(int32_t addr, int32_t *data) {
  int ld_addr = (addr + CONFIG_SOC_FLASH_BASE) & ~0x3u;
  //NOTE:flash的地址没有高位，需要手动补上base，再交给x转发分配
  *data=paddr_read(ld_addr,4);
  // 总是读取地址为`raddr & ~0x3u`的4字节返回给`rdata`
}
extern "C" int32_t mrom_read(int32_t addr) { 
  int ld_addr = addr & ~0x3u;
  word_t ld_rdata=paddr_read(ld_addr,4);
  return ld_rdata;
  // 总是读取地址为`raddr & ~0x3u`的4字节返回给`rdata`
}

extern "C" void psram_write(int32_t addr, int32_t data, char wlen) {
  int waddr = (addr + CONFIG_SOC_PSRAM_BASE);
  paddr_write(waddr,wlen,data);
}

extern "C" void psram_read(int32_t addr,int *data) {
  int ld_addr = (addr + CONFIG_SOC_PSRAM_BASE) & ~0x3u;
  //NOTE:flash的地址没有高位，需要手动补上base，再交给x转发分配
  *data=paddr_read(ld_addr,4);
  // 总是读取地址为`raddr & ~0x3u`的4字节返回给`rdata`
}

static uint8_t sdram_bank[8][8192][1024]={};

extern "C" void sdrambank_read(uint16_t row,uint16_t col,uint16_t *data,
                               uint8_t bank,uint8_t dqm){
  uint16_t result = sdram_bank[bank][row][(col << 1) + 1] << 8 | sdram_bank[bank][row][col << 1];
  printf("low 0x%02x high 0x%02x\n",(sdram_bank[bank][row][col << 1]),(sdram_bank[bank][row][(col << 1) + 1] << 8));
  printf("row 0x%04x col 0x%04x rdata 0x%04x bank 0x%02x\n",row,col,result,bank);
  *data=result;
}

extern "C" void sdrambank_write(uint16_t row,uint16_t col,uint16_t data,
                                uint8_t bank,uint8_t dqm){
  printf("row 0x%04x col 0x%04x data 0x%04x bank 0x%02x\n",row,col,data,bank);
  switch (dqm)
  {
  case 0x0:
    sdram_bank[bank][row][(col << 1) + 1] = (data >> 8) & 0xff;  //high
    printf("%x \n",(sdram_bank[bank][row][(col << 1) + 1]));
    sdram_bank[bank][row][(col << 1)] = data & 0xff; //low
    printf("%x \n",(sdram_bank[bank][row][(col << 1)]));

    break;
  case 0x1:
    sdram_bank[bank][row][(col << 1)] = data & 0xff; //low
    break;
  case 0x2:
    sdram_bank[bank][row][(col << 1) + 1] = (data >> 8) && 0xff;  //high
    break;
  default:
    break;
  }

}
//----------------------------dpi-c----------------------------


void mtrace_store(int pc,int addr,int data,int len){
#ifdef CONFIG_TRACE
#ifdef CONFIG_MTRACE
  char mtrace_logbuf[120];
  sprintf(mtrace_logbuf,"[store]pc:0x%08x addr:0x%08x wdata:0x%08x len:%d",pc,addr,data,len);
  enqueueIRingBuffer(&mtrace_buffer,mtrace_logbuf);
#endif
#endif
}
void mtrace_load (int pc,int addr,int data,int len){
#ifdef CONFIG_TRACE
#ifdef CONFIG_MTRACE //警惕切换riscv64会造成的段错误
  char mtrace_logbuf[120];
  sprintf(mtrace_logbuf,"[load ]pc:0x%08x addr:0x%08x rdata:0x%08x len:%d",pc,addr,data,len);
  enqueueIRingBuffer(&mtrace_buffer,mtrace_logbuf);
#endif
#endif
}
static uint64_t read_cnt=0;
word_t paddr_read(paddr_t addr, int len) {
  word_t pmem_rdata;
  if (likely(in_pmem(addr))){
    pmem_rdata=pmem_read(addr,4);
    return pmem_rdata;
  }
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
