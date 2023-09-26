#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "Vtop.h"
#include "verilated.h"
#include "verilated_vcd_c.h"

int main(int argc,char** argv){
    VerilatedContext* contextp=new VerilatedContext;
    contextp->commandArgs(argc,argv);
    Vtop* top=new Vtop{contextp};

    VerilatedVcdC* tfp=new VerilatedVcdC;
    contextp->traceEverOn(true);
    top->trace(tfp,0);
    tfp->open("wave.vcd");
    int i=20;
    while(!contextp->gotFinish()&&i>=0){
        int a=rand()&1;
        int b=rand()&1;
        top->a=a;
        top->b=b;
        top->eval();
        printf("a=%d,b=%d,f=%d\n",a,b,top->f);

        tfp->dump(contextp->time());
        contextp->timeInc(1);

        assert(top->f==(a^b));

        i--;
    }
    delete top;
    delete contextp;
    tfp->close();
    return 0;
}
