#include <am.h>
#include <klib-macros.h>
#include <riscv/riscv.h>
#define soc_trap(code) asm volatile("mv a0, %0; ebreak" : :"r"(code))
#define UART_BASE 0x10000000L
#define UART_TX   0x0


extern char _heap_start;
int main(const char *args);

extern char _pmem_start;
#define PMEM_SIZE (4 * 1024)
#define PMEM_END  ((uintptr_t)&_pmem_start + PMEM_SIZE)
#define SRAM_SIZE (4 * 1024 * 4 * 1024)
Area heap = RANGE(&_heap_start, &_heap_start - 0x8000 + SRAM_SIZE );
#ifndef MAINARGS
#define MAINARGS ""
#endif
static const char mainargs[] = MAINARGS;



void putch(char ch) {
  *(volatile char *)(UART_BASE + UART_TX) = ch;
}

void halt(int code) {
  soc_trap(code);
  // should not reach here
  while (1);
}

void _trm_init(){
  int ret = main(mainargs);
  halt(ret);
}