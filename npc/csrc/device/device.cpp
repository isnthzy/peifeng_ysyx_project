#include "../include/npc_common.h"
#include "../include/npc/npc_device.h"
#include <SDL2/SDL.h>
#define TIMER_HZ 60

bool is_skip_addr(vaddr_t addr){
  int ret = false;
#ifdef CONFIG_YSYXSOC
  if(addr>=GPIO_SW_ADDR&&addr<GPIO_SW_ADDR+GPIO_SW_SIZE) ret = true;
  if(addr>=SPI_ADDR    &&addr<SPI_ADDR+SPI_SIZE) ret = true;
#else //npc
  if(addr>=VGACTL_ADDR &&addr<VGACTL_ADDR+VGACTL_SIZE) ret = true;
  if(addr>=FB_ADDR     &&addr<=FB_ADDR+screen_size()) ret = true;
#endif
  if(addr>=SERIAL_PORT &&addr<SERIAL_PORT+SERIAL_SIZE) ret = true;
  if(addr>=RTC_ADDR    &&addr<RTC_ADDR+RTC_SIZE) ret = true;
  if(addr>=KBD_ADDR    &&addr<KBD_ADDR+KBD_SIZE) ret = true;

  return ret;
}

void init_device() {
#ifndef CONFIG_YSYXSOC
  IFDEF(CONFIG_HAS_VGA, init_vga());
  IFDEF(DEVICE_HAS_KEYBOARD, init_i8042());
#endif
}


void device_update() {
#ifndef CONFIG_YSYXSOC
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
#endif
}



void  device_write(paddr_t addr,int len,word_t data){
#ifndef CONFIG_YSYXSOC
  // difftest_skip_ref();
  if(addr>=SERIAL_PORT&&addr<SERIAL_PORT+SERIAL_SIZE){
    putchar(data);
    return;
  }
  if(addr==SYNC_ADDR){
    change_vga_sync(data);
    return;
  }

  if(addr>=FB_ADDR&&addr<=FB_ADDR+screen_size()){
    vmem_write(addr,len,data);
    return;
  }
#endif
  out_of_bound(addr);
  return;
}



word_t device_read(paddr_t addr,int len){
#ifndef CONFIG_YSYXSOC
  static uint64_t rtc_us=0;
  // difftest_skip_ref();
  if(addr==RTC_ADDR||addr==RTC_ADDR+4){
    rtc_us=get_time();
    if(addr==RTC_ADDR)   return (uint32_t)rtc_us;
    if(addr==RTC_ADDR+4) return rtc_us >> 32;
  }
  if(addr==KBD_ADDR&&addr<KBD_SIZE)    return key_dequeue();
  if(addr==VGACTL_ADDR) return get_vga_vgactl();
  if(addr>=FB_ADDR&&addr<=FB_ADDR+screen_size()){
    return vmem_read(addr,len);
  }
#endif
  out_of_bound(addr);
  return 0;
}