#include <am.h>
#include <riscv/riscv.h>
#include <klib.h>

static Context* (*user_handler)(Event, Context*) = NULL;

Context* __am_irq_handle(Context *c) {
  if (user_handler) {
    Event ev = {0};
    switch (c->mcause) {
      case 0xb: //11 Environment call from M-mode
        if(c->GPR1==-1){
          ev.event=EVENT_YIELD;
          c->mepc+=4; //做pa3.2的时候发现这里没有处理，，，难绷
        }else if(c->GPR1>=0&&c->GPR1<=19){
          ev.event=EVENT_SYSCALL;
          c->mepc+=4;
        }else{
          printf("abstract-machine/am/src/riscv/nemu/cte.c传入了未知的type");
        }
        break;
      default: ev.event = EVENT_ERROR; break;
    }

    c = user_handler(ev, c);
    assert(c != NULL);
  }

  return c;
}

extern void __am_asm_trap(void);

bool cte_init(Context*(*handler)(Event, Context*)) {
  // initialize exception entry
  asm volatile("csrw mtvec, %0" : : "r"(__am_asm_trap));
  //将异常入口地址设置到mtvec寄存器
  // register event handler
  user_handler = handler;

  return true;
}

Context *kcontext(Area kstack, void (*entry)(void *), void *arg) {
  assert(kstack.start!=NULL&&kstack.end!=NULL);
  assert(kstack.start<kstack.end);
  assert(entry!=NULL);
  Context *cp=kstack.end-1;
  cp->mcause=0xb;
  cp->mstatus=0x1800;
  cp->mepc=(uintptr_t)entry;
  return cp;
}

void yield() {
#ifdef __riscv_e
  asm volatile("li a5, -1; ecall");
#else
  asm volatile("li a7, -1; ecall");
  //将 -1 load到寄存器 a7 中，然后触发一个异常调用。
#endif
}

bool ienabled() {
  return false;
}

void iset(bool enable) {
}
