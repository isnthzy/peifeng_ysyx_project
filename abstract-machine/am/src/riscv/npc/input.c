#include <am.h>
#include <riscv/riscv.h>
#define KEYDOWN_MASK 0x8000
#define DEVICE_BASE  0xa0000000
#define KBD_ADDR     (DEVICE_BASE + 0x0000060)
void __am_input_keybrd(AM_INPUT_KEYBRD_T *kbd) {
  uint32_t tmp_key=inl(KBD_ADDR);
  bool tmp_keydown=((tmp_key&KEYDOWN_MASK)!=0);
  kbd->keydown = tmp_keydown;
  kbd->keycode = tmp_key&~KEYDOWN_MASK;
}
