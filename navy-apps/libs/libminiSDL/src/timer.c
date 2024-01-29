#include <NDL.h>
#include <sdl-timer.h>
#include <stdio.h>
extern uint32_t sdl_begin_time;
SDL_TimerID SDL_AddTimer(uint32_t interval, SDL_NewTimerCallback callback, void *param) {
  fprintf(stderr,"miniSDL_trace SDL_AddTimer\n");
  return NULL;
}

int SDL_RemoveTimer(SDL_TimerID id) {
  fprintf(stderr,"miniSDL_trace SDL_RemoveTimer\n");
  return 1;
}

uint32_t SDL_GetTicks() {
  //SDL_init的时候要调用一次SDL_GetTicks用于实现计算初始化到调用SDL_GetTicks的运行时间
  return NDL_GetTicks()-sdl_begin_time;
}

void SDL_Delay(uint32_t ms) {
  // fprintf(stderr,"miniSDL_trace SDL_Delay\n");
  uint32_t now_ms=SDL_GetTicks();
  while (SDL_GetTicks()-now_ms<ms);
  return;
}
