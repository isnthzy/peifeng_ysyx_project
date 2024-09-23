#include "trap.h"
#define SPI_BASE 0x10001000L
#define SPI_RX0  0x0
#define SPI_TX0  0x0
#define SPI_CTRL 0x10
#define SPI_DIVIDER 0x14
#define SPI_SS   0x18
int main(){
  char ch = 0x14;
  *(volatile char *)(SPI_BASE + SPI_TX0) = ch;
  *(volatile short int *)(SPI_BASE + SPI_DIVIDER) = 0x009f;
  *(volatile char *)(SPI_BASE + SPI_SS) = 0b10000000;
  *(volatile short int *)(SPI_BASE + SPI_CTRL) = 0b100000100;
// Bit #  31:14    13  12  11  10     9      8   7  6:0
// Access R        R/W R/W R/W R/W    R/W    R/W R  R/W
// Name   Reserved ASS IE  LSB Tx_NEG Rx_NEG GO_BSY Reserved CHAR_LEN
// NOTE:不需要下降沿触发所以不对tx rx处理，ass有点看不懂主要是
  while(true){
    volatile short int* spi_state = ((volatile short int *)(SPI_BASE + SPI_CTRL));
    if(((*spi_state) & 0x200) == 0x200) break;
  }
  volatile char * ch_rev = (volatile char *)(SPI_BASE + SPI_TX0);
  check((*ch_rev)==0x28);
}