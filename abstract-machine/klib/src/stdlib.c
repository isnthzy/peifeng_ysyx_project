#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#define INT_MIN -2147483648
#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)
static unsigned long int next = 1;

int rand(void) {
  // RAND_MAX assumed to be 32767
  next = next * 1103515245 + 12345;
  return (unsigned int)(next/65536) % 32768;
}

void srand(unsigned int seed) {
  next = seed;
}

int abs(int x) {
  return (x < 0 ? -x : x);
}

int atoi(const char* nptr) {
  int x = 0;
  while (*nptr == ' ') { nptr ++; }
  while (*nptr >= '0' && *nptr <= '9') {
    x = x * 10 + *nptr - '0';
    nptr ++;
  }
  return x;
}

void reverse(char *s, int len){
  char *end=s+len-1;
  char tmp;
  while (s<end) {
    tmp=*s;
    *s=*end;
    *end=tmp;
    s++;
    end--;
  }
}
char *am_itoa(int value,char * str,int radix){
  bool neg=false;
  int i=0; 
  if(value<0){
    if (value==INT_MIN) {
      // 经过测试(INT_MIN)会发生这种情况,因而特殊处理最小负数(INT_MIN)
      value++;
      str[i++]='8';  // 个位数为8
      value/=radix;
    }
  	value*=-1;
  	neg=true;
  }
  if (value==0){
    str[i++]='0';
  }
  while(value!=0){
  	int tmp=0;
    tmp=value%radix;
	  str[i++]=tmp+'0';
	  value/=radix;
  }
  if(neg) str[i++]='-';
  reverse(str,i);
  str[i]='\0';
  return str;
}

char *htoa(uint32_t value,char *str){ //十六进制转换函数
  int i=0; 
  if (value==0){
    str[i++]='0';
  }
  while(value!=0){
  	int tmp=0;
    tmp=value%16;
	  if (tmp<10) {
      str[i++]=tmp+'0';
    } else {
      str[i++]=tmp-10+'a';
    }
	  value/=16;
  }
  reverse(str,i);
  str[i]='\0';
  return str;
}

#if !(defined(__ISA_NATIVE__) && defined(__NATIVE_USE_KLIB__))
static char *hbrk;
static bool malloc_reset_flag=false;
#endif  

void *malloc(size_t size) {
  // On native, malloc() will be called during initializaion of C runtime.
  // Therefore do not call panic() here, else it will yield a dead recursion:
    // panic() -> putchar() -> (glibc) -> malloc() -> panic()
#if !(defined(__ISA_NATIVE__) && defined(__NATIVE_USE_KLIB__))
  if(!malloc_reset_flag){
    hbrk = (void *)ROUNDUP(heap.start, 8);
    malloc_reset_flag=true;
  }
  size=(size_t)ROUNDUP(size, 8);
  char *old = hbrk;
  hbrk += size;
  // assert((uintptr_t)heap.start<=(uintptr_t)hbrk&&(uintptr_t)hbrk<(uintptr_t)heap.end);
  for(uint64_t *p = (uint64_t *)old;p!=(uint64_t *)hbrk;p++) {
    *p=0;
  }
  // assert((uintptr_t)hbrk-(uintptr_t)heap.start<=0);
  return old;
#endif  
  return NULL;
}

void free(void *ptr) {
}

#endif
