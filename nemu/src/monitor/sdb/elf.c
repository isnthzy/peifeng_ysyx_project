#include <common.h>
#include <stdio.h>
#include <fcntl.h>
#include <sys/types.h>
#include <unistd.h>
#include <malloc.h>
#include <string.h>
#include <elf.h>
// 解析elf文件代码
void outputsyminfo(const Elf32_Sym *psym, const char *pbuffstr, int ncount);
void find_symtab_func(const char *pbuff);
void init_elf(const char *elf_file){
    int fd=open(elf_file, O_RDONLY);//以只读的形式打开elf_file
    if(fd==-1){
        Log("文件打开失败!\n");
        assert(0);
    }
    long int end=lseek(fd, 0, SEEK_END); //把指针移动到末尾位置,生成相应大小
    lseek(fd, 0, SEEK_SET); //把指针复原移动到头位置
    char* pbuff=malloc(end); //分配空间,从头开始
    if(!pbuff){
        Log("文件分配内存失败\n");
        assert(0);
    }
     //初始化0
    memset(pbuff, 0, end);
    if(read(fd, pbuff, end)==-1){
        Log("文件读取失败");
        assert(0);
    }
    find_symtab_func(pbuff);
}
void find_symtab_func(const char *pbuff){
    //从节区里面定位到偏移
    Elf32_Ehdr* pfilehead = (Elf32_Ehdr*)pbuff;
    Elf32_Half eshstrndx = pfilehead->e_shstrndx;
    // printf("%d",eshstrndx);
    Elf32_Shdr* psecheader = (Elf32_Shdr*)(pbuff + pfilehead->e_shoff);
    Elf32_Shdr* pshstr = (Elf32_Shdr*)(psecheader + eshstrndx);
    char* pshstrbuff = (char *)(pbuff + pshstr->sh_offset);
    for(int i = 0;i<pfilehead->e_shnum;++i)
    {
        if(!strcmp(psecheader[i].sh_name + pshstrbuff, ".symtab"))
        {
            Elf32_Sym* psym = (Elf32_Sym*)(pbuff + psecheader[i].sh_offset);
            int ncount = psecheader[i].sh_size / psecheader[i].sh_entsize;
            char* pbuffstr = (char*)((psecheader + psecheader[i].sh_link)->sh_offset + pbuff);
            printf("Symbol table '%s' contains %d entries:\r\n", psecheader[i].sh_name + pshstrbuff, ncount);
            outputsyminfo(psym, pbuffstr, ncount);
            break;
        }
    }
}
void outputsyminfo(const Elf32_Sym *psym, const char *pbuffstr, int ncount)
{
    printf("%7s  %-8s          %s  %s    %s   %s      %s  %s\r\n",
           "Num:", "Value", "Size", "Type", "Bind", "Vis", "Ndx", "Name");
    for(int i = 0;i<ncount;++i)
    {
        printf("%6d:  %016x  %-6u", i, psym[i].st_value, psym[i].st_size);
        char typelow = ELF32_ST_TYPE(psym[i].st_info);
        char bindhig = ELF32_ST_BIND(psym[i].st_info);
        switch(typelow)
        {
            case STT_NOTYPE:
                printf("%-8s", "NOTYPE");break;
            case STT_OBJECT:
                printf("%-8s", "OBJECT");break;
            case STT_FUNC:
                printf("%-8s", "FUNC");break;
            case STT_SECTION:
                printf("%-8s", "SECTION");break;
            case STT_FILE:
                printf("%-8s", "FILE");break;
            default:
                break;
        }
        switch(bindhig)
        {
            case STB_LOCAL:
                printf("%-8s", "LOCAL"); break;
            case STB_GLOBAL:
                printf("%-8s", "GLOBAL"); break;
            case STB_WEAK:
                printf("%-8s", "WEAK"); break;
            default:
                break;
        }
        printf("%-8d", psym[i].st_other);
        switch(psym[i].st_shndx)
        {
            case SHN_UNDEF:
                printf("%s  %s\r\n", "UND", psym[i].st_name + pbuffstr);break;
            case SHN_ABS:
                printf("%s  %s\r\n", "ABS", psym[i].st_name + pbuffstr);break;
            case SHN_COMMON:
                printf("%s  %s\r\n", "COM", psym[i].st_name + pbuffstr);break;
            default:
                printf("%3d  %s\r\n", psym[i].st_shndx, psym[i].st_name + pbuffstr);break;
        }
    }
}
