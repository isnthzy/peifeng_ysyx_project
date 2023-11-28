#include <npc_common.h>
void step_and_dump_wave();
void npc_exev(int step){
  int n=step;
  while(sim_end&&n--){
    top->clock=1;
    top->io_inst=pmem_read(top->io_pc,4);
    step_and_dump_wave();

    top->clock=0;
    step_and_dump_wave();
  }
}