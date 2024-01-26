#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/time.h>
#define FD_FB 3
#define FD_EVENTS 4
#define FD_DISPINFO 5
static int evtdev = -1;
static int fbdev = -1;
static int screen_w = 0, screen_h = 0;
static int canvas_w = 0, canvas_h = 0;  //画布的宽和高
static uint32_t get_time_ms() {
  struct timeval now;
  gettimeofday(&now, NULL);
  uint32_t ms = now.tv_sec*1000 + now.tv_usec/1000;
  return ms;
}
uint32_t NDL_GetTicks() {
  return get_time_ms();
}

int NDL_PollEvent(char *buf, int len) {
  int real_len=read(FD_EVENTS, buf, len);
  if(real_len>0) return 1;
  else return 0;
  return 0;
}

void NDL_OpenCanvas(int *w, int *h) {
  if (getenv("NWM_APP")) {
    int fbctl = 4;
    fbdev = 5;
    screen_w = *w; screen_h = *h;
    char buf[64];
    int len = sprintf(buf, "%d %d", screen_w, screen_h);
    // let NWM resize the window and create the frame buffer
    write(fbctl, buf, len);
    while (1) {
      // 3 = evtdev
      int nread = read(3, buf, sizeof(buf) - 1);
      if (nread <= 0) continue;
      buf[nread] = '\0';
      if (strcmp(buf, "mmap ok") == 0) break;
    }
    close(fbctl);
  }
  char dispinfo_buf[64];
  read(FD_DISPINFO, dispinfo_buf, 64);
  sscanf(dispinfo_buf, "WIDTH :%d\nHEIGHT:%d", &screen_w, &screen_h);
  if(*w>screen_w||*h>screen_h)  printf("画布大小超过屏幕大小\n");
  if(*w==0||*h==0) {
    *w=screen_w;
    *h=screen_h;
  }
  canvas_w=*w;
  canvas_h=*h;
  printf("%d %d\n%d %d\n",canvas_w,canvas_h,screen_w,screen_h);
}

void NDL_DrawRect(uint32_t *pixels, int x, int y, int w, int h) {
  //接口限制，len不能拆分传递，可以写成循环传递，传递h次，每次传递w个像素

  // int len=w*h;
  size_t offset=(y-0)*screen_w+x;
  //因为存放pixels是uint32类型，所以可以不用*4
  lseek(FD_FB,offset,SEEK_SET);
  for(int i=0;i<h;i++){
    write(FD_FB,pixels,w);
    if(i!=h-1) lseek(FD_FB,screen_w-w,SEEK_CUR);
  }

}

void NDL_OpenAudio(int freq, int channels, int samples) {
}

void NDL_CloseAudio() {
}

int NDL_PlayAudio(void *buf, int len) {
  return 0;
}

int NDL_QueryAudio() {
  return 0;
}

int NDL_Init(uint32_t flags) {
  if (getenv("NWM_APP")) {
    evtdev = 3;
  }
  return 0;
}

void NDL_Quit() {
}
