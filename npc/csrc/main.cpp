// #include <nvboard.h>
#include <VSimTop.h>
#include "verilated.h"
#include "verilated_vcd_c.h"
#include "svdpi.h"
#include "VSimTop__Dpi.h"
typedef unsigned int   uint32_t;
#define START_ADDR 0x80000000
uint32_t pmem[0x8000000]= {
  0xC0010093,
  0x40008213,
  0x40008213,
  0x40008213,
  0x40008213,
  0x40008213,
  0x40008213,
  0x40008213,
  0x40008213,
  0x00100073
};

VerilatedContext* contextp = NULL;
VerilatedVcdC* tfp = NULL;
static VSimTop* top;
// void nvboard_bind_all_pins(VSimTop* top);
static TOP_NAME dut;


// void* guest_to_host(uint32_t paddr) {
//   return pmem + paddr - START_ADDR;
// }
// static inline uint32_t host_read(void* addr) {
//   return *(uint32_t*)addr;
// }
uint32_t pmem_read(uint32_t addr) {
  uint32_t ret =pmem[(addr-0x80000000)/4];
  printf("%x %x\n",addr,ret);
  return ret;
}

void sim_init(){
  contextp = new VerilatedContext;
  tfp = new VerilatedVcdC;
  top = new VSimTop;
  contextp->traceEverOn(true);
  top->trace(tfp, 0);
  tfp->open("dump.vcd");
}
void step_and_dump_wave(){
  top->eval();
  contextp->timeInc(1); //时间+1
  tfp->dump(contextp->time()); //使用时间
}
extern void sim_exit(){
  delete top;
  tfp->close();
}
void reset(int n){
  top->reset=1;
  while (n-->0){
    top->clock=0;
    step_and_dump_wave();
    top->clock=1;
    step_and_dump_wave();
  }
  top->reset=0;
}

// static void single_cycle() {
//   dut.clock = 0; dut.eval();
//   dut.clock = 1; dut.eval();
// }

// static void reset(int n) {
//   dut.reset = 1;
//   while (n -- > 0) single_cycle();
//   dut.reset = 0;
// }
bool si_break=true;
extern void sim_break(){
  si_break=false;
}
int main() {
  // nvboard_bind_all_pins(&dut);
  // nvboard_init();
  // reset(10);
  // while(1) {
  //   nvboard_update();
  //   single_cycle();
  // }
  // int i=2;
  sim_init();
  reset(10);
  while(si_break){
    top->clock=0;
    step_and_dump_wave();

    top->clock=1;
    top->io_inst=pmem_read(top->io_pc);
    step_and_dump_wave();
  }
  sim_exit();
}
