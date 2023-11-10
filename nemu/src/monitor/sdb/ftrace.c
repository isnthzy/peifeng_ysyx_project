
#include <stdio.h>
#include "ftrace.h"

int func_depth=0;
char func_name[64];
char n_spaces[32];
char put_ftrace[128];
ELF_Func elf_func[1024]; 
int func_cnt=0;
// 解析elf文件代码
void init_elf(const char *elf_file){
    if(elf_file==NULL){Log("Ftrace: OFF");
        return ;
    }
    else Log("Ftrace: ON");
    FILE* file = fopen(elf_file, "rb");//以只读的形式打开elf_file
    if(!file){
        Log("文件打开失败!\n");
        assert(0);
    }
    size_t result;
    
    // 读取 ELF 文件的头部信息
    Elf_Ehdr elf_header;
    result=fread(&elf_header, sizeof(Elf_Ehdr), 1, file);
    if (result== 0) assert(0);
    // 获取节头表的偏移量和条目数量
    Elf_Off section_header_offset = elf_header.e_shoff;
    Elf_Half section_header_entry_count = elf_header.e_shnum;
    // 定位到节头表
    fseek(file, section_header_offset, SEEK_SET);
    // 读取节头表
    Elf_Shdr section_headers[section_header_entry_count];
    result=fread(section_headers, sizeof(Elf64_Shdr), section_header_entry_count, file);
    // 定位到字符串表节
    Elf_Shdr string_table_header = section_headers[elf_header.e_shstrndx];
    fseek(file, string_table_header.sh_offset, SEEK_SET);
    // 读取字符串表内容
    char string_table[string_table_header.sh_size];
    result=fread(string_table, string_table_header.sh_size, 1, file);
    // 查找符号表节和字符串表节
    Elf_Shdr symtab_header;
    for (int i = 0; i < section_header_entry_count; ++i) {
        if (section_headers[i].sh_type == SHT_SYMTAB) {
            symtab_header = section_headers[i];
            break;
        }
    }
    // 定位到符号表节
    fseek(file, symtab_header.sh_offset, SEEK_SET);
    // 计算符号表中的符号数量
    size_t symbol_count = symtab_header.sh_size / symtab_header.sh_entsize;
    // 读取符号表
    Elf_Sym symbols[symbol_count];
    result=fread(symbols, sizeof(Elf64_Sym), symbol_count, file);
    // 遍历符号表，筛选出类型为FUNC的符号
    for (size_t i = 0; i < symbol_count; ++i) {
        if (ELF32_ST_TYPE(symbols[i].st_info) == STT_FUNC) {
            if(symbols[i].st_size==0) continue; //不符合的大小直接略过
            // 获取符号的名称
            char* symbol_name=string_table + symbols[i].st_name;
            strcpy(elf_func[i].func_name,symbol_name);
            // 获取符号的地址
            elf_func[i].value=symbols[i].st_value;
            elf_func[i].size =symbols[i].st_size;
            func_cnt++;
            printf("Function: %s\nAddress: 0x%x %d(Dec) %x(Hec)\n",elf_func[i].func_name,symbols[i].st_value,symbols[i].st_size,symbols[i].st_size);
        }
    }
    fclose(file);
}

/*ftrace追踪内容*/
void generateSpaces(int length, char* spaces) {
    spaces[0] = '\0'; // 确保初始为空字符串
    char space[] = " "; // 单个空格字符
    while (length>0) {
        strncat(spaces, space, sizeof(space) - 1);
        length--;
    }
}
void func_call(paddr_t pc,paddr_t dnpc){
    func_depth++;
    strcmp(func_name,find_funcname(dnpc));
    generateSpaces(func_depth,n_spaces);
    Log("0x%x:%s call[%s@0x%x]",pc,n_spaces,func_name,dnpc);
}
void func_ret(paddr_t pc,paddr_t dnpc){
    func_depth--;
    strcmp(func_name,find_funcname(dnpc));
    generateSpaces(func_depth,n_spaces);
    Log(" 0x%x:%s ret [%s@0x%x]",pc,n_spaces,func_name,dnpc);
}

char* find_funcname(paddr_t pc){
    for(int i=0;i<func_cnt;i++){
        if(elf_func[i].value<=pc&&(pc<elf_func[i].value+elf_func[i].size)){
            return elf_func[i].func_name;
        }
    }
    return "???";
}
