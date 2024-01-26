#include <common.h>
#include <unistd.h>
#include "syscall.h"
int fs_open(const char *pathname, int flags, int mode);
size_t fs_read(int fd, void *buf, size_t len);
size_t fs_write(int fd, const void *buf, size_t len);
size_t fs_lseek(int fd, size_t offset, int whence);
int fs_close(int fd);
#define FILE_NAME_NUM 128
char* file_names[FILE_NAME_NUM]={"stdin","stdout","stderr","/dev/fb","/dev/events","/proc/dispinfo"};

struct sys_timeval{
  uint64_t tv_sec;     /* seconds */
  uint64_t tv_usec;    /* microseconds */
};
int sys_gettimeofday(struct sys_timeval *tv){
  if(tv==NULL) return 0;
  uint64_t sys_time=io_read(AM_TIMER_UPTIME).us;
  uint64_t us=sys_time%1000000;
  uint64_t s=sys_time/1000000;
  tv->tv_sec=s;
  tv->tv_usec=us;
  return 0;
}
/*gettimeofday*/
#ifdef CONFIG_STRACE
void strace_log(int gpr,int fd,int a1,int a2,int a3){
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
  case SYS_gettimeofday: syscall_name="SYS_gettimeofday"; break;
  default:
    panic("Unhandled syscall ID  = %d by strace", gpr);
    break;
  }
  if(gpr==SYS_read&&fd==4) return; //屏蔽读取键盘的trace,注意，fd==4的4要随着dev/event在数组中的位置调整
  char *file_name;
  if(fd>=FILE_NAME_NUM) panic("存放文件名的数组空间不够了");
  if(fd==-1){
    file_name=" ";
    Log("Syscall:%s!!! %d %d %d",syscall_name,a1,a2,a3);
  }else{
    file_name=file_names[fd];
    Log("file:%s Syscall:%s!!! %d %d %d",file_name,syscall_name,a1,a2,a3);
  }
}
#else
void strace_log(int gpr,int fd,int a1,int a2,int a3){}
#endif
void do_syscall(Context *c) {
  uintptr_t a[4];
  a[0] = c->GPR1;
  a[1] = c->GPR2;
  a[2] = c->GPR3;
  a[3] = c->GPR4;
  a[4] = c->GPRx;

  switch (a[0]) {
    case SYS_exit:
      strace_log(a[0],-1,a[1],a[2],a[3]);
      halt(c->GPRx); break;
    case SYS_yield:
      strace_log(a[0],-1,a[1],a[2],a[3]);
      yield(); c->GPRx=0; break;
    case SYS_write:
      strace_log(a[0],a[1],a[1],a[2],a[3]);
      c->GPRx=fs_write(a[1],(void *)a[2],a[3]);
      break;
    case SYS_brk:
      strace_log(a[0],-1,a[1],a[2],a[3]);
      c->GPRx=0; break;
    case SYS_read:
      strace_log(a[0],a[1],a[1],a[2],a[3]);
      c->GPRx=fs_read(a[1],(void *)a[2],a[3]);
      break;
    case SYS_open:
      strace_log(a[0],-1,a[1],a[2],a[3]);
      c->GPRx=fs_open((char *)a[1],a[2],a[3]);
      file_names[c->GPRx]=(char *)a[1];
      break;
    case SYS_lseek:
      strace_log(a[0],a[1],a[1],a[2],a[3]);
      c->GPRx=fs_lseek(a[1],a[2],a[3]);
      break;
    case SYS_close:
      strace_log(a[0],a[1],a[1],a[2],a[3]);
      c->GPRx=fs_close(a[1]);
      break;  
    case SYS_gettimeofday:
      // strace_log(a[0],-1,a[1],a[2],a[3]);
      c->GPRx=sys_gettimeofday((struct sys_timeval *)a[1]);
      break;
    default: panic("Unhandled syscall ID = %d", a[0]);
  }
}
