#ifndef NPC_EXE_H
#define NPC_EXE_H
#include "../npc_common.h"
#include "../util/iringbuf.h"

uint64_t get_time();
void init_traces();
void step_and_dump_wave();

#endif