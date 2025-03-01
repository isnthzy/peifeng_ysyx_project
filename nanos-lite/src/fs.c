#include <fs.h>
size_t ramdisk_read(void *buf, size_t offset, size_t len);
size_t ramdisk_write(const void *buf, size_t offset, size_t len);

size_t serial_write(const void *buf, size_t offset, size_t len);
size_t events_read(void *buf, size_t offset, size_t len);
size_t dispinfo_read(void *buf, size_t offset, size_t len);
size_t fb_write(const void *buf, size_t offset, size_t len);
typedef size_t (*ReadFn) (void *buf, size_t offset, size_t len);
typedef size_t (*WriteFn) (const void *buf, size_t offset, size_t len);

typedef struct {
  char *name;
  size_t size;
  size_t disk_offset;
  size_t open_offset;
  ReadFn read;
  WriteFn write;
} Finfo;

enum {FD_STDIN, FD_STDOUT, FD_STDERR, FD_FB, FD_EVENTS, FD_DISPINFO};

size_t invalid_read(void *buf, size_t offset, size_t len) {
  panic("should not reach here");
  return 0;
}

size_t invalid_write(const void *buf, size_t offset, size_t len) {
  panic("should not reach here");
  return 0;
}

/* This is the information about all files in disk. */
static Finfo file_table[] __attribute__((used)) = {
  [FD_STDIN]  = {"stdin", 0, 0, 0,invalid_read, invalid_write},
  [FD_STDOUT] = {"stdout", 0, 0, 0,invalid_read, serial_write},
  [FD_STDERR] = {"stderr", 0, 0, 0,invalid_read, serial_write},
  [FD_FB]     = {"/dev/fb", 0, 0, 0,invalid_read, fb_write},
  [FD_EVENTS] = {"/dev/events", 0, 0, 0,events_read, invalid_write},
  [FD_DISPINFO]={"/proc/dispinfo", 0, 0, 0,dispinfo_read, invalid_write},
#include "files.h"
};
int file_nums=sizeof(file_table)/sizeof(file_table[0]);
int fs_open(const char *pathname, int flags, int mode){
  for(int i=0;i<file_nums;i++){
    if(strcmp(pathname,file_table[i].name)==0){
      return i;
    }
  }
  panic("file not found");
}
size_t fs_read(int fd, void *buf, size_t len){
  if(fd==FD_STDIN){
    file_table[fd].read(buf,0,len);
    //open_offset不需要更新
  }else if(fd==FD_EVENTS){
    len=file_table[fd].read(buf,0,len);
    //open_offset不需要更新
  }else if(fd==FD_DISPINFO){
    len=file_table[fd].read(buf,0,len);
    //open_offset不需要更新
  }else{
    if(file_table[fd].open_offset>=file_table[fd].size) return 0;
    if(file_table[fd].open_offset+len>=file_table[fd].size){
      size_t realsize=file_table[fd].size-file_table[fd].open_offset;
      ramdisk_read(buf,file_table[fd].disk_offset+file_table[fd].open_offset,realsize);
      file_table[fd].open_offset+=realsize;
      return realsize;
    }
    ramdisk_read (buf,file_table[fd].disk_offset+file_table[fd].open_offset,len);
    file_table[fd].open_offset+=len;
  }
  return len;
}
int fs_close(int fd){
  file_table[fd].open_offset=0;
  //被这个坑了，笑死，之前居然忘了给偏移量清0导致再打开这个文件的时候读取出现问题
  return 0;
}
size_t fs_write(int fd, const void *buf, size_t len){
  if(fd==FD_STDOUT||fd==FD_STDERR){
    file_table[fd].write(buf,0,len);
    //open_offset不需要更新
  }else if(fd==FD_FB){
    file_table[fd].write(buf,file_table[fd].open_offset,len);
    file_table[fd].open_offset+=len;
  }else{
    if(file_table[fd].open_offset+len>=file_table[fd].size){
      size_t realsize=file_table[fd].size-file_table[fd].open_offset;
      ramdisk_write(buf,file_table[fd].disk_offset+file_table[fd].open_offset,realsize);
      file_table[fd].open_offset+=realsize;
      return realsize;
    }
    ramdisk_write(buf,file_table[fd].disk_offset+file_table[fd].open_offset,len);
    file_table[fd].open_offset+=len;
  }
  return len;
}
size_t fs_lseek(int fd, size_t offset, int whence){
  switch (whence)
  {
  case SEEK_SET:
    file_table[fd].open_offset=0+offset;
    break;
  case SEEK_CUR:
    file_table[fd].open_offset+=offset;
    break;
  case SEEK_END:
    file_table[fd].open_offset=file_table[fd].size+offset;
    break;
  default:
    panic("Invalid whence");
    break;
  }
  // printf("offset:%d\n",file_table[fd].open_offset);
  return file_table[fd].open_offset;
}


void init_fs() {
  // fb_buf=malloc

  // int num_files = sizeof(file_table) / sizeof(file_table[0]);

  // for (int i = 0; i < num_files; i++) {
  //   printf("File %d:\n", i);
  //   printf("  Name: %s\n", file_table[i].name);
  //   printf("  Size: %d\n", file_table[i].size);
  //   printf("  Disk Offset: %x\n", file_table[i].disk_offset);
  //   printf("\n");
  // }
  // TODO: initialize the size of /dev/fb
}
