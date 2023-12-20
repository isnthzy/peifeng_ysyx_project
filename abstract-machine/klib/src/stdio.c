#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

int printf(const char *fmt, ...) {
  char out_buffer[1024];
  va_list args;
  va_start(args,fmt);
  int len=sprintf(out_buffer,fmt,args);
  char *tmp = out_buffer;
  while (*tmp != 0) {
      putch(*tmp);
      tmp++;
  }
  // putstr(out_buffer);
  va_end(args);
  return len;
  // panic("Not implemented");
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  return 0;
  // panic("Not implemented");
}

int sprintf(char *out, const char *fmt, ...) { //fmt可以当个字符串处理
  va_list ap;
  va_start(ap, fmt);
//   printf("%s\n",out);
  *out='\0';
  char *s,c;
  int d,i;
  for (i=0;fmt[i]!='\0';i++) {
    if (fmt[i]!='%') {
      strncat(out,&fmt[i],1);
      continue;
    }
    i++;
    switch(fmt[i]){
    case 's':
	    s=va_arg(ap,char*);
	    strcat(out,s);
	    break;
    case 'd':
      d=va_arg(ap,int);
      char tmp[30];
      itoa(d,tmp,10);
      strcat(out,tmp);
      break;
    case 'c':
      c=(char)va_arg(ap,int);
      strncat(out,&c,1);
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
