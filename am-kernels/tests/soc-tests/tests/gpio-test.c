#include "trap.h"
#define GPIO_BASE 0x10002000L
int main(){
  for(int i = 0; i < 4; i++){
    for(int j = 0; j < 8; j++){
      int n = 100;
      while(n--){
        *(volatile int *)(GPIO_BASE + i) = 1 << j;
      }
      
    }
  }
}