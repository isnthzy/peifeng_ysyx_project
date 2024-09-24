#include "trap.h"
#define FLASH_BASE 0x03000000L
#define SPI_BASE 0x10001000L
#define SPI_RX  0x0
#define SPI_TX  0x0
#define SPI_CTRL 0x10
#define SPI_DIVIDER 0x14
#define SPI_SS   0x18

uint32_t flash_read(uint32_t addr){
  uint32_t data = *(volatile int *)(addr); //地址解引用获得data
  data = ((data >> 24)
         |(data >> 8 & 0x00ff00)
         |(data << 8 & 0xff0000)
         |(data << 24));
  return data;
}

int main(){
  for(int i=0;i<20;i+=4){
    uint32_t addr = FLASH_BASE + i;
    uint32_t data = flash_read(addr);
    char *ch = (char *)&data; 
    for(int i=0;i<4;i++){
      putch(ch[i]);
    }
  }
} 