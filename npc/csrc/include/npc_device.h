#ifndef NPC_DEVICE_H
#define NPC_DEVICE_H
#include "npc_common.h"


void  device_write(paddr_t addr,int len,word_t data);
word_t device_read(paddr_t addr,int len);
#endif