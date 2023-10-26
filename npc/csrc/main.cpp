#include <nvboard.h>
#include <VSimTop.h>
#include "verilated.h"
#include "verilated_vcd_c.h"

static TOP_NAME dut;
VerilatedContext* contextp = NULL;
VerilatedVcdC* tfp = NULL;

static VSimTop* SimTop;


void nvboard_bind_all_pins(VSimTop* top);
// void step_and_dump_wave(){
//   SimTop->eval();
//   contextp->timeInc(1);
//   tfp->dump(contextp->time());
// }
// void sim_init(){
//   contextp = new VerilatedContext;
//   tfp = new VerilatedVcdC;
//   SimTop = new VSimTop;
//   contextp->traceEverOn(true);
//   SimTop->trace(tfp, 0);
//   tfp->open("wave.vcd");
// }
// void sim_exit(){
//   step_and_dump_wave();
//   tfp->close();
// }

// static void single_cycle() {
//   dut.clock = 0; dut.eval();
//   dut.clock = 1; dut.eval();
// }

// static void reset(int n) {
//   dut.reset = 1;
//   while (n -- > 0) single_cycle();
//   dut.reset = 0;
// }


int main(int argc,char** argv){
    VerilatedContext* contextp=new VerilatedContext;
    contextp->commandArgs(argc,argv);
    VSimTop* top=new VSimTop{contextp};

    VerilatedVcdC* tfp=new VerilatedVcdC;
    contextp->traceEverOn(true);
    top->trace(tfp,0);
    tfp->open("wave.vcd");
    int i=1000;
    while(!contextp->gotFinish()&&i>=0){
        top->eval();
        i--;
    }
    delete top;
    delete contextp;
    tfp->close();
    return 0;
}