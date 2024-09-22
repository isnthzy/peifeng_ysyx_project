#include "trap.h"
#define FLASH_BASE 0x30000000L

int main(){
  for(int i=0;i<30;i++){
    volatile char* ch= (volatile char *)(FLASH_BASE + i);
    putch((*ch));
  }
}