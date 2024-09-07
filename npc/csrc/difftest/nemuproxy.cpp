#include "../include/difftest/difftest.h"
#include "../include/difftest/nemuproxy.h"
#include <dlfcn.h>
#include <assert.h>

void NemuProxy::init_nemu_proxy(char *ref_so_file, int port){
  assert(ref_so_file != NULL);
  printf("Using %s for difftest\n", ref_so_file);
  handle = dlopen(ref_so_file, RTLD_LAZY);
  assert(handle);

  ref_difftest_memcpy = (void (*)(paddr_t, void*, size_t, bool))dlsym(handle, "difftest_memcpy");
  assert(ref_difftest_memcpy);

  ref_difftest_regcpy = (void (*)(void *, bool))dlsym(handle, "difftest_regcpy");
  assert(ref_difftest_regcpy);

  ref_difftest_exec =  (void (*)(uint64_t))dlsym(handle, "difftest_exec");
  assert(ref_difftest_exec);

  ref_difftest_raise_intr = (void (*)(uint64_t))dlsym(handle, "difftest_raise_intr");
  assert(ref_difftest_raise_intr);

  void (*ref_difftest_init)(int) = (void (*)(int))dlsym(handle, "difftest_init");
  assert(ref_difftest_init);

  ref_reg_display = (void (*)(void))dlsym(handle, "difftest_ref_reg_display");

  Log("Differential testing: %s", ANSI_FMT("ON", ANSI_FG_GREEN));

  ref_difftest_init(port);
} //NOTE:init_difftest抽象为init_nemu_proxy和同步mem与reg两个步骤，分别调用抽象

void NemuProxy::exit_nemu_proxy(){
  if (handle != NULL) {
    dlclose(handle);
  }
}
