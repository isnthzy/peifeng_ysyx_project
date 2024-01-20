#include <common.h>
#include "syscall.h"
#define SYS_exit  0
#define SYS_yield 1
void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;

  switch (a[0]) {
    case SYS_exit:
      halt(0);
    case SYS_yield:
      yield(); c->GPR1=c->GPRx; break;
    default: panic("Unhandled syscall ID = %d", a[0]);
  }
}
