#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/time.h>
#include <assert.h>
#include <fcntl.h>

static int fd_dispinfo = -1;
static int fd_events = -1;
static int fd_fb = -1;

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
  int real_len=read(fd_events, buf, len);
  if(real_len>0) return 1;
  else return 0;
}

void NDL_OpenCanvas(int *w, int *h) {
  if (getenv("NWM_APP")) {
    int fbctl = 4;
    fbdev = 5;
    screen_w = *w; screen_h = *h;
    char buf[64];
    int len = sprintf(buf, "%d %d", screen_w, screen_h);
    // let NWM resize the window and create the frame buffer
    (void)!write(fbctl, buf, len); //(void)!屏蔽编译器的Wunused-result警告
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

  (void)!read(fd_dispinfo, dispinfo_buf, 64);
  sscanf(dispinfo_buf, "WIDTH :%d\nHEIGHT:%d", &screen_w, &screen_h);

  if(*w>screen_w||*h>screen_h){
    printf("画布大小超过屏幕大小\n");
    assert(0);
  }
  if(*w==0||*h==0) {
    *w=screen_w;
    *h=screen_h;
  }
  canvas_w=*w;
  canvas_h=*h;
  printf("canvas w:%d h:%d\nscreen w:%d h:%d\n",canvas_w,canvas_h,screen_w,screen_h);
}

void NDL_DrawRect(uint32_t *pixels, int x, int y, int w, int h) {
  //接口限制，len不能拆分传递，可以写成循环传递，传递h次，每次传递w个像素
  
  //因为存放pixels是uint32类型，所以可以不用*4
  //pixels的长宽为canvas的长宽
  /*系统屏幕(即frame buffer), NDL_OpenCanvas()打开的画布, 以及NDL_DrawRect()指示的绘制区域之间的位置关系.
    pixels就是已经准备好的数据，我们需要将这个数据映射到画布上，再用画布通过偏移量控制映射到屏幕相应的位置上
    所以我们采用逐行写入的方式，再借助fb_write函数，fb_write函数的职责是定位画布的位置
    并在画布起始位置通过io写入相应的数据
    所以实现居中的画布，应该在fb_write中修改相应的参数定位
  */

  /*做了pa3.5的补充，跑了native,发现前面对画布的处理是错的，重新思考做这个问题*/
  if(x==0&&y==0&&w==0&&h==0){
    w=canvas_w;
    h=canvas_h;
  }//做全屏写入处理
  size_t offset_mid=screen_w*(screen_h-h)/2+(screen_w-w)/2;
  /*在NDL_DrawRect中实现居中显示*/
  size_t offset=(y-0)*screen_w+x+offset_mid;
  lseek(fd_fb,offset*4,SEEK_SET);
  for(int i=0;i<h;i++){
    (void)!write(fd_fb,pixels+i*w,(w*4)); //api传的是void类型，长度为1，uint32长度为4
    lseek(fd_fb,(screen_w-w)*4,SEEK_CUR);
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
  fd_dispinfo=open("/proc/dispinfo",0,0);
  fd_events  =open("/dev/events",0,0);
  fd_fb      =open("/dev/fb",0,0);
  return 0;
}

void NDL_Quit() {
  close(fd_dispinfo);
  close(fd_events);
  close(fd_fb);
}
