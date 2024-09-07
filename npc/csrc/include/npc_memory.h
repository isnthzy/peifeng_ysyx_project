#ifndef NPC_MEMORY_H
#define NPC_MEMORY_H

#include "npc_common.h"
#include "iringbuf.h"
word_t paddr_read(paddr_t addr, int len);
void paddr_write(paddr_t addr, int len, word_t data);
word_t pmem_read(paddr_t addr, int len);
void pmem_write(paddr_t addr, int len, word_t data);

extern IRingBuffer mtrace_buffer;

#endif