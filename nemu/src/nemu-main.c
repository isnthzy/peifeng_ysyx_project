/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include "monitor/sdb/sdb.h"
#include <common.h>

void init_monitor(int, char *[]);
void am_init_monitor();
void engine_start();
int is_exit_status_bad();

int main(int argc, char *argv[]) {
  /* Initialize the monitor. */
#ifdef CONFIG_TARGET_AM
  am_init_monitor();
#else
  init_monitor(argc, argv);
#endif
  char ea[1024];
  char *val;
  char *evall;
  /* Start engine. */
  FILE *fp=fopen("/home/wangxin/ysyx-workbench/nemu/tools/gen-expr/input", "r");
  if (fp == NULL) {
        printf("无法打开文件。\n");
        return 1;
  }
  int cnt=1;
  while(fgets(ea,1024,fp)){
    val = strtok(ea, " ");
    evall = strtok(ea, " ");
    bool flag=true;
    word_t value_p = expr(evall,&flag);
    word_t u32;
    u32 = strtoul(val, NULL, 10);
    if(u32==value_p) printf("%d true\n",cnt++);
  }
  engine_start();

  return is_exit_status_bad();
}
