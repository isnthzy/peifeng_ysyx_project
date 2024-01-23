#include <common.h>
#include <unistd.h>
#include "syscall.h"
int fs_open(const char *pathname, int flags, int mode);
size_t fs_read(int fd, void *buf, size_t len);
size_t fs_write(int fd, const void *buf, size_t len);
size_t fs_lseek(int fd, size_t offset, int whence);
int fs_close(int fd);
void strace_log(int gpr,int a1,int a2,int a3){
  char *syscall_name;
  switch (gpr)
  {
  case SYS_exit:  syscall_name="SYS_exit" ; break;
  case SYS_yield: syscall_name="SYS_yield"; break;
  case SYS_write: syscall_name="SYS_write"; break;
  case SYS_brk:   syscall_name="SYS_brk";   break;
  case SYS_read:  syscall_name="SYS_read";  break; 
  case SYS_open:  syscall_name="SYS_open";  break;
  case SYS_lseek: syscall_name="SYS_lseek"; break;
  case SYS_close: syscall_name="SYS_close"; break;  
  default:
    panic("Unhandled syscall ID  = %d by strace", gpr);
    break;
  }
  Log("Syscall:%s!!! %d %d %d",syscall_name,a1,a2,a3);
}
void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;
  a[1] = c->GPR2;
  a[2] = c->GPR3;
  a[3] = c->GPR4;
  a[4] = c->GPRx;
  #ifdef CONFIG_STRACE
  strace_log(a[0],a[1],a[2],a[3]);
  #endif
  switch (a[0]) {
    case SYS_exit:
      // printf("%d",c->GPRx);
      halt(c->GPRx); break;
    case SYS_yield:
      yield(); c->GPRx=0; break;
    case SYS_write:
      if(a[1]==1||a[1]==2){
        char *write_buf=(char *)a[2];
        for(size_t i=0;i<a[3];i++){
          putch(write_buf[i]);
        } 
      }else c->GPRx=fs_write(a[1],(void *)a[2],a[3]);
      c->GPRx=0;
      break;
    case SYS_brk:
      c->GPRx=0; break;
    case SYS_read:
      c->GPRx=fs_read(a[1],(void *)a[2],a[3]);
      break;
    case SYS_open:
      c->GPRx=fs_open((char *)a[1],a[2],a[3]);
      break;
    case SYS_lseek:
      c->GPRx=fs_lseek(a[1],a[2],a[3]);
      break;
    case SYS_close:
      c->GPRx=fs_close(a[1]);
      break;  
    default: panic("Unhandled syscall ID = %d", a[0]);
  }
}
