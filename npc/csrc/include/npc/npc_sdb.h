#ifndef NPC_SDB_H
#define NPC_SDB_H

#include "../npc_common.h"
word_t expr(char *e, bool *success);
void reg_dut_display();
void npc_exev(uint64_t step);
void sdb_mainloop();
void sdb_set_batch_mode();
void init_regex();
void init_wp_pool();

void add_watch(char *expr,word_t addr);
void display_watch();
void remove_watch(int num);


#endif
