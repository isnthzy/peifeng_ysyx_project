#include <am.h>
#include <nemu.h>

#define KEYDOWN_MASK 0x8000

void __am_input_keybrd(AM_INPUT_KEYBRD_T *kbd) {
  uint32_t tmp_key=inl(KBD_ADDR);
  bool tmp_keydown=((tmp_key&KEYDOWN_MASK)!=0);
  kbd->keydown = tmp_keydown;
  kbd->keycode = tmp_key&~KEYDOWN_MASK;
}
