// #include <nvboard.h>
#include <VSimTop.h>
#include <stdio.h>
#include <string.h>
#include "verilated.h"
#include "verilated_vcd_c.h"
#include "svdpi.h"
#include "VSimTop__Dpi.h"
#define NULL 0
#define CONFIG_MSIZE 0x8000000
#define START_ADDR   0x7ffffffc
#define CONFIG_MBASE 0x7ffffffc
#define PG_ALIGN __attribute((aligned(4096)))
typedef unsigned int   uint32_t;
typedef unsigned char  uint8_t;  
typedef unsigned short int  uint16_t; 
static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN = {};
static const uint32_t init_img [] = {
  0xC0010093,
  0x40008213,
  0x40008213,
  0x40008213,
  0x40008213,
  0x40008213,
  0x40008213,
  0x40008213,
  0x40008213,
  0x00100073,
};

static char *img_file = NULL;
static inline uint32_t host_read(void *addr, int len) {
  switch (len) {
    case 1: return *(uint8_t  *)addr;
    case 2: return *(uint16_t *)addr;
    case 4: return *(uint32_t *)addr;
    default:  return 0;
  }
}
static inline void host_write(void *addr, int len, uint32_t data) {
  switch (len) {
    case 1: *(uint8_t  *)addr = data; return;
    case 2: *(uint16_t *)addr = data; return;
    case 4: *(uint32_t *)addr = data; return;
    default: return;
  }
}
uint8_t* guest_to_host(uint32_t paddr) { return pmem + paddr - CONFIG_MBASE; }
uint32_t host_to_guest(uint8_t *haddr) { return haddr - pmem + CONFIG_MBASE; }
static uint32_t pmem_read(uint32_t addr, int len) {
  uint32_t ret = host_read(guest_to_host(addr), len);
  // printf("%x\n",ret);
  return ret;
}

static void pmem_write(uint32_t addr, int len, uint32_t data) {
  host_write(guest_to_host(addr), len, data);
}


VerilatedContext* contextp = NULL;
VerilatedVcdC* tfp = NULL;
static VSimTop* top;
// void nvboard_bind_all_pins(VSimTop* top);
// static TOP_NAME dut;

static long load_img() {
  if (img_file == NULL) {
    printf("No image is given. Use the default build-in image.");
    return 4096; // built-in image size
  }

  FILE *fp = fopen(img_file, "rb");
  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);
  printf("The image is %s, size = %ld", img_file, size);
  fseek(fp, 0, SEEK_SET);
  int ret = fread(guest_to_host(START_ADDR), size, 1, fp);
  assert(ret == 1);
  fclose(fp);
  return size;
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
bool sim_end=true;
extern void sim_break(){
  sim_end=false;
}
int main(int argc, char *argv[]) {
  // nvboard_bind_all_pins(&dut);
  // nvboard_init();
  // reset(10);
  // while(1) {
  //   nvboard_update();
  //   single_cycle();
  // }
  // int i=2;
  img_file=argv[1];
  if(argc<2){
    printf("\033[0m\033[1;31mimg=NULL -> use init_img\033[0m\n");
    memcpy(guest_to_host(START_ADDR),init_img, sizeof(init_img));
  }else load_img();
  
  sim_init();
  reset(10);
  int cnt=100000;
  while(sim_end&&cnt){
    top->clock=0;
    step_and_dump_wave();

    top->clock=1;
    top->io_inst=pmem_read(top->io_pc,4);
    step_and_dump_wave();
    cnt--;
  }
  sim_exit();
}
