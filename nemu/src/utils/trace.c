#include "debug.h"
#include <common.h>
#include <stdbool.h>

static FILE *itrace_fp = NULL;
static FILE *btrace_fp = NULL;
static FILE *mtrace_fp = NULL;
bool need_build_path = false;
void init_trace(const char *build_path){
  char itrace_build_path[512];
#ifdef CONFIG_BTRACE
  char btrace_build_path[512];
#endif
#ifdef CONFIG_MTRACE_WRITE
  char mtrace_build_path[512];
#endif
  if (build_path == NULL) {
    printf("Need build path to generate trace file");
    need_build_path = true;
  }else{
    sprintf(itrace_build_path, "%s/nemu-itrace.txt", build_path);
    itrace_fp = fopen(itrace_build_path, "w");
    Assert(itrace_fp, "Can not open '%s'", itrace_build_path);

#ifdef CONFIG_BTRACE
    wLog("BTrace is enabled");
    sprintf(btrace_build_path, "%s/nemu-btrace.txt", build_path);
    btrace_fp = fopen(btrace_build_path, "w");
    Assert(btrace_fp, "Can not open '%s'", btrace_build_path);
#endif

#ifdef CONFIG_MTRACE_WRITE
    wLog("MTrace Write is enabled");
    sprintf(mtrace_build_path, "%s/nemu-mtrace.txt", build_path);
    mtrace_fp = fopen(mtrace_build_path, "w");
    Assert(mtrace_fp, "Can not open '%s'", mtrace_build_path);
#endif
  }
}

void itrace_write(char *itrace_str){
  Assert(need_build_path == false, "Need build path to generate itrace file");
  fprintf(itrace_fp,"%s\n",itrace_str);
}

void btrace_write(char *btrace_str){
  Assert(need_build_path == false, "Need build path to generate btrace file");
  fprintf(btrace_fp,"%s\n",btrace_str);
}

void mtrace_write(char *mtrace_str){
  Assert(need_build_path == false, "Need build path to generate mtrace file");
  fprintf(mtrace_fp,"%s\n",mtrace_str);
}