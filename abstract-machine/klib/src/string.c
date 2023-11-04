#include <klib.h>
#include <klib-macros.h>
#include <stdint.h>
#define MIN(i, j) (((i) < (j)) ? (i) : (j))
#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

size_t strlen(const char *s) {
  panic("Not implemented");
}

char *strcpy(char *dst, const char *src) {
  panic("Not implemented");
}

char *strncpy(char *dst, const char *src, size_t n) {
  panic("Not implemented");
}

char *strcat(char *dst, const char *src) {
  panic("Not implemented");
}

int strcmp(const char *s1, const char *s2) {
  // int i=0;
  // int lens1=strlen(s1);
  // int lens2=strlen(s2);
  // int len=MIN(lens1,lens2);
  // for(int i=0;i<len;i++){
  //   if(s1[i]>s2[i]){
  //     return 1;
  //   }else if(s1[i]==s2[i]){
  //     if(i==lens1-1&&lens1<lens2){
  //       return -1;
  //     }else if(i==lens1-1&&lens1>lens2){
  //       return 1;
  //     }
  //     continue;
  //   }else if(s1[i]<s2[i]){
  //     return -1;
  //   }
  // }
  // return 0;
  panic("Not implemented");
}

int strncmp(const char *s1, const char *s2, size_t n) {
  panic("Not implemented");
}

void *memset(void *s, int c, size_t n) {
  panic("Not implemented");
}

void *memmove(void *dst, const void *src, size_t n) {
  panic("Not implemented");
}

void *memcpy(void *out, const void *in, size_t n) {
  panic("Not implemented");
}

int memcmp(const void *s1, const void *s2, size_t n) {
  panic("Not implemented");
}

#endif
