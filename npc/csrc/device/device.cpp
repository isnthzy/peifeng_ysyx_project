#include "../include/npc_common.h"
#define DEVICE_BASE 0xa0000000
#define SERIAL_PORT (DEVICE_BASE + 0x00003f8)
#define RTC_ADDR    (DEVICE_BASE + 0x0000048)
extern void out_of_bound(paddr_t addr);
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

word_t device_read(paddr_t addr){
  switch (addr)
  {
  case RTC_ADDR:
    break;
  
  default:
    out_of_bound(addr);
    break;
  }
  return 0;
}
