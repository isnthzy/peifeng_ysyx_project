#ifndef __NPCCOMMON_H__
#define __NPCCOMMON_H__
#include "macro.h"
#include <assert.h>
#include <iostream>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
typedef uint32_t word_t;
typedef word_t vaddr_t;
typedef word_t paddr_t;
#define CONFIG_MSIZE 0x8000000
#define START_ADDR   0x80000000
#define CONFIG_MBASE 0x80000000
#define PG_ALIGN __attribute__((aligned(4096)))
#define printf_red(...) \
    do { \
        printf("\033[1;32m"); \
        printf(__VA_ARGS__); \
        printf("\033[0m"); \
    } while (0)
#define gpr(idx) (read_reg_idx(check_reg_idx(idx)))
void pmem_write(uint32_t addr, int len, uint32_t data);
uint32_t pmem_read(uint32_t addr, int len);
word_t expr(char *e, bool *success);
void sdb_mainloop();

#include "npc_debug.h"
#endif