#ifndef NPC_MEMORY_H
#define NPC_MEMORY_H

#include "../npc_common.h"
#include "../util/iringbuf.h"
word_t paddr_read(paddr_t addr, int len);
void paddr_write(paddr_t addr, int len, word_t data);
word_t pmem_read(paddr_t addr, int len);
void pmem_write(paddr_t addr, int len, word_t data);

uint8_t* guest_to_host(paddr_t paddr);
// paddr_t host_to_guest(uint8_t *haddr);

enum {SOC_DEVICE_ERROR, SOC_PMEM, SOC_DEVICE_MROM, SOC_DEVICE_FLASH, SOC_DEVICE_PSRAM};


void mtrace_store(int pc,int addr,int data,int len);
void mtrace_load(int pc,int addr,int data,int len);
void init_mem();

extern IRingBuffer mtrace_buffer;

#endif