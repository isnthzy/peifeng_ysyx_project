#ifndef _FTRACE_H__
#define _FTRACE_H__
#include <elf.h>
#include <common.h>
typedef MUXDEF(CONFIG_ISA64, Elf64_Ehdr, Elf32_Ehdr) Elf_Ehdr;
typedef MUXDEF(CONFIG_ISA64, Elf64_Half, Elf32_Half) Elf_Half;
typedef MUXDEF(CONFIG_ISA64, Elf64_Shdr, Elf32_Shdr) Elf_Shdr;
typedef MUXDEF(CONFIG_ISA64, Elf64_Off , Elf32_Off ) Elf_Off ;
typedef MUXDEF(CONFIG_ISA64, Elf64_Sym , Elf32_Sym ) Elf_Sym ;
typedef MUXDEF(CONFIG_ISA64, Elf64_Addr, Elf32_Addr) Elf_Addr;
typedef struct {
    char func_name[1024]; // 函数名
    size_t value;      // 起始地址
    size_t size;        // 函数体大小
}ELF_Func;              // [start, start+size)
void func_call(paddr_t pc,paddr_t dnpc,bool is_tail);
void func_ret(paddr_t pc);
char* find_funcname(paddr_t target);
#endif