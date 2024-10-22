#include <am.h>
#include <riscv/riscv.h>

#define UART_RX_ADDR 0x10000000L
#define KEYBRD_ADDR  0x10011000L

/*
#define MAP(c, f) c(f)
#define NPC_KEYS(f) \
  f(ESCAPE) f(F1) f(F2) f(F3) f(F4) f(F5) f(F6) f(F7) f(F8) f(F9) f(F10) f(F11) f(F12) \
f(GRAVE) f(1) f(2) f(3) f(4) f(5) f(6) f(7) f(8) f(9) f(0) f(MINUS) f(EQUALS) f(BACKSPACE) \
f(TAB) f(Q) f(W) f(E) f(R) f(T) f(Y) f(U) f(I) f(O) f(P) f(LEFTBRACKET) f(RIGHTBRACKET) f(BACKSLASH) \
f(CAPSLOCK) f(A) f(S) f(D) f(F) f(G) f(H) f(J) f(K) f(L) f(SEMICOLON) f(APOSTROPHE) f(RETURN) \
f(LSHIFT) f(Z) f(X) f(C) f(V) f(B) f(N) f(M) f(COMMA) f(PERIOD) f(SLASH) f(RSHIFT) \
f(LCTRL) f(APPLICATION) f(LALT) f(SPACE) f(RALT) f(RCTRL) \
f(UP) f(DOWN) f(LEFT) f(RIGHT) f(INSERT) f(DELETE) f(HOME) f(END) f(PAGEUP) f(PAGEDOWN)

#define NPC_KEY_NAME(k) NPC_KEY_ ## k,

enum {
  NPC_KEY_NONE = 0,
  MAP(NPC_KEYS, NPC_KEY_NAME)
};

#define SDL_KEYMAP(k) keymap[SDL_SCANCODE_ ## k] = NPC_KEY_ ## k;
static uint32_t keymap[256] = {};

static void init_keymap() {
  MAP(NPC_KEYS, SDL_KEYMAP)
}
*/

void __am_input_keybrd(AM_INPUT_KEYBRD_T *kbd) {
  uint32_t tmp_key=inw(KEYBRD_ADDR);
  bool tmp_keydown=((tmp_key & 0xf000) != 0xf000);
  kbd->keydown = tmp_keydown;
  char ascii = tmp_key & 0xff; 
  kbd->keycode = ascii;
}

void __am_uart_rx(AM_UART_RX_T *recv) {
  recv->data = (inb(UART_RX_ADDR + 5) & 0x1) ? inb(UART_RX_ADDR) : -1;
  //如果char > 128不符合ascii码，则返回-1
}