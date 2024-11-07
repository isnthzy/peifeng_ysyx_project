#ifndef DIFFSTATE_H
#define DIFFSTATE_H
#include "../npc_common.h"
#include "nemuproxy.h"
#include "../util/iringbuf.h"
#include "../util/utils.h"

#define DIFFTEST_TO_REF 1
#define DIFFTEST_TO_DUT 0

extern const char *regs[];
extern bool g_print_step;
void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
void wp_trace(char *decodelog);

typedef struct excp_event{
  uint8_t  excp_valid = 0;
  uint8_t  is_mret    = 0;
  uint32_t interrupt = 0;
  uint32_t exception = 0;
  vaddr_t  exceptionPC = 0;
  uint32_t exceptionInst = 0;
} excp_event_t;

typedef struct {
  vaddr_t  pc;
  uint32_t inst;
} base_state_t; //不参与dpi提交，在diff_step时保留关键信息

typedef struct instr_commit{
  uint8_t  valid = 0;
  vaddr_t  pc;
  uint32_t inst;
  uint8_t  skip;
  uint8_t  wen;
  uint8_t  wdest;
  data_t   wdata;
  uint8_t  csr_rstat;
  data_t   csr_data;
} instr_commit_t;

typedef struct __attribute__((packed)) {
  data_t  mstatus;
  data_t  mtvec;
  data_t  mepc;
  data_t  mcause;
} csr_state_t;

typedef struct store_event{
  uint8_t  valid = 0;
  paddr_t  paddr;
  vaddr_t  vaddr;
  data_t   data;
  uint8_t  len;
} store_event_t;

typedef struct load_event{
  uint8_t valid = 0;
  paddr_t paddr;
  vaddr_t vaddr;
  data_t  data;
  uint8_t len;
} load_event_t;

typedef struct {
  data_t  gpr[MUXDEF(CONFIG_RVE,16,32)];
} greg_state_t;

typedef struct {
  base_state_t base;
  greg_state_t regs;
  csr_state_t  csr;
} difftest_core_state_t;

typedef struct{
  excp_event_t excp;
  instr_commit_t commit[DIFFTEST_COMMIT_WIDTH];
  store_event_t store[DIFFTEST_COMMIT_WIDTH];
  load_event_t load[DIFFTEST_COMMIT_WIDTH];
} difftest_core_commit_t;


class Difftest{
  private:
    difftest_core_state_t dut;
    difftest_core_state_t ref;

    difftest_core_commit_t dut_commit; //dut_commit 提交分析信息，dut和ref用于结构体同步

    data_t* dut_regs_ptr=(data_t *)&dut.regs;
    data_t* ref_regs_ptr=(data_t *)&ref.regs;

    NemuProxy* nemu_proxy=NULL;
    /* the index of instructions per commit */
    uint32_t idx_commit = 0;
    long img_size=0;
    bool sim_over=false;
    // bool skip_commit=false;

    uint64_t total_inst=0;
    uint32_t idx_commit_num=0;
    uint32_t commit_load_num=0; //提交的load指令数量
    uint32_t commit_store_num=0; //提交的store指令数量
    uint32_t step_skip_num=0;
    uint32_t deadlock_timer=0; //死锁计数器

    void trace_inst_commit(vaddr_t pc,uint32_t inst);
    int  excp_process(int excp_idx, int excp_code,paddr_t epc);
    void first_commit();
    void skip_devices(vaddr_t addr);
    bool checkregs();
    bool store_commit_diff(int idx);
    bool load_commit_diff(int idx);

  public:
    void init_difftest(char *ref_so_file, int port);
    void exit_difftest();
    int  diff_step();
    void display();

    void set_img_size(long size){ 
      img_size=size; 
    }

    vaddr_t get_dut_pc(){
      return dut.base.pc;
    }

    data_t get_dut_gpr(int index){
      assert(index >= 0 && index <MUXDEF(CONFIG_RVE, 16, 32));
      return index;
    }

    void get_ref_reg_display(){
      nemu_proxy->ref_reg_display();
    }

    inline excp_event_t* get_excp_event(){
      return &(dut_commit.excp);
    }

    inline instr_commit_t* get_instr_commit(uint8_t index){
      return &(dut_commit.commit[index]);
    }

    inline csr_state_t* get_csr_state(){
      return &(dut.csr);
    }

    inline store_event_t* get_store_event(uint8_t index){
      return &(dut_commit.store[index]);
    }

    inline load_event_t* get_load_event(uint8_t index){
      return &(dut_commit.load[index]);
    }

    inline greg_state_t* get_greg_state(){
      return &(dut.regs);
    }

};


extern Difftest* difftest;
#endif