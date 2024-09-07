#ifndef NPC_MEMORY_H
#define NPC_MEMORY_H

#include "../npc_common.h"
#include "../util/iringbuf.h"
word_t paddr_read(paddr_t addr, int len);
void paddr_write(paddr_t addr, int len, word_t data);
word_t pmem_read(paddr_t addr, int len);
void pmem_write(paddr_t addr, int len, word_t data);

uint8_t* guest_to_host(paddr_t paddr);
paddr_t host_to_guest(uint8_t *haddr);

extern IRingBuffer mtrace_buffer;

#endif