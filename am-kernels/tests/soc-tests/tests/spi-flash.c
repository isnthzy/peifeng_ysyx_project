#include "trap.h"
#define FLASH_BASE 0x30000000L
#define SPI_BASE 0x10001000L
#define SPI_RX  0x0
#define SPI_TX  0x0
#define SPI_CTRL 0x10
#define SPI_DIVIDER 0x14
#define SPI_SS   0x18

uint32_t flash_read(uint32_t addr){

  *(volatile int *)(SPI_BASE + SPI_TX + 0x4) = addr;
  *(volatile short int *)(SPI_BASE + SPI_DIVIDER) = 0x009f;
  *(volatile char *)(SPI_BASE + SPI_SS) = 0b00000001;
  *(volatile short int *)(SPI_BASE + SPI_CTRL) = 0b000101000000;
  while(1){
    volatile short int* spi_state = ((volatile short int *)(SPI_BASE + SPI_CTRL));
    if(((*spi_state) & 0x100) != 0x100){
      *(volatile char *)(SPI_BASE + SPI_SS) &= ~0b00000001;
      break;
    }
  }
  uint32_t data = *(volatile int *)(SPI_BASE + SPI_RX);
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