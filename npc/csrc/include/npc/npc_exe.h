#ifndef NPC_EXE_H
#define NPC_EXE_H
#include "../npc_common.h"
#include "../util/iringbuf.h"

IRingBuffer mtrace_buffer;
IRingBuffer iring_buffer;

void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
void wp_trace(char *decodelog);
uint64_t get_time();

#endif