#include "trap.h"
#define SRAM_BASE 0x0f000000L

int main() {
  for(int i=0;i<16;i++){
    int addr = SRAM_BASE + i;
    *(volatile char *)(SRAM_BASE + i) =  addr&0xff;
  }
  for(int i=15;i>=0;i--){
    int addr = SRAM_BASE + i;

    check((addr & 0xff) == *((char *)(SRAM_BASE + i)));
  }

  return 0;
}
