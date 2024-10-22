#include <am.h>
#include <riscv/riscv.h>

#define UART_RX_ADDR 0x10000000L
#define KEYBRD_ADDR  0x10011000L

void __am_input_keybrd(AM_INPUT_KEYBRD_T *kbd) {
  uint32_t tmp_key=inw(KEYBRD_ADDR);
  bool tmp_keydown=((tmp_key & 0xf000) != 0xf000);
  kbd->keydown = tmp_keydown;
  char scankey = 0x0; 
  switch (tmp_key & 0x00ff) {
    case 0x76: scankey = 1; break; // ESCAPE
    case 0x05: scankey = 2; break; // F1
    case 0x06: scankey = 3; break; // F2
    case 0x04: scankey = 4; break; // F3
    case 0x0c: scankey = 5; break; // F4
    case 0x03: scankey = 6; break; // F5
    case 0x0b: scankey = 7; break; // F6
    case 0x83: scankey = 8; break; // F7
    case 0x0a: scankey = 9; break; // F8
    case 0x01: scankey = 10; break; // F9

    case 0x09: scankey = 11; break; // F10
    case 0x78: scankey = 12; break; // F11
    case 0x0e: scankey = 13; break; // GARVE
    case 0x16: scankey = 14; break; // 1
    case 0x1e: scankey = 15; break; // 2
    case 0x26: scankey = 16; break; // 3
    case 0x25: scankey = 17; break; // 4
    case 0x2e: scankey = 18; break; // 5

    case 0x36: scankey = 19; break; // 6
    case 0x3d: scankey = 20; break; // 7
    case 0x3e: scankey = 21; break; // 8
    case 0x46: scankey = 22; break; // 9
    case 0x45: scankey = 23; break; // 0
    case 0x4e: scankey = 24; break; // MINUS
    case 0x55: scankey = 25; break; // EQUALS
    case 0x66: scankey = 26; break; // BACKSPACE
    case 0x0d: scankey = 27; break; // TAB
    case 0x15: scankey = 28; break; // q
    case 0x1d: scankey = 29; break; // w
    case 0x24: scankey = 30; break; // e
    case 0x2d: scankey = 31; break; // r
    case 0x2c: scankey = 32; break; // t
    case 0x35: scankey = 33; break; // y
    case 0x3c: scankey = 34; break; // u
    case 0x43: scankey = 35; break; // i
    case 0x44: scankey = 36; break; // o
    case 0x4d: scankey = 37; break; // p
    case 0x54: scankey = 38; break; // LEFTBRACKET
    case 0x5b: scankey = 39; break; // RIGHTBRACKET
    case 0x5d: scankey = 40; break; // BACKSLASH
    case 0x58: scankey = 41; break; // CAPSLOCK
    case 0x1c: scankey = 42; break; // a
    case 0x1b: scankey = 43; break; // s
    case 0x23: scankey = 43; break; // d
    case 0x2b: scankey = 43; break; // f
    case 0x34: scankey = 43; break; // g
    case 0x33: scankey = 43; break; // h
    case 0x3b: scankey = 43; break; // j
    case 0x42: scankey = 43; break; // k
    case 0x4b: scankey = 43; break; // l
    case 0x4c: scankey = 43; break; // SEMICOLON
    case 0x52: scankey = 43; break; // APOSTROPHE
    case 0x5a: scankey = 43; break; // RETURN
    case 0x12: scankey = 44; break; // LSHIFT
    case 0x1a: scankey = 45; break; // z
    case 0x22: scankey = 46; break; // x
    case 0x21: scankey = 47; break; // c
    case 0x2a: scankey = 48; break; // v
    case 0x32: scankey = 49; break; // b
    case 0x31: scankey = 50; break; // n
    case 0x3a: scankey = 51; break; // m
    case 0x41: scankey = 52; break; // COMMA
    case 0x49: scankey = 53; break; // PERIOD
    case 0x4a: scankey = 54; break; // SLASH
    case 0x59: scankey = 55; break; // RSHIFT
    case 0x14: scankey = 56; break; // LCTRL
    case 0x11: scankey = 57; break; // LALT
    case 0x29: scankey = 57; break; // SPACE
    case 0xe011: scankey = 57; break; // RALT
    case 0xe014: scankey = 57; break; // RCTRL
    case 0xe075: scankey = 58; break; // up
    case 0xe072: scankey = 59; break; // down
    case 0xe06b: scankey = 60; break; // left
    case 0xe074: scankey = 61; break; // right
    case 0xe070: scankey = 62; break; // insert
    case 0xe071: scankey = 63; break; // delete
    case 0xe06c: scankey = 64; break; // home
    case 0xe069: scankey = 65; break; // end
    case 0xe07d: scankey = 66; break; // pageup
    case 0xe07a: scankey = 67; break; // pagedown

    default:  scankey = 0x00; // Default case
  }
  kbd->keycode = scankey;
}

void __am_uart_rx(AM_UART_RX_T *recv) {
  recv->data = (inb(UART_RX_ADDR + 5) & 0x1) ? inb(UART_RX_ADDR) : -1;
  //如果char > 128不符合ascii码，则返回-1
}