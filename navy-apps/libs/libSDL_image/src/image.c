#define SDL_malloc  malloc
#define SDL_free    free
#define SDL_realloc realloc

#define SDL_STBIMAGE_IMPLEMENTATION
#include "SDL_stbimage.h"
#include <stdio.h>

SDL_Surface* IMG_Load_RW(SDL_RWops *src, int freesrc) {
  assert(src->type == RW_TYPE_MEM);
  assert(freesrc == 0);
  return NULL;
}

SDL_Surface* IMG_Load(const char *filename) {
  FILE *file;
  int file_size;
  char *file_buf=NULL;
  SDL_Surface* surface_buf=NULL;
  file=fopen(filename, "r+");
  if(file==NULL){
    fprintf(stderr, "IMG_Load:无法打开文件\n");
    return NULL;
  }
  fseek(file,0,SEEK_END);
  file_size=ftell(file);
  fseek(file,0,SEEK_SET);
  file_buf=malloc(file_size);
  if(fread(file_buf,file_size,1,file)!=1) assert(0);
  surface_buf=STBIMG_LoadFromMemory(file_buf,file_size);

  free(file_buf);
  fclose(file);
  return surface_buf;
}

int IMG_isPNG(SDL_RWops *src) {
  return 0;
}

SDL_Surface* IMG_LoadJPG_RW(SDL_RWops *src) {
  return IMG_Load_RW(src, 0);
}

char *IMG_GetError() {
  return "Navy does not support IMG_GetError()";
}
