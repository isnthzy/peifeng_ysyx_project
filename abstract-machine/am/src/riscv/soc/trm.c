#include <am.h>
#include <klib-macros.h>
#include <riscv/riscv.h>
#include <string.h>
#define soc_trap(code) asm volatile("mv a0, %0; ebreak" : :"r"(code))
#define UART_BASE 0x10000000L
#define UART_TX   0x0
#define UART_LSB  0x0
#define UART_MSB  0x1
#define UART_LCR  0x3

extern char _data_start [];
extern char _data_size  [];
extern char _data_load_start [];

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
  *(volatile char *)(UART_BASE + UART_TX) = ch;
}

void halt(int code) {
  soc_trap(code);
  // should not reach here
  while (1);
}

void _trm_init(){
  if (_data_start != _data_load_start) memcpy(_data_start, _data_load_start, (size_t) _data_size);
  init_uart();
  int ret = main(mainargs);
  halt(ret);
}