#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

int printf(const char *fmt, ...) {
  char out_buffer[1024];
  va_list args;
  va_start(args,fmt);
  int len=vsprintf(out_buffer,fmt,args);
  //传入的参数是va_list类型还是 ...类型还是有区别的
  putstr(out_buffer);
  va_end(args);
  return len;
}

char* gSpaces(int glength,char g_char) { //空格生成器
  if (glength < 0) {
    return NULL;
  }
  static char spaces[128]; // 假设最大长度为 128
  for (int i=0;i<glength;i++) {
    spaces[i]=g_char;
  }
  spaces[glength]='\0'; // 字符串以 null 结尾
  return spaces;
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  *out='\0';
  char *s,c;
  int d,i;
  for (i=0;fmt[i]!='\0';i++) {
    if (fmt[i]!='%') {
      strncat(out,&fmt[i],1);
      continue;
    }
    i++;
    int width=0;
    char g_char=' ';
    if(fmt[i]=='0'){
      g_char='0';
      i++;
    }
    while (fmt[i]>='1'&&fmt[i]<='9') {
      width=width*10+(fmt[i]-'0');
      i++;
    }
    switch (fmt[i]) {
      case 's':
        s = va_arg(ap, char*);
        int s_length=strlen(s);
        if(s_length<width) strcat(out,gSpaces(width-s_length,g_char));
        strcat(out, s);
        break;
      case 'd':
        d = va_arg(ap, int);
        char tmp[30];
        itoa(d,tmp,10);
        int d_length=strlen(tmp);
        if(d_length<width) strcat(out,gSpaces(width-d_length,g_char));
        strcat(out, tmp);
        break;
      case 'c':
        c = (char)va_arg(ap, int);
        strncat(out, &c, 1);
        break;
    }
  }
  return 0;
}

int sprintf(char *out, const char *fmt, ...) { //fmt可以当个字符串处理
  va_list ap;
  va_start(ap, fmt);
  int len = vsprintf(out, fmt, ap);
  va_end(ap);
  return len;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  return 0;
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  return 0;
}

#endif
