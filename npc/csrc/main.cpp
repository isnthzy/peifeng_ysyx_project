// #include <nvboard.h>
#include <npc_common.h>
void init_img(int argc, char *argv);
static char *img_file = NULL;


void sim_init(){
  contextp = new VerilatedContext;
  tfp = new VerilatedVcdC;
  top = new VSimTop;
  contextp->traceEverOn(true);
  top->trace(tfp, 0);
  tfp->open("dump.vcd"); 
  //使用make sim生成的dump.vcd在npc/
  //SimTop+*.bin生成的dump.vcd在npc/build
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
  top->clock=0;
  step_and_dump_wave();
  while (n-->0){
    top->clock=1;
    step_and_dump_wave();
    top->clock=0;
    step_and_dump_wave();
  }
  top->reset=0;
}


extern void sim_break(int pc,int ret_reg){
  if(ret_reg==0) printf("npc: \033[1;32mHIT GOOD TRAP\033[0m at pc = 0x%08x\n",pc);
  else printf("npc: \033[1;31mHIT BAD TRAP\033[0m at pc = 0x%08x\n",pc);
  sim_end=false;
}
extern void inv_break(int pc){
  printf("npc: \033[1;31mABORT\033[0m at pc = 0x%08x\n",pc);
  sim_end=false;
}
int main(int argc, char *argv[]) {
  img_file=argv[1];
  sim_init();
  init_img(argc,img_file);
  reset(2);
  sdb_mainloop();
  int cnt=100;
  // while(sim_end&&cnt){
  //   top->clock=1;
  //   top->io_inst=pmem_read(top->io_pc,4);
  //   step_and_dump_wave();

  //   top->clock=0;
  //   step_and_dump_wave();
  //   cnt--;
  // }
  // sim_exit();
}
