#include <am.h>
#include <klib-macros.h>
#include <riscv/riscv.h>
#include <string.h>
#define soc_trap(code) asm volatile("mv a0, %0; ebreak" : :"r"(code))
#define GPIO_BASE 0x10002000L
#define UART_BASE 0x10000000L
#define UART_TX   0x0
#define UART_LSB  0x0
#define UART_MSB  0x1
#define UART_LCR  0x3
#define UART_LSR  0x5

extern char _heap_start;
extern char _heap_end;
int main(const char *args);

extern char _pmem_start;
#define PMEM_SIZE (4 * 1024)
#define PMEM_END  ((uintptr_t)&_pmem_start + PMEM_SIZE)
#define SRAM_SIZE (4 * 1024 * 4 * 1024)
Area heap = RANGE(&_heap_start, &_heap_end);
#ifndef MAINARGS
#define MAINARGS ""
#endif
static const char mainargs[] = MAINARGS;

void init_uart(){
  char lcr = *((volatile char *)(UART_BASE + 0x3));
  *(volatile char *)(UART_BASE + UART_LCR) = lcr | 0x80;
  *(volatile char *)(UART_BASE + UART_MSB) = 0x0;
  *(volatile char *)(UART_BASE + UART_LSB) = 0x3;
  *(volatile char *)(UART_BASE + UART_LCR) = lcr & 0x7F;
}

void putch(char ch) {
  while(true){
    volatile char* fifo_state = ((volatile char *)(UART_BASE + UART_LSR));
    
    if(((*fifo_state) & 0x20)==0x20) break;
  }
  *(volatile char *)(UART_BASE + UART_TX) = ch;
}

void halt(int code) {
  soc_trap(code);
  // should not reach here
  while (1);
}

void put_csr(){
  // 使用内联汇编读取CSR寄存器，并将每一位存储到内存中
  uint32_t mvendorid;
  uint32_t marchid;
  asm("csrr %0, mvendorid" : "=r"(mvendorid) : );
  asm("csrr %0, marchid" : "=r"(marchid) : );
  char *mvendorid_char=(char *)&mvendorid;
  for(int i=3;i>=0;i--){
    putch(mvendorid_char[i]);
  }
  char digits[8]; // 用printf太笨拙了,而且极慢，所以写了个easy解析器
  for(int i=0;i<8;i++){
    digits[i]=marchid%10;
    marchid/=10;
  }
  for(int i=7;i>=0;i--){
    putch(digits[i]+'0');
  }
}

void _trm_init(){
  init_uart();
  uint32_t marchid;
  asm("csrr %0, marchid" : "=r"(marchid) : );
  *(volatile int *)(GPIO_BASE + 0x8) = marchid; //显示学号
  int ret = main(mainargs);
  halt(ret);
}