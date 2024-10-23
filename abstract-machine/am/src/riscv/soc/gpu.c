#include <am.h>
#include <riscv/riscv.h>
// #define DEVICE_BASE  0xa0000000
// #define VGACTL_ADDR  (DEVICE_BASE + 0x0000100)
// #define SYNC_ADDR    (VGACTL_ADDR + 4)
#define FB_ADDR      0x21000000L

void __am_gpu_init() {
  // int i;
  // int w = io_read(AM_GPU_CONFIG).width;  // TODO: get the correct width
  // int h = io_read(AM_GPU_CONFIG).height;  // TODO: get the correct height
  // uint32_t *fb = (uint32_t *)(uintptr_t)FB_ADDR;
  // for (i = 0; i < w * h; i ++) fb[i] = i;
  // outl(SYNC_ADDR, 1);
}

void __am_gpu_config(AM_GPU_CONFIG_T *cfg) {
  // uint32_t tmp_gpu_config=inl(VGACTL_ADDR);
  // int tmp_config_low =(int)(tmp_gpu_config&0xFFFF);
  // int tmp_config_high=(int)((tmp_gpu_config>>16)&0xFFFF);
  *cfg = (AM_GPU_CONFIG_T) {
    .present = true, .has_accel = false,
    .width = 480, .height = 640,
    .vmemsz = 0
  };
}

void __am_gpu_fbdraw(AM_GPU_FBDRAW_T *ctl) {
  if (!ctl->sync&&(ctl->h==0||ctl->w==0)) return;
  //如果刷新内容为空，直接返回

  uint32_t *fb =(uint32_t *)(uintptr_t)FB_ADDR;
  uint32_t vga_begin=0;
  //原理就是行优先存储，那么写入也是从行写
  //简单来说是二维数据一维转储的过程
  /*
    如果说从写入地方的角度，每次刷新要刷新的地方就行了
    那么位置就是
	假设屏幕: 为w:9 * h:5 横向为x轴，纵向为y轴从左往右从上到下
	*********
	*********
	*********
	*********
	*********
	那么想往屏幕坐标 [4,3]开始更新宽度为x+w,y+h个量
	就要向vga_begin(屏幕的宽度)*3+4 这样实际上就是把二维数据用一维数组的方法进行写入
	那么这时候我们只需要把需要取出来的数据放进要放的位置就好了
  */
  uint32_t *pixels=ctl->pixels;
  for(int y=ctl->y;y<ctl->y+ctl->h;y++){
    for(int x=ctl->x;x<ctl->x+ctl->w;x++){
      fb[vga_begin*y+x]=pixels[(y-ctl->y)*ctl->w+(x-ctl->x)];
    }
  }
  // if (ctl->sync) {
  //   outl(SYNC_ADDR, 1);
  // }
}

void __am_gpu_status(AM_GPU_STATUS_T *status) {
  status->ready = true;
}
