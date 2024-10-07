#ifndef NEMUPROXY_H
#define NEMUPROXY_H 
#include "../npc_common.h"


class NemuProxy {
  private:
    void* handle = NULL;
  public:
    void init_nemu_proxy(char *ref_so_file, int port);
    void exit_nemu_proxy();
    void (*ref_difftest_memcpy)(paddr_t addr, void *buf, size_t n, bool direction) = NULL;
    void (*ref_difftest_regcpy)(void *dut, bool direction) = NULL;
    void (*ref_difftest_exec)(uint64_t n) = NULL;
    void (*ref_difftest_raise_intr)(uint64_t NO,paddr_t epc) = NULL;
    void (*ref_reg_display)() = NULL;
    bool (*ref_check_load)(paddr_t addr,int type) = NULL;
    bool (*ref_check_store)(paddr_t addr,word_t data,int len) = NULL;

};

#endif // NEMUPROXY_H


