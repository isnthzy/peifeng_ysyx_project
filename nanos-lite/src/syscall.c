#include <common.h>
#include "syscall.h"
void strace_log(int gpr){
  char *syscall_name;
  switch (gpr)
  {
  case SYS_exit:  syscall_name="SYS_exit" ; break;
  case SYS_yield: syscall_name="SYS_yield"; break;
  default:
    panic("Unhandled syscall ID  = %d by strace", gpr);
    break;
  }
  Log("Syscall:%s!!!",syscall_name);
}
void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;
  switch (a[0]) {
    case SYS_exit:
      halt(c->GPRx); break;
    case SYS_yield:
      yield(); c->GPRx = 0; break;
    default: panic("Unhandled syscall ID = %d", a[0]);
  }
}
