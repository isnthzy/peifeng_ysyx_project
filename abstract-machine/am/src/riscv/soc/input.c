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
  char ascii = 0x0; 
  switch (tmp_key & 0xff) {
    case 0x16: ascii = 0x30; break; // 0
    case 0x1e: ascii = 0x31; break; // 1
    case 0x26: ascii = 0x32; break; // 2
    case 0x25: ascii = 0x33; break; // 3
    case 0x2e: ascii = 0x34; break; // 4
    case 0x36: ascii = 0x35; break; // 5
    case 0x3d: ascii = 0x36; break; // 6
    case 0x3e: ascii = 0x37; break; // 7
    case 0x46: ascii = 0x38; break; // 8
    case 0x45: ascii = 0x39; break; // 9

    case 0x41: ascii = 0x2c; break; // ,
    case 0x49: ascii = 0x2e; break; // .
    case 0x4a: ascii = 0x2f; break; // /
    case 0x4c: ascii = 0x3b; break; // ;
    case 0x52: ascii = 0x27; break; // '
    case 0x54: ascii = 0x5b; break; // [
    case 0x5b: ascii = 0x5d; break; // ]
    case 0x5a: ascii = 0x0a; break; // enter

    case 0x15: ascii = 0x51; break; // Q
    case 0x1d: ascii = 0x57; break; // W
    case 0x24: ascii = 0x45; break; // E
    case 0x2d: ascii = 0x52; break; // R
    case 0x2c: ascii = 0x54; break; // T
    case 0x35: ascii = 0x59; break; // Y
    case 0x3c: ascii = 0x55; break; // U
    case 0x43: ascii = 0x49; break; // I
    case 0x44: ascii = 0x4f; break; // O
    case 0x4d: ascii = 0x50; break; // P
    case 0x1c: ascii = 0x41; break; // A
    case 0x1b: ascii = 0x53; break; // S
    case 0x23: ascii = 0x44; break; // D
    case 0x2b: ascii = 0x46; break; // F
    case 0x34: ascii = 0x47; break; // G
    case 0x33: ascii = 0x48; break; // H
    case 0x3b: ascii = 0x4a; break; // J
    case 0x42: ascii = 0x4b; break; // K
    case 0x4b: ascii = 0x4c; break; // L
    case 0x1a: ascii = 0x5a; break; // Z
    case 0x22: ascii = 0x58; break; // X
    case 0x21: ascii = 0x43; break; // C
    case 0x2a: ascii = 0x56; break; // V
    case 0x32: ascii = 0x42; break; // B
    case 0x31: ascii = 0x4e; break; // N
    case 0x3a: ascii = 0x4d; break; // M
    default:  ascii = 0x00; // Default case
  }
  kbd->keycode = ascii;
}

void __am_uart_rx(AM_UART_RX_T *recv) {
  recv->data = (inb(UART_RX_ADDR + 5) & 0x1) ? inb(UART_RX_ADDR) : -1;
  //如果char > 128不符合ascii码，则返回-1
}