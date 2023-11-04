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
  // panic("Not implemented");
}

void *stpcpy(char *restrict dst, const char *restrict src){
    char  *p;
    p = memcpy(dst, src, strlen(src))+strlen(src); //mempcpy
    *p = '\0';
    return p;
}
char *strcpy(char *dst, const char *src) {
  stpcpy(dst, src);
  return dst;
  // panic("Not implemented");
}

char *strncpy(char *dst, const char *src, size_t n) {
  return 0;
  // panic("Not implemented");
}

char *strcat(char *dst, const char *src) {
  stpcpy(dst + strlen(dst), src);
  return dst;
  // panic("Not implemented");
}

int strcmp(const char *s1, const char *s2) {
  size_t lens1=strlen(s1);
  size_t lens2=strlen(s2);
  size_t len=MIN(lens1,lens2);
  int retn=strncmp(s1,s2,len);
  if(retn!=0){
    if(lens1<lens2){
      return -1;
    }else{
      return 1;
    }
  }else{
    return retn;
  }
  // panic("Not implemented");
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
  // panic("Not implemented");
}

void *memset(void *s, int c, size_t n) {
  char *set=s;
  while(n--){
    *set++=c;
  }
  return s;
  // panic("Not implemented");
}

void *memmove(void *dst, const void *src, size_t n) {
  panic("Not implemented");
}

void *memcpy(void *out, const void *in, size_t n) {
  if(out==NULL||in==NULL){
    return NULL;
  }
  void *ret=out;
  size_t i=0;
  if(out<=in||(char*)out>=(char*)in+n){
    for(i=0;i<n;i++){
      *((char *)out+i)= *((char *)in+i);
    }
  }else{
    for(i=n-1;i>0;i--){
      *((char *)out+i)= *((char *)in+i);
    }
  }
  return ret;
  // panic("Not implemented");
}

int memcmp(const void *s1, const void *s2, size_t n) {
  if(s1==NULL||s2==NULL){
    // return NULL;
    assert(0);
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
  // panic("Not implemented");
}

#endif
