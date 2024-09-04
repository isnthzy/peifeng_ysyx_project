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

#ifndef __ISA_RISCV_H__
#define __ISA_RISCV_H__

#include <common.h>

typedef struct {
  word_t  gpr[MUXDEF(CONFIG_RVE, 16, 32)];
  vaddr_t pc;
  word_t  mstatus;
  word_t  mepc;
  word_t  mtvec;
  word_t  mcause;
} MUXDEF(CONFIG_RV64, riscv64_CPU_state, riscv32_CPU_state);

typedef struct {
  vaddr_t  pc;
  uint32_t inst;
} base_state_t; //不参与dpi提交，在diff_step时保留关键信息

typedef struct {
  word_t  gpr[32];
} greg_state_t;

typedef struct __attribute__((packed)) {
  word_t  mstatus;
  word_t  mtvec;
  word_t  mepc;
  word_t  mcause;
} csr_state_t;
typedef struct {
  base_state_t base;
  greg_state_t regs;
  csr_state_t  csr;
} MUXDEF(CONFIG_RV64, riscv64_NPC_state, riscv32_NPC_state);

// decode
typedef struct {
  union {
    uint32_t val;
  } inst;
} MUXDEF(CONFIG_RV64, riscv64_ISADecodeInfo, riscv32_ISADecodeInfo);

#define isa_mmu_check(vaddr, len, type) (MMU_DIRECT)

#endif
