#include <nvboard.h>
#include <Vtop.h>
#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include "verilated.h"
#include "verilated_vcd_c.h"
static TOP_NAME dut;

void nvboard_bind_all_pins(Vtop* top);

static void single_cycle() {
  dut.clk = 0; dut.eval();
  dut.clk = 1; dut.eval();
}

static void reset(int n) {
  dut.rst = 1;
  while (n -- > 0) single_cycle();
  dut.rst = 0;
}

int main(int argc,char** argv) {
  VerilatedContext* contextp=new VerilatedContext;
  contextp->commandArgs(argc,argv);
  Vtop* top=new Vtop{contextp};

  // VerilatedVcdC* tfp=new VerilatedVcdC;
  // contextp->traceEverOn(true);
  // top->trace(tfp,0);
  // tfp->open("wave.vcd");

  nvboard_bind_all_pins(&dut);
  nvboard_init();

  reset(10);
  while(1) {
    nvboard_update();
    single_cycle();
  }
  delete top;
  delete contextp;
  tfp->close();
  return 0;
}
