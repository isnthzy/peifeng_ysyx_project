/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#ifndef __MEMORY_PADDR_H__
#define __MEMORY_PADDR_H__

#include <common.h>

#define PMEM_LEFT  ((paddr_t)CONFIG_MBASE)
#define PMEM_RIGHT ((paddr_t)CONFIG_MBASE + CONFIG_MSIZE - 1)
#ifdef CONFIG_SOC_DEVICE
#define RESET_VECTOR CONFIG_SOC_RESET_PC
#else
#define RESET_VECTOR (PMEM_LEFT + CONFIG_PC_RESET_OFFSET)
#endif

#ifdef CONFIG_SOC_DEVICE
#define MROM_LEFT  ((paddr_t)CONFIG_SOC_MROM_BASE)
#define MROM_RIGHT ((paddr_t)CONFIG_SOC_MROM_BASE + CONFIG_SOC_MROM_SIZE - 1)
#define SRAM_LEFT  ((paddr_t)CONFIG_SOC_SRAM_BASE)
#define SRAM_RIGHT ((paddr_t)CONFIG_SOC_SRAM_BASE + CONFIG_SOC_SRAM_SIZE - 1)
#define FLASH_LEFT  ((paddr_t)CONFIG_SOC_FLASH_BASE)
#define FLASH_RIGHT ((paddr_t)CONFIG_SOC_FLASH_BASE + CONFIG_SOC_FLASH_SIZE - 1)
#define PSRAM_LEFT  ((paddr_t)CONFIG_SOC_PSRAM_BASE)
#define PSRAM_RIGHT ((paddr_t)CONFIG_SOC_PSRAM_BASE + CONFIG_SOC_PSRAM_SIZE - 1)
#endif

/* convert the guest physical address in the guest program to host virtual address in NEMU */
uint8_t* guest_to_host(paddr_t paddr);
/* convert the host virtual address in NEMU to guest physical address in the guest program */
paddr_t host_to_guest(uint8_t *haddr);

void store_commit_queue_push(paddr_t addr,word_t data,int len);
void load_commit_queue_push(paddr_t addr,word_t data,int type);
bool check_load_commit(paddr_t addr,int type);
bool check_store_commit(paddr_t addr,word_t data,int len);

#ifdef CONFIG_SOC_DEVICE
enum {SOC_DEVICE_ERROR, SOC_DEVICE_MROM, SOC_DEVICE_SRAM, SOC_DEVICE_FLASH, SOC_DEVICE_PSRAM, SOC_DEVICE_SDRAM};
static inline int in_soc_device(paddr_t addr) {
  int in_device_num=0;
  if(addr - CONFIG_SOC_MROM_BASE < CONFIG_SOC_MROM_SIZE)   in_device_num=SOC_DEVICE_MROM;
  if(addr - CONFIG_SOC_SRAM_BASE < CONFIG_SOC_SRAM_SIZE)   in_device_num=SOC_DEVICE_SRAM;
  if(addr - CONFIG_SOC_FLASH_BASE < CONFIG_SOC_FLASH_SIZE) in_device_num=SOC_DEVICE_FLASH;
  if(addr - CONFIG_SOC_PSRAM_BASE < CONFIG_SOC_PSRAM_SIZE) in_device_num=SOC_DEVICE_PSRAM;
  if(addr - CONFIG_SOC_SDRAM_BASE < CONFIG_SOC_SDRAM_SIZE) in_device_num=SOC_DEVICE_SDRAM;
  return in_device_num;
}
#endif
static inline bool in_pmem(paddr_t addr) {
#ifdef CONFIG_SOC_DEVICE
  if(in_soc_device(addr)) return true;
  else return false;
#else
  return addr - CONFIG_MBASE < CONFIG_MSIZE;
#endif
}

word_t paddr_read(paddr_t addr, int len,int model);
void paddr_write(paddr_t addr, int len, word_t data);

typedef struct{
  paddr_t addr; 
  word_t  data;
  int     len;
  vaddr_t atpc;
  bool    valid;
}store_commit_t; //存的都是未对齐的

typedef struct{
  paddr_t addr; 
  word_t  data;
  int     type;
  vaddr_t atpc;
  bool    valid;
}load_commit_t;  //存的都是未对齐的

#endif
