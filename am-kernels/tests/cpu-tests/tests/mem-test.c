#include "trap.h"
#define SRAM_BASE 0x0f000000L

int main() {
  for(int i=0;i<16;i++){
    int addr = SRAM_BASE + i;
    *(volatile char *)(SRAM_BASE + i) =  addr & 0xff;
  }
  for(int i=15;i>=0;i--){
    int addr = SRAM_BASE + i;
    check((addr & 0xff) == *((char *)(SRAM_BASE + i)));
  }

  for(int i=16;i<32;i++){
    int addr = SRAM_BASE + i;
    *(volatile short int *)(SRAM_BASE + i) =  addr & 0xffff;
  }
  for(int i=31;i>=16;i--){
    int addr = SRAM_BASE + i;
    check((addr & 0xffff) == *((short int *)(SRAM_BASE + i)));
  }

  // for(int i=32;i<48;i++){
  //   int addr = SRAM_BASE + i;
  //   *(volatile int *)(SRAM_BASE + i) =  addr & 0xffffffff;
  // }
  // for(int i=47;i>=32;i--){
  //   int addr = SRAM_BASE + i;

  //   check((addr & 0xffffffff) == *((int *)(SRAM_BASE + i)));
  // }


  return 0;
}
