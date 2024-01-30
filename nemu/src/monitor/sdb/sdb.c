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

#include <isa.h>
#include <stdlib.h>
#include <memory/vaddr.h>
#include <cpu/cpu.h>
#include <readline/readline.h>
#include <readline/history.h>
#include "sdb.h"
#define NEMU_NOT_REF 0
static int is_batch_mode = false;

void init_regex();
void init_wp_pool();

void add_watch(char *expr,word_t addr);
void display_watch();
void remove_watch(int num);
/* We use the `readline' library to provide more flexibility to read from stdin. */
static char* rl_gets() {
  static char *line_read = NULL;

  if (line_read) {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(nemu) ");

  if (line_read && *line_read) {
    add_history(line_read);
  }

  return line_read;
}

static int cmd_c(char *args) {
  cpu_exec(-1,NEMU_NOT_REF);
  return 0;
}


static int cmd_q(char *args) {
  nemu_state.state = NEMU_QUIT;
  //pa1 rtfsc优美的退出
  return -1;
}

static int cmd_si(char *args) {
  char *arg = strtok(NULL, " ");
  int step=1;
  if(arg!=NULL){
    step=atoi(arg); 
  } 
  cpu_exec(step,NEMU_NOT_REF);
  return 0;
}

static int cmd_info(char *args) {
  char *arg = strtok(NULL, " ");
  char subcmd = arg[0];
  if(subcmd=='r'){
    isa_reg_display();
  }else if(subcmd=='w'){
    display_watch();
  }else{
    Log("Invalid info command\n");
  }
  return 0;
}

static int cmd_x(char *args) {
  char *arg = strtok(NULL, " ");
  int s1 = atoi(arg);
  char *EXPR  = strtok(NULL, " ");
  bool flag=true;
  word_t addr = expr(EXPR,&flag);
  if(flag==false){
    Log("There is an error in the expression, please retype it\n");
    return 0;
  }
  // vaddr_t addr;
  // sscanf(EXPR,"%x", &addr);
  int i,j;
  printf("addr        mem\n");
  for(i=0;i<s1;i++){
    printf("0x%08x: ",addr);
    vaddr_t data = vaddr_read(addr,4);
    
    for(j=3;j>=0;j--){
      printf("0x%02x ",(data>>(j*8))&0xff);
      // printf("0x%02x ",(data&0xff);
      // data=data>>8; //内存显示顺序的两种方式,顺or逆
    }
    printf("\n");
    addr+=4;
  }
  return 0;
}

static int cmd_p(char *args) {
  // char *arg = strtok(NULL, " ");
  bool flag=true;
  word_t value_p = expr(args,&flag);
  if(flag==false&&args==NULL){
    Log("There is an error in the expression, please retype it\n");
    return 0;
  }else printf("%d\n",value_p);
  return 0;
}

static int cmd_w(char *args) {
  char *EXPR  = strtok(NULL, " ");
  if(EXPR==NULL){
    Log("There is an error in the expression, please retype it\n");
    return 0;
  }
  bool flag=true;
  word_t addr = expr(EXPR,&flag);
  if(flag==false){
    Log("There is an error in the expression, please retype it\n");
    return 0;
  }
  add_watch(EXPR,addr);
  return 0;
}

static int cmd_d(char *args) {
  char *NUM  = strtok(NULL, " ");
  int num = atoi(NUM);
  remove_watch(num);
  return 0;
}


bool difftest_mode=true;
static int cmd_detach(char *args) {
  IFNDEF(CONFIG_DIFFTEST,printf("not have difftest config");return;)
  printf("difftest is close\n");
  difftest_mode=false;
  return 0;
}

static int cmd_attach(char *args) {
  IFNDEF(CONFIG_DIFFTEST,printf("not have difftest config");return;)
  printf("wait.... it's slow(About 10 seconds) \n");
  void difftest_sync_mem_reg_to_ref();
  printf("difftest is open\n");
  difftest_mode=true;
  return 0;
}

static int cmd_help(char *args);

static struct {
  const char *name;
  const char *description;
  int (*handler) (char *);
} cmd_table [] = {
  { "help", "Display information about all supported commands", cmd_help },
  { "c", "Continue the execution of the program", cmd_c },
  { "q", "Exit NEMU", cmd_q },
  { "si", " \"si [N]\" Lets the program pause after executing N instructions in a single step.\
When N is not given, the default is 1", cmd_si },
  { "info", "info r:Printing Register Status \n \
  info w:Print watchpoint information",cmd_info },
  { "x", "\"x N EXPR\"Find the value of the expression EXPR, use the result as the starting memory \
address,\n and output N consecutive 4-byte outputs in hexadecimal." ,cmd_x},
  { "p", "\"p EXPR\"Find the value of the expression EXPR" , cmd_p},
  { "w", "\"w EXPR\"Suspends program execution when the value of expression EXPR changes." ,cmd_w},
  { "d", "\"d N\"Delete the monitoring point with serial number N" ,cmd_d},
  { "detach", "\"d N\"close difftest mode" ,cmd_detach},
  { "attach", "\"d N\"open  difftest mode" ,cmd_attach},
  /* TODO: Add more commands */

};

#define NR_CMD ARRLEN(cmd_table)

static int cmd_help(char *args) {
  /* extract the first argument */
  char *arg = strtok(NULL, " ");
  int i;

  if (arg == NULL) {
    /* no argument given */
    for (i = 0; i < NR_CMD; i ++) {
      printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
    }
  }
  else {
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(arg, cmd_table[i].name) == 0) {
        printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
        return 0;
      }
    }
    printf("Unknown command '%s'\n", arg);
  }
  return 0;
}

void sdb_set_batch_mode() {
  is_batch_mode = true;
}

void sdb_mainloop() {
  if (is_batch_mode) {
    cmd_c(NULL);
    return;
  }

  for (char *str; (str = rl_gets()) != NULL; ) {
    char *str_end = str + strlen(str);

    /* extract the first token as the command */
    char *cmd = strtok(str, " ");
    if (cmd == NULL) { continue; }

    /* treat the remaining string as the arguments,
     * which may need further parsing
     */
    char *args = cmd + strlen(cmd) + 1;
    if (args >= str_end) {
      args = NULL;
    }

#ifdef CONFIG_DEVICE
    extern void sdl_clear_event_queue();
    sdl_clear_event_queue();
#endif

    int i;
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(cmd, cmd_table[i].name) == 0) {
        if (cmd_table[i].handler(args) < 0) { return; }
        break;
      }
    }

    if (i == NR_CMD) { printf("Unknown command '%s'\n", cmd); }
  }
}

void init_sdb() {
  /* Compile the regular expressions. */
  init_regex();

  /* Initialize the watchpoint pool. */
  init_wp_pool();
}
