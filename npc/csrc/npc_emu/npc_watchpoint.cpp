#include "../include/npc_common.h"
#include "../include/npc/npc_sdb.h"
#define NR_WP MUXDEF(CONFIG_RVE, 16, 32)

typedef struct watchpoint {
  int NO;
  char expr[1000];
  word_t last;
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
  }
  else{
    WP *tmp=head;
    while(tmp->next!=wp) tmp=tmp->next;
    if(tmp==NULL) return;
    tmp->next=wp->next;
    wp->next=free_;
    free_=wp;
  }
  return;
}

void add_watch(char *expr,word_t addr){
  WP* wp=new_wp();
  if(expr==NULL){
    printf("Error:add null expr");
    return;
  }
  strcpy(wp->expr,expr);
  wp->last=addr;
  printf("watchpoint %d: %s\n",wp->NO,expr);
}
void display_watch(){
  WP* h=head;
  if(h==NULL){
    Log("No watchpoints");
  }else{
    printf("Num     What    Value\n");
    while(h){
      printf("%-8d%-8s%u(0x%08x)\n",h->NO,h->expr,h->last,h->last);
      h=h->next;
    }
  }
}
void remove_watch(int num){
  WP* n = &wp_pool[num];
  free_wp(n);
  printf("Delete watchpoint %d: %s\n", n->NO, n->expr);
}
void wp_trace(char *decodelog){
  WP* h=head;
  bool flag=false;
  bool flagput=false;
  while(h){
    bool b;
    word_t new_value=expr(h->expr,&b);
    if(new_value!=h->last){
      if(flagput==false){
        puts(decodelog);
        flagput=true;
      }
      printf("watchpoint %d: %s\n",h->NO,h->expr);
      printf("Old value = %u(0x%08x)\n",h->last,h->last);
      printf("New value = %u(0x%08x)\n",new_value,new_value);
      h->last=new_value;
      flag=true;
    }
    h=h->next;
  }
  if(flag) npc_state.state=NPC_STOP;
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

