#include <am.h>
#include <riscv/riscv.h>
#include <klib.h>

static Context* (*user_handler)(Event, Context*) = NULL;

Context* __am_irq_handle(Context *c) {
  if (user_handler) {
    Event ev = {0};
    printf("%x\n",c->mcause);
    switch (c->mcause) {
      case 0xb: //11 Environment call from M-mode
        if(c->GPR1==-1){
          ev.event=EVENT_YIELD;
          // c->mepc+=4;
        }else if(c->GPR1==1){
          ev.event=EVENT_SYSCALL;
          // c->mepc+=4;
        }else{ 
          //典中典之pa后边的部分被pa前面的实现坑惨，后边要求为识别的异常时间编号为4，但是经过检查发现
          //自己的输出为0，几番定位定位到这里给了0，照理说传入不对的数据都应该给0
          ev.event=EVENT_ERROR;
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
  return NULL;
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
