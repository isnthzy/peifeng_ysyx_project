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

#include "sdb.h"

#define NR_WP 32

typedef struct watchpoint {
  int NO;
  char expr[10000];
  word_t last;
  word_t new;
  struct watchpoint *next;

  /* TODO: Add more members if necessary */

} WP;

static WP wp_pool[NR_WP] = {};
static WP *head = NULL, *free_ = NULL;

WP* new_wp(){
  if(free_==NULL){
    Log("The free_ is NULL\n");
    return 0;
  }
  WP* tmp=free_;
  free_=free_->next;
  tmp->next=head;
  head=tmp;
  return head;
}

void free_wp(WP *wp){
  if(head==NULL){
    Log("No watchpoint to free\n");
    return;
  }

  if(wp==head){
    head=head->next;
    wp->next=free_;
    free_=wp;
  }else{
    WP *tmp=head;
    while(tmp->next==wp) tmp=tmp->next;
    tmp->next=wp->next;
    wp->next=free_;
    free_=wp;
  }
  return;
}

void add_watch(char *expr,word_t addr){
  WP* wp=new_wp();
  strcpy(wp->expr,expr);
  wp->last=addr;
  printf("watchpoint %d: %s\n",wp->NO,expr);
}
void display_watch(){
  WP* h=head;
  if(h==NULL){
    Log("No watchpoints\n");
  }else{
    while(h){
      printf("%d : %s\n",h->NO,h->expr);
      h=h->next;
    }
  }
}
void remove_watch(int num){
  WP* n = &wp_pool[num];
  free_wp(n);
  printf("Delete watchpoint %d: %s\n", n->NO, n->expr);
}
void wp_trace(){
  WP* h=head;
  if(h!=NULL){
    printf("before:");
    while(h){
      printf(" %d : %s,",h->NO,h->expr);
      h=h->next;
    }
    printf("\n");
  }
  bool flag=false;
  while(h){
    bool b;
    word_t new=expr(h->expr,&b);
    if(new!=h->last){
      printf("watchpoint %d: %s\n",h->NO,h->expr);
      printf("Old value = %d\n",h->last);
      printf("Old value = %d\n",new);
      flag=true;
    }
    h=h->next;
  }
  if(flag) nemu_state.state=NEMU_STOP;
}


void init_wp_pool() {
  int i;
  for (i = 0; i < NR_WP; i ++) {
    wp_pool[i].NO = i;
    wp_pool[i].next = (i == NR_WP - 1 ? NULL : &wp_pool[i + 1]);
  }

  head = NULL;
  free_ = wp_pool;
}

/* TODO: Implement the functionality of watchpoint */

