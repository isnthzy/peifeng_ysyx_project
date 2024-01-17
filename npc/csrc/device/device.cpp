#include "../include/npc_common.h"
#define DEVICE_BASE 0xa0000000
#define SERIAL_PORT (DEVICE_BASE + 0x00003f8)
#define RTC_ADDR    (DEVICE_BASE + 0x0000048)
#ifndef CONFIG_DIFFTEST
void difftest_skip_ref(){}
#endif
extern CPU_state cpu;
extern uint64_t get_time();
extern void out_of_bound(paddr_t addr);
extern void difftest_skip_ref();
void  device_write(paddr_t addr,word_t data){
  switch (addr)
  {
  case SERIAL_PORT:
    putchar(data);
    break;

  default:
    out_of_bound(addr);
    break;
  }
}
uint64_t rtc_us=0;
word_t device_read(paddr_t addr){
  switch (addr)
  {
  case RTC_ADDR:
  case RTC_ADDR+4:
    difftest_skip_ref();
    rtc_us=get_time();
    if(addr==RTC_ADDR)   return (uint32_t)rtc_us;
    if(addr==RTC_ADDR+4) return rtc_us >> 32;
    
    break;

  default:
    out_of_bound(addr);
    break;
  }
  return 0;
}
