#include <nvboard.h>
#include <VSimTop.h>
#include "verilated.h"
#include "verilated_vcd_c.h"
typedef unsigned int   uint32_t;
#define START_ADDR 0x80000000
uint32_t pmem[0x8000000]={};
static TOP_NAME dut;
uint32_t* guest_to_host(paddr_t paddr) { return pmem + paddr - START_ADDR; }
// void nvboard_bind_all_pins(VSimTop* top);
uint32_t pmem_read(uint32_t pc){
  uint32_t ret=guest_to_host(addr);
  return ret;
}

void singlecycle_wave(){
  top->clock=0;
  top->eval();
  contextp->timeInc(1); //时间+1
  tfp->dump(contextp->time()); //使用时间

  top->clock=1;
  top->io_inst=pmem_read(io_inst->pc);
  top->eval();
  top->eval();
  contextp->timeInc(1); //时间+1
  tfp->dump(contextp->time()); //使用时间
}
void sim_init(){
  top = new VSimTop;
  contextp = new VerilatedContext;
  tfp = new VerilatedVcdC;
  contextp->traceEverOn(true);
  top->trace(tfp, 0);
  tfp->open("dump.vcd");
}
extern void sim_exit(){
  delete top;
  tfp->close();
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

int main() {
  // nvboard_bind_all_pins(&dut);
  // nvboard_init();
  // reset(10);
  // while(1) {
  //   nvboard_update();
  //   single_cycle();
  // }
  sim_init();
  while(1){
    singlecycle_wave();
  }
  sim_exit();
}
