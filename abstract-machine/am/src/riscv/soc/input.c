#include <am.h>
#include <riscv/riscv.h>

#define UART_RX_ADDR 0x10000000L
#define KEYBRD_ADDR  0x10011000L
// #define KEYDOWN_MASK 0x8000
// #define DEVICE_BASE  0xa0000000
// #define KBD_ADDR     (DEVICE_BASE + 0x0000060)
void __am_input_keybrd(AM_INPUT_KEYBRD_T *kbd) {
  uint32_t tmp_key=inw(KEYBRD_ADDR);
  bool tmp_keydown=((tmp_key & 0xf000) != 0xf000);
  kbd->keydown = tmp_keydown;
  kbd->keycode = tmp_key & 0xff;
}

void __am_uart_rx(AM_UART_RX_T *recv) {
  recv->data = (inb(UART_RX_ADDR + 5) & 0x1) ? inb(UART_RX_ADDR) : -1;
  //如果char > 128不符合ascii码，则返回-1
}