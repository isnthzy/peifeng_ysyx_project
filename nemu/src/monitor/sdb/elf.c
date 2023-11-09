#include <common.h>
#include <stdio.h>
// 解析elf文件代码

void init_elf(const char *elf_file){
    FILE* file = fopen(elf_file, "rb");//以只读的形式打开elf_file
    if(!file){
        Log("文件打开失败!\n");
        assert(0);
    }
    size_t resultt;
    
    // 读取 ELF 文件的头部信息
    Elf_Ehdr elf_header;
    resultt=fread(&elf_header, sizeof(Elf_Ehdr), 1, file);
    if (resultt!= 0) puts("");
    // 获取节头表的偏移量和条目数量
    Elf_Off section_header_offset = elf_header.e_shoff;
    Elf_Half section_header_entry_count = elf_header.e_shnum;
    // 定位到节头表
    fseek(file, section_header_offset, SEEK_SET);
    // 读取节头表
    Elf_Shdr section_headers[section_header_entry_count];
    resultt=fread(section_headers, sizeof(Elf64_Shdr), section_header_entry_count, file);
    // 定位到字符串表节
    Elf_Shdr string_table_header = section_headers[elf_header.e_shstrndx];
    fseek(file, string_table_header.sh_offset, SEEK_SET);
    // 读取字符串表内容
    char string_table[string_table_header.sh_size];
    resultt=fread(string_table, string_table_header.sh_size, 1, file);
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
    resultt=fread(symbols, sizeof(Elf64_Sym), symbol_count, file);
    // 遍历符号表，筛选出类型为FUNC的符号
    for (size_t i = 0; i < symbol_count; ++i) {
        if (ELF32_ST_TYPE(symbols[i].st_info) == STT_FUNC) {
            // 获取符号的名称
            char* symbol_name = string_table + symbols[i].st_name;
            // 获取符号的地址
            Elf64_Addr symbol_address = symbols[i].st_value;
            printf("Function: %s\nAddress: 0x%lx\n\n", symbol_name, symbol_address);
        }
    }
    fclose(file);
}

