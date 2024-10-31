#ifndef NPC_DEVICE_H
#define NPC_DEVICE_H
#include "../npc_common.h"
#ifdef CONFIG_YSYXSOC
  #define GPIO_SW_ADDR 0x10002004
  #define GPIO_SW_SIZE 0x4
  #define SPI_ADDR    0x10001000
  #define SPI_SIZE    0x1000
  #define SERIAL_PORT 0x10000000
  #define SERIAL_SIZE 0x1000

  #define RTC_ADDR    0x02000000
  #define RTC_SIZE    0x8
  #define KBD_ADDR    0x10011000
  #define KBD_SIZE    0x8
#else //npc 把npc的device_base映射到0x03000000-0x0f000000 //以防止覆盖其他设备
  #define DEVICE_BASE 0x03000000
  #define DEVICE_SIZE 0x0c000000
  #define SERIAL_PORT (DEVICE_BASE + 0x00003f8) //UART
  #define SERIAL_SIZE 0x4
  #define RTC_ADDR    (DEVICE_BASE + 0x0000048)
  #define RTC_SIZE    0x8
  #define KBD_ADDR    (DEVICE_BASE + 0x0000060)
  #define KBD_SIZE    0x4
  #define VGACTL_ADDR (DEVICE_BASE + 0x0000100)
  #define VGACTL_SIZE 0x4
  #define SYNC_ADDR   (VGACTL_ADDR + 4)
  #define FB_ADDR     (DEVICE_BASE + 0x1000000)
#endif

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