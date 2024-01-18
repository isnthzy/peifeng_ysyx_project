

#include "../include/npc_common.h"
#include <SDL2/SDL.h>
#define DEVICE_BASE 0xa0000000
#define FB_ADDR     (DEVICE_BASE + 0x1000000)
#define SCREEN_W (MUXDEF(CONFIG_VGA_SIZE_800x600, 800, 400))
#define SCREEN_H (MUXDEF(CONFIG_VGA_SIZE_800x600, 600, 300))

static uint32_t screen_width() {
  return SCREEN_W;
}

static uint32_t screen_height() {
  return SCREEN_H;
}

uint32_t screen_size() {
  return screen_width() * screen_height() * sizeof(uint32_t);
}

static uint8_t vmem[480000];
static uint32_t vgactl_port_base[2];

#ifdef CONFIG_VGA_SHOW_SCREEN
#include <SDL2/SDL.h>

static SDL_Renderer *renderer = NULL;
static SDL_Texture *texture = NULL;

static void init_screen() {
  SDL_Window *window = NULL;
  char title[128];
  sprintf(title, "%s-NPC", str(ISA__RISCV));
  SDL_Init(SDL_INIT_VIDEO);
  SDL_CreateWindowAndRenderer(
      SCREEN_W * (MUXDEF(CONFIG_VGA_SIZE_400x300, 2, 1)),
      SCREEN_H * (MUXDEF(CONFIG_VGA_SIZE_400x300, 2, 1)),
      0, &window, &renderer);
  SDL_SetWindowTitle(window, title);
  texture = SDL_CreateTexture(renderer, SDL_PIXELFORMAT_ARGB8888,
      SDL_TEXTUREACCESS_STATIC, SCREEN_W, SCREEN_H);
  SDL_RenderPresent(renderer);
}

static inline void update_screen() {
  SDL_UpdateTexture(texture, NULL, vmem, SCREEN_W * sizeof(uint32_t));
  SDL_RenderClear(renderer);
  SDL_RenderCopy(renderer, texture, NULL, NULL);
  SDL_RenderPresent(renderer);
}

#endif

void vga_update_screen() {
  if(vgactl_port_base[1]) update_screen();
  vgactl_port_base[1]=0;
  // TODO: call `update_screen()` when the sync register is non-zero,
  // then zero out the sync register
}

void init_vga() {
  vgactl_port_base[0] = (screen_width() << 16) | screen_height();
  IFDEF(CONFIG_VGA_SHOW_SCREEN, init_screen());
  IFDEF(CONFIG_VGA_SHOW_SCREEN, memset(vmem, 0, screen_size()));
}

uint32_t get_vga_vgactl(){
  return vgactl_port_base[0];
}

void change_vga_sync(word_t data){
  vgactl_port_base[1]=data;
  return;
}


static inline uint32_t vhost_read(void *addr, int len) {
  switch (len) {
    case 1: return *(uint8_t  *)addr;
    case 2: return *(uint16_t *)addr;
    case 4: return *(uint32_t *)addr;
    default:  return 0;
  }
}
static inline void vhost_write(void *addr, int len, uint32_t data) {
  switch (len) {
    case 1: *(uint8_t  *)addr = data; return;
    case 2: *(uint16_t *)addr = data; return;
    case 4: *(uint32_t *)addr = data; return;
    default: return;
  }
}


uint8_t* guest_to_vhost(paddr_t paddr) { return vmem + paddr - FB_ADDR; }

word_t vmem_read(paddr_t addr, int len) {
  word_t ret = vhost_read(guest_to_vhost(addr), len);
  return ret;
}

void vmem_write(paddr_t addr, int len, word_t data) {
  vhost_write(guest_to_vhost(addr), len, data);
}
