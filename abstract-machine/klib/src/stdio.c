#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

int printf(const char *fmt, ...) {
  return 0;
  // panic("Not implemented");
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  return 0;
  // panic("Not implemented");
}

int sprintf(char *out, const char *fmt, ...) { //fmt可以当个字符串处理
  va_list ap;
  va_start(ap,fmt);
  int i=0;
  char c,*s,*d;
  char str[200];
  for(i=0;fmt[i]!='\0';i++){
    if(fmt[i]!='%'){
      strcat(str,&fmt[i]);
      continue;
    }
    switch (*fmt++){
    case 's':
      s=va_arg(ap,char *);
      strcat(str,s);
      break;
    case 'd':
      d=va_arg(ap,char *);
      strcat(str,d);
      break;
    case 'c':
      c=(char)va_arg(ap,int);
      strcat(str,&c);
      break;
    }
  }
  va_end(ap);
  return 0;
  // panic("Not implemented");
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  return 0;
  // panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  return 0;
  // panic("Not implemented");
}

#endif
