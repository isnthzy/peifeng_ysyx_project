#include <NDL.h>
#include <stdio.h>
uint32_t sdl_begin_time=0;
int SDL_Init(uint32_t flags) {
  uint32_t sdl_begin_time=NDL_GetTicks();
  return NDL_Init(flags);
}

void SDL_Quit() {
  NDL_Quit();
}

char *SDL_GetError() {
  return "Navy does not support SDL_GetError()";
}

int SDL_SetError(const char* fmt, ...) {
  fprintf(stderr,"miniSDL_trace SDL_SetError\n");
  return -1;
}

int SDL_ShowCursor(int toggle) {
  fprintf(stderr,"miniSDL_trace SDL_ShowCursor\n");
  return 0;
}

void SDL_WM_SetCaption(const char *title, const char *icon) {
  // fprintf(stderr,"miniSDL_trace SDL_WM_SetCaption\n");
  return;
}
