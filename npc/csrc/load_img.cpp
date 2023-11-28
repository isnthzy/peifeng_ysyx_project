#include <npc_common.h>
static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN = {};
static const uint32_t defaultImg [] = {
  0xC0010093,
  0x40008213,
  0x40008213,
  0x40008213,
  0x40008213,
  0x40008213,
  0x40008213,
  0x40008213,
  0x40008213,
  0x00100073,
};

static char *img_file = NULL;
static inline uint32_t host_read(void *addr, int len) {
  switch (len) {
    case 1: return *(uint8_t  *)addr;
    case 2: return *(uint16_t *)addr;
    case 4: return *(uint32_t *)addr;
    default:  return 0;
  }
}
static inline void host_write(void *addr, int len, uint32_t data) {
  switch (len) {
    case 1: *(uint8_t  *)addr = data; return;
    case 2: *(uint16_t *)addr = data; return;
    case 4: *(uint32_t *)addr = data; return;
    default: return;
  }
}
uint8_t* guest_to_host(uint32_t paddr) { return pmem + paddr - CONFIG_MBASE; }
uint32_t host_to_guest(uint8_t *haddr) { return haddr - pmem + CONFIG_MBASE; }


uint32_t pmem_read(uint32_t addr, int len) {
  uint32_t ret = host_read(guest_to_host(addr), len);
  printf("pc: %x,inst: %x\n",addr,ret);
  return ret;
}

void pmem_write(uint32_t addr, int len, uint32_t data) {
  host_write(guest_to_host(addr), len, data);
}

long load_img(char *img_file) {
  if (img_file == NULL) {
    printf("No image is given. Use the default build-in image.\n");
    return 4096; // built-in image size
  }

  FILE *fp = fopen(img_file, "rb");
  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);
  printf("The image is %s, size = %ld\n", img_file, size);
  fseek(fp, 0, SEEK_SET);
  int ret = fread(guest_to_host(START_ADDR), size, 1, fp);
  assert(ret == 1);
  fclose(fp);
  return size;
}

void init_img(int argc, char *img_file){
  if(argc<2){
    printf("\033[0m\033[1;31m img=NULL -> use init_img \033[0m\n");
    memcpy(guest_to_host(START_ADDR),init_img, sizeof(defaultImg));
  }else load_img(img_file);
}
