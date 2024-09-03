#ifndef DIFFSTATE_H
#define DIFFSTATE_H
#include "npc_common.h"

typedef struct {
  uint8_t  excp_valid = 0;
  uint8_t  is_mret    = 0;
  uint32_t interrupt = 0;
  uint32_t exception = 0;
  vaddr_t  exceptionPC = 0;
  uint32_t exceptionIst = 0;
} excp_event_t;

typedef struct {
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

typedef struct {
  uint8_t  valid = 0;
  paddr_t  paddr;
  vaddr_t  vaddr;
  data_t   data;
} store_event_t;

typedef struct {
  uint8_t valid = 0;
  paddr_t paddr;
  vaddr_t vaddr;
  data_t  data;
} load_event_t;

typedef struct {
  data_t  gpr[32];
} greg_state_t;

typedef struct {
  excp_event_t excp;
  instr_commit_t commit[DIFFTEST_COMMIT_WIDTH];
  greg_state_t regs;
  csr_state_t  csr;
  store_event_t store[DIFFTEST_COMMIT_WIDTH];
  load_event_t load[DIFFTEST_COMMIT_WIDTH];
} difftest_core_state_t;

class Difftest{
  private:
    difftest_core_state_t dut;
    difftest_core_state_t ref;

    data_t* dut_regs_ptr=(data_t *)&dut.regs;
    data_t* ref_regs_ptr=(data_t *)&ref.regs;


    /* the index of instructions per commit */
    uint32_t idx_commit = 0;
  public:
    int diff_step();
    
    void display();

    inline excp_event_t* get_excp_event(){
      return &(dut.excp);
    }

    inline instr_commit_t* get_instr_commit(uint8_t index){
      return &(dut.commit[index]);
    }

    inline csr_state_t* get_csr_state(){
      return &(dut.csr);
    }

    inline store_event_t* get_store_event(uint8_t index){
      return &(dut.store[index]);
    }

    inline load_event_t* get_load_event(uint8_t index){
      return &(dut.load[index]);
    }

    inline greg_state_t* get_greg_state(){
      return &(dut.regs);
    }

};

#endif