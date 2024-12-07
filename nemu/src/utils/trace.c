#include "debug.h"
#include <common.h>

static FILE *itrace_fp = NULL;
static FILE *btrace_fp = NULL;
void init_trace(const char *build_path){
  char itrace_build_path[512];
#ifdef CONFIG_BTRACE
  char btrace_build_path[512];
#endif
  if (build_path != NULL) {
    sprintf(itrace_build_path, "%s/nemu-itrace.txt", build_path);
    itrace_fp = fopen(itrace_build_path, "w");
    Assert(itrace_fp, "Can not open '%s'", itrace_build_path);
    
#ifdef CONFIG_BTRACE
    sprintf(btrace_build_path, "%s/nemu-btrace.txt", build_path);
    btrace_fp = fopen(btrace_build_path, "w");
    Assert(btrace_fp, "Can not open '%s'", btrace_build_path);
#endif
  }else{
    printf("Need build path to generate trace file");
  }
}

void itrace_write(char *itrace_str){
  fprintf(itrace_fp,"%s\n",itrace_str);
}

void btrace_write(char *btrace_str){
  fprintf(btrace_fp,"%s\n",btrace_str);
}