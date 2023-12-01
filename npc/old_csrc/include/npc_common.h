#ifndef __NPCCOMMON_H__
#define __NPCCOMMON_H__
#include "macro.h"
#include "npc_debug.h"
#include "npc_utils.h"
#include <assert.h>
#include <VSimTop.h>
#include <iostream>
#include <stdlib.h>
#include <string.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
#include "svdpi.h"
#include "VSimTop__Dpi.h"
typedef MUXDEF(CONFIG_ISA64, uint64_t, uint32_t) word_t;
typedef word_t vaddr_t;
typedef word_t paddr_t;
#define NULL 0
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
typedef unsigned int   uint32_t;
typedef unsigned char  uint8_t;  
typedef unsigned short int  uint16_t; 
void pmem_write(uint32_t addr, int len, uint32_t data);
uint32_t pmem_read(uint32_t addr, int len);
uint8_t* guest_to_host(paddr_t paddr);
word_t host_to_guest(uint8_t *haddr);
word_t expr(char *e, bool *success);
void sdb_mainloop();

VerilatedContext* contextp = NULL;
VerilatedVcdC* tfp = NULL;
VSimTop* top;
bool sim_end=true;
#endif