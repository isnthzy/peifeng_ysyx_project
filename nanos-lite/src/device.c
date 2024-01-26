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
  // AM_INPUT_KEYBRD_T ev = io_read(AM_INPUT_KEYBRD);
  // if(ev.keycode == AM_KEY_NONE) return 0;
  // int ret_ken=0;
  // if(ev.keydown) ret_ken=snprintf(buf,len,"kd %s\n",keyname[ev.keycode]);
  // else ret_ken=snprintf(buf,len,"ku %s\n",keyname[ev.keycode]);
  // printf("%s",buf);
  // return ret_ken;
  return 0;
}

size_t dispinfo_read(void *buf, size_t offset, size_t len) {
  return 0;
}

size_t fb_write(const void *buf, size_t offset, size_t len) {
  return 0;
}

void init_device() {
  Log("Initializing devices...");
  ioe_init();
}
