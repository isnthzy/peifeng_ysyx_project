#include "trap.h"
#define GPIO_BASE 0x10002000L
int main(){

  // int x = 10000;
  // while(x--){
  //   if(x%100==0){
  //     // volatile char* ch0= (volatile char *)(GPIO_BASE + 0x4);
  //     // volatile char* ch1= (volatile char *)(GPIO_BASE + 0x5);
  //     // putch((*ch0));
  //     // putch(*(ch1));
  //     putch('\n');
  //   }
  // }
  *(volatile int *)(GPIO_BASE + 0x8) = 0x23060115;
  for(int i = 0; i < 2; i++){
    for(int j = 0; j < 8; j++){
      int n = 30;
      while(n--){
        *(volatile char *)(GPIO_BASE + i) = 1 << j;
      }
    }
  }
}