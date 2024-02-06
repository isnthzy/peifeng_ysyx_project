#ifndef __NPCCOMMON_H__
#define __NPCCOMMON_H__
#include "macro.h"
#include "npc_conf.h"
#include <assert.h>
#include <iostream>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include <inttypes.h>
typedef uint32_t word_t;
typedef word_t vaddr_t;
typedef word_t paddr_t;
#define CONFIG_MSIZE 0x8000000
#define START_ADDR   0x80000000
#define CONFIG_MBASE 0x80000000
#define PG_ALIGN __attribute__((aligned(4096)))
#define FMT_WORD MUXDEF(CONFIG_ISA64, "0x%016" PRIx64, "0x%08" PRIx32)
#define FMT_PADDR MUXDEF(PMEM64, "0x%016" PRIx64, "0x%08" PRIx32)
#define PMEM_LEFT  ((paddr_t)CONFIG_MBASE)
#define PMEM_RIGHT ((paddr_t)CONFIG_MBASE + CONFIG_MSIZE - 1)
#define RESET_VECTOR (PMEM_LEFT + CONFIG_PC_RESET_OFFSET)
#define printf_red(...) \
    do { \
        printf("\033[1;32m"); \
        printf(__VA_ARGS__); \
        printf("\033[0m"); \
    } while (0)
#define gpr(idx) (cpu.gpr[check_reg_idx(idx)])

word_t expr(char *e, bool *success);
void sdb_mainloop();
typedef struct {
  word_t gpr[MUXDEF(CONFIG_RVE, 16, 32)];
  vaddr_t pc;
  word_t mstatus;
  word_t mepc;
  word_t mtvec;
  word_t mcause;
}CPU_state;

typedef struct {
  vaddr_t nextpc;
  word_t inst;
}CPU_info;
#include "npc_debug.h"
#endif