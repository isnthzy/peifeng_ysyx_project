#include <dlfcn.h>
#include <assert.h>
#include "../include/difftest/difftest.h"
#include "../include/difftest/nemuproxy.h"
#include "../include/npc/npc_monitor.h"
template<typename... Args>
void null_function(Args... args) {}
template<typename... Args>
bool bool_null_function(Args... args) {
    return true;
}
void NemuProxy::init_nemu_proxy(char *ref_so_file, int port){
  if(!difftest_flag){ //NOTE:如果没有打开diff测试，则NemuProxy调用null_function
    printf("\033[0m\033[1;31m No Link-So NemuProxy=NULL \033[0m\n");
    ref_difftest_memcpy = null_function<paddr_t, void*, size_t, bool>;
    ref_difftest_regcpy = null_function<void*, bool>;
    ref_difftest_exec = null_function<uint64_t>;
    ref_difftest_raise_intr = null_function<uint64_t,paddr_t>;
    ref_reg_display = null_function;
    ref_check_load = bool_null_function<paddr_t,int>;
    ref_check_store = bool_null_function<paddr_t,word_t,int>;
    return;
  }

  assert(ref_so_file != NULL);
  printf_green("Using %s for difftest\n", ref_so_file);
  handle = dlopen(ref_so_file, RTLD_LAZY);
  assert(handle);

  ref_difftest_memcpy = (void (*)(paddr_t, void*, size_t, bool))dlsym(handle, "difftest_memcpy");
  assert(ref_difftest_memcpy);

  ref_difftest_regcpy = (void (*)(void *, bool))dlsym(handle, "difftest_regcpy");
  assert(ref_difftest_regcpy);

  ref_difftest_exec =  (void (*)(uint64_t))dlsym(handle, "difftest_exec");
  assert(ref_difftest_exec);

  ref_difftest_raise_intr = (void (*)(uint64_t,paddr_t))dlsym(handle, "difftest_raise_intr");
  assert(ref_difftest_raise_intr);

  ref_reg_display = (void (*)(void))dlsym(handle, "difftest_ref_reg_display");

  ref_check_load  = (bool (*)(paddr_t,int))dlsym(handle, "difftest_check_load");
  assert(ref_check_load);

  ref_check_store = (bool (*)(paddr_t,word_t,int))dlsym(handle, "difftest_check_store");
  assert(ref_check_store);

  //NOTE:再添加动态链接函数时，一定不要忘记在if(!difftest_flag){块中添加对应的null_function

  void (*ref_difftest_init)(int) = (void (*)(int))dlsym(handle, "difftest_init");
  assert(ref_difftest_init);


  Log("Differential testing: %s", ANSI_FMT("ON", ANSI_FG_GREEN));

  ref_difftest_init(port);
} //NOTE:init_difftest抽象为init_nemu_proxy和同步mem与reg两个步骤，分别调用抽象

void NemuProxy::exit_nemu_proxy(){
  if (handle != NULL) {
    dlclose(handle);
  }
}
