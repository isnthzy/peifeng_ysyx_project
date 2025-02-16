#include <klib.h>
#include <klib-macros.h>
#include <stdint.h>
#define MIN(i, j) (((i) < (j)) ? (i) : (j))
#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

size_t strlen(const char *s) {
  size_t i=0;
  while(s[i]!='\0'){
    i++;
  }
  return i;
}

char *stpcpy(char *restrict dst, const char *restrict src){
    char  *p;
    p = memcpy(dst, src, strlen(src))+strlen(src); //mempcpy
    *p = '\0';
    return p;
}
char *strcpy(char *dst, const char *src) {
  stpcpy(dst, src);
  return dst;
}

char *strncpy(char *dst, const char *src, size_t n) {
  if(strlen(src)<n) n=strlen(src);
  memcpy(dst, src, n);
  // size_t i;
  // for(i=0;i<n&&src[i]!='\0';i++){
  //   dst[i]=src[i];
  //   if(src[i+1]=='\0') dst[i+1]=src[i+1];
  // }
  return dst;
}

char *strcat(char *dst, const char *src) {
  stpcpy(dst + strlen(dst), src);
  return dst;
}
char *strncat(char *dst, const char *src, size_t n){
  size_t dst_len = strlen(dst);
  size_t i;

  for (i = 0; i < n && src[i] != '\0'; i++) {
    dst[dst_len + i] = src[i];
  }
  dst[dst_len + i] = '\0';

  return dst;

}

int strcmp(const char *s1, const char *s2) {
  size_t lens1=strlen(s1);
  size_t lens2=strlen(s2);
  size_t len=MIN(lens1,lens2);
  int retn=strncmp(s1,s2,len);
  if(retn==0&&lens1!=lens2){
    if(lens1<lens2){
      return -1;
    }else{
      return 1;
    }
  }else{
    return retn;
  }
}

int strncmp(const char *s1, const char *s2, size_t n) {
  size_t i=0;
  for(i=0;i<n;i++){
    if(s1[i]>s2[i]){
      return 1;
    }else if(s1[i]==s2[i]){
      continue;
    }else if(s1[i]<s2[i]){
      return -1;
    }
  }
  return 0;
}

void *memset(void *s, int c, size_t n) {
  if(s==NULL){
    return NULL;
  }
  char *set=s;
  while(n--){
    *set++=c;
  }
  return s;
}

void *memmove(void *dst, const void *src, size_t n) {
  if(dst==NULL||src==NULL){
    return NULL;
  }
  void *ret=dst;
  size_t i=0;
  if(dst<=src||(char*)dst>=(char*)src+n){
    for(i=0;i<n;i++){
      *((char *)dst+i)= *((char *)src+i);
    }//如果目标地址小于等于源地址，或者目标地址大于等于源地址+n，那么就是正向拷贝，会覆盖src，但是无所谓
  }else{
    for(i=n-1;i>=0;i--){
      *((char *)dst+i)= *((char *)src+i);
    }//如果目标地址大于源地址并小于源地址src+n，后向拷贝，避免覆盖
  }
  return ret;
} //前向拷贝，后向拷贝策略，避免覆盖

void *memcpy(void *out, const void *in, size_t n) {
  if(out==NULL||in==NULL){
    return NULL;
  }
  void *ret=out;
  size_t i=0;
  // 这里可以直接正向拷贝，不做重叠判断
  for (i = 0; i < n; i++) {
    *((char *)out+i)= *((char *)in+i);
  }
  return ret;
  return ret;
}

int memcmp(const void *s1, const void *s2, size_t n) {
  if(s1==NULL||s2==NULL){
    return -1;
    // assert(0);
  }
  size_t i=0;
  for(i=0;i<n;i++){
    if(*((char *)s1+i)>*((char *)s2+i)){
      return 1;
    }else if(*((char *)s1+i)<*((char *)s2+i)){
      return -1;
    }else if(*((char *)s1+i)==*((char *)s2+i)){
      continue;
    }
  }
  return 0;
}

#endif
