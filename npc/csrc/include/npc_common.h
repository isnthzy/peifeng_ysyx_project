#ifndef __NPCCOMMON_H__
#define __NPCCOMMON_H__

#include "util/macro.h"
#include "util/debug.h"
#include "util/utils.h"
#include "npc_conf.h"
#include <stdbool.h>
#include <inttypes.h>
#include <assert.h>

enum { NPC_RUNNING, NPC_STOP, NPC_SUCCESS_END, NPC_ERROR_END, NPC_ABORT, NPC_NOCOMMIT,NPC_QUIT };
typedef uint32_t word_t;
typedef MUXDEF(CONFIG_RV64,uint64_t, uint32_t) vaddr_t;
typedef MUXDEF(CONFIG_RV64,uint64_t, uint32_t) paddr_t;
typedef MUXDEF(CONFIG_RV64,uint64_t, uint32_t) data_t;
#define CONFIG_MSIZE 0x8000000
#define START_ADDR   0x20000000
#define CONFIG_MBASE START_ADDR
#define PG_ALIGN __attribute__((aligned(4096)))
#define FMT_WORD MUXDEF(CONFIG_RV64, "0x%016" PRIx64, "0x%08" PRIx32)
#define FMT_PADDR MUXDEF(CONFIG_RV64, "0x%016" PRIx64, "0x%08" PRIx32)
#define PMEM_LEFT  ((paddr_t)CONFIG_MBASE)
#define PMEM_RIGHT ((paddr_t)CONFIG_MBASE + CONFIG_MSIZE - 1)
#define RESET_VECTOR (PMEM_LEFT + CONFIG_PC_RESET_OFFSET)
#define printf_green(...) \
    do { \
        printf("\033[1;32m"); \
        printf(__VA_ARGS__); \
        printf("\033[0m"); \
    } while (0)
#define printf_red(...) \
    do { \
        printf("\033[1;31m"); \
        printf(__VA_ARGS__); \
        printf("\033[0m"); \
    } while (0)

word_t expr(char *e, bool *success);
// typedef struct {
//   word_t gpr[MUXDEF(CONFIG_RVE, 16, 32)];
//   vaddr_t pc;
//   word_t mstatus;
//   word_t mepc;
//   word_t mtvec;
//   word_t mcause;
// }CPU_state;

// typedef struct {
//   vaddr_t nextpc;
//   word_t inst;
//   bool valid;
// }CPU_info;

// ----------- state -----------

typedef struct {
  int state;
  vaddr_t halt_pc;
} NPCState;

extern NPCState npc_state;


#endif