#include "../include/npc_common.h"
#include "../include/npc/npc_device.h"
#include <SDL2/SDL.h>
#define TIMER_HZ 60
#define DEVICE_BASE 0xa0000000
#define SERIAL_PORT (DEVICE_BASE + 0x00003f8)
#define RTC_ADDR    (DEVICE_BASE + 0x0000048)
#define KBD_ADDR    (DEVICE_BASE + 0x0000060)
#define VGACTL_ADDR (DEVICE_BASE + 0x0000100)
#define SYNC_ADDR   (VGACTL_ADDR + 4)
#define FB_ADDR     (DEVICE_BASE + 0x1000000)


void init_device() {

  // IFDEF(CONFIG_HAS_VGA, init_vga());
  // IFDEF(DEVICE_HAS_KEYBOARD, init_i8042());

}


void device_update() {
  // static uint64_t last = 0;
  // uint64_t now = get_time();
  // if (now - last < 1000000 / TIMER_HZ) {
  //   return;
  // }
  // last = now;
  // IFDEF(CONFIG_HAS_VGA, vga_update_screen());
  // SDL_Event event;
  // while (SDL_PollEvent(&event)) {
  //   switch (event.type) {
  //     case SDL_QUIT:
  //       npc_state.state = NPC_QUIT;
  //       break;
  //     // If a key was pressed
  //     case SDL_KEYDOWN:
  //     case SDL_KEYUP: {
  //       uint8_t k = event.key.keysym.scancode;
  //       bool is_keydown = (event.key.type == SDL_KEYDOWN);
  //       send_key(k, is_keydown);
  //       break;
  //     }
  //     default: break;
  //   }
  // }
}



void  device_write(paddr_t addr,int len,word_t data){
  // // difftest_skip_ref();
  // if(addr==SERIAL_PORT){
  //   putchar(data);
  //   return;
  // }
  // if(addr==SYNC_ADDR){
  //   change_vga_sync(data);
  //   return;
  // }

  // if(addr>=FB_ADDR&&addr<=FB_ADDR+screen_size()){
  //   vmem_write(addr,len,data);
  //   return;
  // }
  out_of_bound(addr);
  return;
}



word_t device_read(paddr_t addr,int len){
  // static uint64_t rtc_us=0;
  // // difftest_skip_ref();
  // if(addr==RTC_ADDR||addr==RTC_ADDR+4){
  //   rtc_us=get_time();
  //   if(addr==RTC_ADDR)   return (uint32_t)rtc_us;
  //   if(addr==RTC_ADDR+4) return rtc_us >> 32;
  // }
  // if(addr==KBD_ADDR)    return key_dequeue();
  // if(addr==VGACTL_ADDR) return get_vga_vgactl();
  // if(addr>=FB_ADDR&&addr<=FB_ADDR+screen_size()){
  //   return vmem_read(addr,len);
  // }
  out_of_bound(addr);
  return 0;
}