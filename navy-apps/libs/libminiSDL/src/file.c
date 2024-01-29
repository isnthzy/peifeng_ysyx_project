#include <sdl-file.h>
#include <stdio.h>
SDL_RWops* SDL_RWFromFile(const char *filename, const char *mode) {
  fprintf(stderr,"miniSDL_trace SDL_RWFromFile\n");
  return NULL;
}

SDL_RWops* SDL_RWFromMem(void *mem, int size) {
  fprintf(stderr,"miniSDL_trace SDL_RWFromMem\n");
  return NULL;
}
