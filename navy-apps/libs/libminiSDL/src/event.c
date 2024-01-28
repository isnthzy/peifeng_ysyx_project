#include <NDL.h>
#include <SDL.h>
#include <string.h>

#define keyname(k) #k,

static const char *keyname[] = {
  "NONE",
  _KEYS(keyname)
};

int SDL_PushEvent(SDL_Event *ev) {
  return 0;
}

int get_SDL_keynum(const char *key_name){
  for(int i=0;i<sizeof(keyname)/sizeof(char*);i++){
    if(!strcmp(key_name,keyname[i])) return i;
  }
}
int SDL_PollEvent(SDL_Event *ev) {
  char event_buf[64];
  int NDL_PollEvent_ret=NDL_PollEvent(event_buf,sizeof(event_buf));
  //返回值为1有效，0无效
  char key_name[60];
  char key_command[4];
  if(NDL_PollEvent_ret){
    sscanf(event_buf, "%s %s\n", key_command, key_name);
    if(!strcmp(key_command,"kd")) ev->type=SDL_KEYDOWN;
    else if(!strcmp(key_command,"ku")) ev->type=SDL_KEYUP;

    ev->key.keysym.sym=get_SDL_keynum(key_name);
    // printf("%d %d",ev->type,ev->key.keysym.sym);
    return 1;
  }
  return 0;
}

int SDL_WaitEvent(SDL_Event *event) {
  char event_buf[64];
  while(NDL_PollEvent(event_buf,sizeof(event_buf)));
  //返回值为1有效，0无效
  char key_name[60];
  char key_command[4];
  sscanf(event_buf, "%s %s\n", key_command, key_name);
  if(!strcmp(key_command,"kd")) event->type=SDL_KEYDOWN;
  else if(!strcmp(key_command,"ku")) event->type=SDL_KEYUP;
  // else printf("触发了未准备的事件");

  event->key.keysym.sym=get_SDL_keynum(key_name);
  return 1;
}

int SDL_PeepEvents(SDL_Event *ev, int numevents, int action, uint32_t mask) {
  return 0;
}

uint8_t* SDL_GetKeyState(int *numkeys) {
  return NULL;
}
