#include "trap.h"
#define GPIO_BASE 0x10002000L
int main(){

  int x = 100;
  *(volatile char *)(GPIO_BASE ) = 0xff;
  while(x--){
    
  }
  // for(int i = 0; i < 2; i++){
  //   for(int j = 0; j < 8; j++){
  //     int n = 1000;
  //     while(n--){
  //       *(volatile char *)(GPIO_BASE + i) = 1 << j;
  //     }
      
  //   }
  // }
}