#ifndef NPC_DEVICE_H
#define NPC_DEVICE_H
#include "../npc_common.h"

bool is_skip_addr(vaddr_t addr);
void  device_write(paddr_t addr,int len,word_t data);
word_t device_read(paddr_t addr,int len);
void init_device();

uint64_t get_time();
void out_of_bound(paddr_t addr);
void difftest_skip_ref();
void device_update();

void send_key(uint8_t scancode, bool is_keydown);
void init_i8042();
void init_vga();
void vga_update_screen();
void change_vga_sync(word_t data);
uint32_t get_vga_vgactl();
uint32_t key_dequeue();
uint32_t screen_size();
word_t vmem_read(paddr_t addr,int len);
void vmem_write(paddr_t addr,int len,word_t data);
void reg_dut_display();
#endif