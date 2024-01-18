#include "../include/npc_common.h"
#include <SDL2/SDL.h>
#define TIMER_HZ 60
#define DEVICE_BASE 0xa0000000
#define SERIAL_PORT (DEVICE_BASE + 0x00003f8)
#define RTC_ADDR    (DEVICE_BASE + 0x0000048)
#define KBD_ADDR    (DEVICE_BASE + 0x0000060)
#define VGACTL_ADDR (DEVICE_BASE + 0x0000100)
#define SYNC_ADDR   (VGACTL_ADDR + 4)
#define FB_ADDR     (DEVICE_BASE + 0x1000000)

#ifndef CONFIG_DIFFTEST
void difftest_skip_ref(){}
#endif
extern CPU_state cpu;
extern uint64_t get_time();
extern void out_of_bound(paddr_t addr);
extern void difftest_skip_ref();

void send_key(uint8_t scancode, bool is_keydown);
void init_i8042();
void init_vga();
void vga_update_screen();
void change_vga_sync(word_t data);
uint32_t get_vga_vgactl();
uint32_t key_dequeue();
uint32_t screen_size();
word_t vmem_read(paddr_t addr);
void vmem_write(paddr_t addr,word_t data);

void init_device() {
  // IFDEF(CONFIG_HAS_SERIAL, init_serial());
  // IFDEF(CONFIG_HAS_TIMER, init_timer());
  IFDEF(CONFIG_HAS_VGA, init_vga());
  IFDEF(DEVICE_HAS_KEYBOARD, init_i8042());
  // IFDEF(CONFIG_HAS_AUDIO, init_audio());
  // IFDEF(CONFIG_HAS_DISK, init_disk());
  // IFDEF(CONFIG_HAS_SDCARD, init_sdcard());
  // IFNDEF(CONFIG_TARGET_AM, init_alarm());
}


void device_update() {
  static uint64_t last = 0;
  uint64_t now = get_time();
  if (now - last < 1000000 / TIMER_HZ) {
    return;
  }
  last = now;
  IFDEF(CONFIG_HAS_VGA, vga_update_screen());
  SDL_Event event;
  while (SDL_PollEvent(&event)) {
    switch (event.type) {
      case SDL_QUIT:
        npc_state.state = NPC_QUIT;
        break;
      // If a key was pressed
      case SDL_KEYDOWN:
      case SDL_KEYUP: {
        uint8_t k = event.key.keysym.scancode;
        bool is_keydown = (event.key.type == SDL_KEYDOWN);
        send_key(k, is_keydown);
        break;
      }
      default: break;
    }
  }
}

// void  device_write(paddr_t addr,word_t data){
//   difftest_skip_ref();
//   switch (addr)
//   {
//   case SERIAL_PORT:
//     putchar(data);
//     break;

//   case SYNC_ADDR:
//     change_vga_sync(data);
//     break;

//   case FB_ADDR:
//   screen_size();
//   default:
//     out_of_bound(addr);
//     break;
//   }
//   return;
// }

void  device_write(paddr_t addr,word_t data){
  difftest_skip_ref();
  if(addr==SERIAL_PORT){
    putchar(data);
    return;
  }
  if(addr==SYNC_ADDR){
    change_vga_sync(data);
    return;
  }
  if(addr>=FB_ADDR&&addr<=FB_ADDR+screen_size()){
    vmem_write(addr,data);
  }
  out_of_bound(addr);
  return;
}

// word_t device_read(paddr_t addr){
//   static uint64_t rtc_us=0;
//   difftest_skip_ref();
//   switch (addr)
//   {
//   case RTC_ADDR:
//   case RTC_ADDR+4:
//     rtc_us=get_time();
//     if(addr==RTC_ADDR)   return (uint32_t)rtc_us;
//     if(addr==RTC_ADDR+4) return rtc_us >> 32;
//     break;
//   case KBD_ADDR:
//     return key_dequeue();
//     break;

//   case VGACTL_ADDR:
//     return get_vga_vgactl();
//     break;

//   default:
//     out_of_bound(addr);
//     break;
//   }
//   return 0;
// }


word_t device_read(paddr_t addr){
  static uint64_t rtc_us=0;
  difftest_skip_ref();
  if(addr==RTC_ADDR||addr==RTC_ADDR+4){
    rtc_us=get_time();
    if(addr==RTC_ADDR)   return (uint32_t)rtc_us;
    if(addr==RTC_ADDR+4) return rtc_us >> 32;
  }
  if(addr==KBD_ADDR)    return key_dequeue();
  if(addr==VGACTL_ADDR) return get_vga_vgactl();
  if(addr>=FB_ADDR&&addr<=FB_ADDR+screen_size()){
    return vmem_read(addr);
  }
  out_of_bound(addr);
  return 0;
}