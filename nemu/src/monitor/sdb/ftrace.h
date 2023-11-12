#ifndef _FTRACE_H__
#define _FTRACE_H__
#include <common.h>
typedef struct {
    char func_name[64]; // 函数名
    size_t value;      // 起始地址
    size_t size;        // 函数体大小
}ELF_Func;              // [start, start+size)
void func_call(paddr_t pc,paddr_t dnpc);
void func_ret(paddr_t pc,paddr_t dnpc);
char* find_funcname(paddr_t target);
#endif