#include <common.h>

static FILE *itrace_fp = NULL;
void init_trace(const char *build_path){
  char itrace_build_path[512];
  // char mtrace_build_path[512];
  if (build_path != NULL) {
    sprintf(itrace_build_path, "%s/npc-itrace.txt", build_path);
    // sprintf(mtrace_build_path, "%s/mtrace.txt", build_path);
    itrace_fp = fopen(itrace_build_path, "w");
    Assert(itrace_fp, "Can not open '%s'", itrace_build_path);
  }else{
    printf("Need build path to generate itrace file");
  }
}

void itrace_write(char *itrace_str){
  fprintf(itrace_fp,"%s\n",itrace_str);
}