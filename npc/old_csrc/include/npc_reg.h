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

#ifndef __NPC_REG_H__
#define __NPC_REG_H__
#include <assert.h>

static uint32_t read_reg_idx(int idx) {
  uint32_t* npc_reg_value=(uint32_t*)((char*)top->rootp->SimTop__DOT__RegFile__DOT__rf_0+idx*sizeof(uint32_t));
  return *npc_reg_value;
}

static inline int check_reg_idx(int idx) {
  assert(idx >= 0 && idx <32);
  return idx;
}

void reg_display();
#define gpr(idx) (read_reg_idx(check_reg_idx(idx)))

// static inline const char* reg_name(int idx) {
//   extern const char* regs[];
//   return regs[check_reg_idx(idx)];
// }

#endif
