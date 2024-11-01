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
#define PG_ALIGN __attribute__((aligned(4096)))
#define FMT_WORD MUXDEF(CONFIG_RV64, "0x%016" PRIx64, "0x%08" PRIx32)
#define FMT_PADDR MUXDEF(CONFIG_RV64, "0x%016" PRIx64, "0x%08" PRIx32)
#define PMEM_LEFT  ((paddr_t)CONFIG_MBASE)
#define PMEM_RIGHT ((paddr_t)CONFIG_MBASE + CONFIG_MSIZE - 1)

#define printf_green(...) \
    do { \
        printf("\033[1;32m"); \
        printf(__VA_ARGS__); \
        printf("\033[0m"); \
        fflush(NULL); \
    } while (0)
#define printf_red(...) \
    do { \
        printf("\033[1;31m"); \
        printf(__VA_ARGS__); \
        printf("\033[0m"); \
        fflush(NULL); \
    } while (0)

word_t expr(char *e, bool *success);

// ----------- state -----------

typedef struct {
  int state;
  vaddr_t halt_pc;
} NPCState;

extern NPCState npc_state;


#endif