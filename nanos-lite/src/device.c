#include <common.h>

#if defined(MULTIPROGRAM) && !defined(TIME_SHARING)
# define MULTIPROGRAM_YIELD() yield()
#else
# define MULTIPROGRAM_YIELD()
#endif

#define NAME(key) \
  [AM_KEY_##key] = #key,

static const char *keyname[256] __attribute__((used)) = {
  [AM_KEY_NONE] = "NONE",
  AM_KEYS(NAME)
};

size_t serial_write(const void *buf, size_t offset, size_t len) {
  char *write_buf=(char *)buf;
  for(size_t i=0;i<len;i++){
    putch(write_buf[i]);
  } 
  return len;
}

size_t events_read(void *buf, size_t offset, size_t len) {
  AM_INPUT_KEYBRD_T ev = io_read(AM_INPUT_KEYBRD);
  if(ev.keycode == AM_KEY_NONE) return 0;
  int ret_ken=0;
  if(ev.keydown) ret_ken=snprintf(buf,len,"kd %s\n",keyname[ev.keycode]);
  else ret_ken=snprintf(buf,len,"ku %s\n",keyname[ev.keycode]);
  // printf("%s",buf);
  return ret_ken;
}


static size_t screen_w = 0, screen_h = 0;
size_t dispinfo_read(void *buf, size_t offset, size_t len) {
  AM_GPU_CONFIG_T gpu_cfg=io_read(AM_GPU_CONFIG);
  snprintf(buf,len,"WIDTH :%d\nHEIGHT:%d",gpu_cfg.width,gpu_cfg.height);
  screen_w = gpu_cfg.width;
  screen_h = gpu_cfg.height;
  return 0;
}


size_t fb_write(const void *buf, size_t offset, size_t len) {
  size_t begin_x=offset%screen_w;
  size_t begin_y=offset/screen_w;
  uint32_t *color_buf=(uint32_t *)(buf+offset*4);
  printf("%d %d %d %d %d\n",begin_x,begin_y,color_buf,len);
  io_write(AM_GPU_FBDRAW,begin_x,begin_y,color_buf,len,1,true);
  //感觉问题发生在每次的color_buf都是从头开始，从而导致写入的图像全是空白
  return 0;
}

void init_device() {
  Log("Initializing devices...");
  ioe_init();
}
