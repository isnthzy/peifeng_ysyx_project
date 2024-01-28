#include <NDL.h>
#include <sdl-timer.h>
#include <stdio.h>
extern uint32_t sdl_begin_time;
SDL_TimerID SDL_AddTimer(uint32_t interval, SDL_NewTimerCallback callback, void *param) {
  return NULL;
}

int SDL_RemoveTimer(SDL_TimerID id) {
  return 1;
}

uint32_t SDL_GetTicks() {
  //SDL_init的时候要调用一次SDL_GetTicks用于实现计算初始化到调用SDL_GetTicks的运行时间
  return NDL_GetTicks()-sdl_begin_time;
}

void SDL_Delay(uint32_t ms) {
}
