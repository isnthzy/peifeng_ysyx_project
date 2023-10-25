#include <nvboard.h>
#include <VSimTop.h>
#include "verilated.h"
#include "verilated_vcd_c.h"

static TOP_NAME dut;

void nvboard_bind_all_pins(VSimTop* top);
// void step_and_dump_wave(){
//   top->eval();
//   contextp->timeInc(1);
//   tfp->dump(contextp->time());
// }
// void sim_init(){
//   contextp = new VerilatedContext;
//   tfp = new VerilatedVcdC;
//   top = new Vmux21;
//   contextp->traceEverOn(true);
//   top->trace(tfp, 0);
//   tfp->open("dump.vcd");
// }
// void sim_exit(){
//   step_and_dump_wave();
//   tfp->close();
// }

static void single_cycle() {
  dut.clock = 0; dut.eval();
  dut.clock = 1; dut.eval();
}

static void reset(int n) {
  dut.reset = 1;
  while (n -- > 0) single_cycle();
  dut.reset = 0;
}

int main() {
  nvboard_bind_all_pins(&dut);
  nvboard_init();

  reset(10);

  while(1) {
    nvboard_update();
    single_cycle();
  }
}
