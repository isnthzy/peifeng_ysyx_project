
#include <stdio.h>
#include <string.h>
#include "ftrace.h"

int func_depth=0;
char n_spaces[128];
char put_ftrace[128];
ELF_Func elf_func[1024]; 
int func_cnt=0;
extern bool ftrace_flag;
typedef struct tail_rec_node {
	paddr_t pc;
	int depth;
	struct tail_rec_node *next;
} TailRecNode;
TailRecNode *tail_rec_head = NULL; 
static void init_tail_rec_list();
// 解析elf文件代码
void init_elf(const char *elf_file){
    init_tail_rec_list(); //初始化尾调用处理数据结构
    if(ftrace_flag==false){Log("Ftrace: OFF");
        return ;
    }
    else Log("Ftrace: ON");
   FILE* file = fopen(elf_file, "rb");
    if (!file) {
        printf("Failed to open file: %s\n", elf_file);
        return ;
    }

    Elf32_Ehdr elf_header;
    size_t result;
    result=fread(&elf_header, sizeof(Elf32_Ehdr), 1, file);
    if(result==0) assert(0);
    if (memcmp(elf_header.e_ident, ELFMAG, SELFMAG) != 0) {
        printf("Invalid ELF file: %s\n", elf_file);
        fclose(file);
        return ;
    }

    fseek(file, elf_header.e_shoff, SEEK_SET);
    Elf32_Shdr* section_headers = (Elf32_Shdr*)malloc(elf_header.e_shentsize * elf_header.e_shnum);
    result=fread(section_headers, elf_header.e_shentsize, elf_header.e_shnum, file);

    Elf32_Shdr* string_table_header = &section_headers[elf_header.e_shstrndx];
    char* string_table = (char*)malloc(string_table_header->sh_size);
    fseek(file, string_table_header->sh_offset, SEEK_SET);
    result=fread(string_table, string_table_header->sh_size, 1, file);

    Elf32_Shdr* symbol_table_header = NULL;
    Elf32_Shdr* text_section_header = NULL;

    for (int i = 0; i < elf_header.e_shnum; ++i) {
        if (section_headers[i].sh_type == SHT_SYMTAB) {
            symbol_table_header = &section_headers[i];
            break;
        }
    }

    if (!symbol_table_header) {
        printf("No symbol table found in the ELF file.\n");
        free(section_headers);
        free(string_table);
        fclose(file);
        return ;
    }

    for (int i = 0; i < elf_header.e_shnum; ++i) {
        if (section_headers[i].sh_addr == symbol_table_header->sh_link) {
            text_section_header = &section_headers[i];
            break;
        }
    }

    if (!text_section_header) {
        printf("No associated text section found for the symbol table.\n");
        free(section_headers);
        free(string_table);
        fclose(file);
        return ;
    }

    Elf32_Sym* symbols = (Elf32_Sym*)malloc(symbol_table_header->sh_size);
    fseek(file, symbol_table_header->sh_offset, SEEK_SET);
    result=fread(symbols, symbol_table_header->sh_size, 1, file);

    int symbol_count = symbol_table_header->sh_size / sizeof(Elf32_Sym);

    for (int i = 0; i < symbol_count; ++i) {
        Elf32_Sym* symbol = &symbols[i];
        if (ELF32_ST_TYPE(symbol->st_info) == STT_FUNC) {
            const char* function_name = string_table + symbol->st_name;
            strcpy(elf_func[func_cnt].func_name,function_name);
            // 获取符号的地址
            elf_func[func_cnt].value=symbol->st_value;
            elf_func[func_cnt].size =symbol->st_size;
            printf("Function: %s\nAddress: 0x%lx %ld(Dec) %lx(Hec)\n",elf_func[func_cnt].func_name,elf_func[func_cnt].value,elf_func[func_cnt].size,elf_func[func_cnt].size);
            func_cnt++; //func_cnt用于只筛出来符合要求的函数
        }
    }

    free(section_headers);
    free(string_table);
    free(symbols);
    fclose(file);

    return ;
}

/*ftrace追踪内容*/
static void init_tail_rec_list() {
	tail_rec_head = (TailRecNode *)malloc(sizeof(TailRecNode));
	tail_rec_head->pc  = 0;
	tail_rec_head->next = NULL;
}

static void insert_tail_rec(paddr_t pc,paddr_t dnpc,int depth) {
	TailRecNode *node = (TailRecNode *)malloc(sizeof(TailRecNode));
	node->pc  = pc;
	node->depth = depth;
	node->next = tail_rec_head->next;
	tail_rec_head->next = node;
}

static void remove_tail_rec() {
	TailRecNode *node = tail_rec_head->next;
	tail_rec_head->next = node->next;
	free(node);
}

void generateSpaces(int length, char* spaces) {
    spaces[0] = '\0'; // 确保初始为空字符串
    char space[] = " "; // 单个空格字符
    while (length>0) {
        strncat(spaces, space, sizeof(space) - 1);
        length--;
    }
}
void func_call(paddr_t pc,paddr_t dnpc,bool is_tail){
    func_depth++;
    if(func_depth<=1) return; //忽略trm_init
    generateSpaces(func_depth,n_spaces);
    printf("0x%x:%s call[%s->%s@0x%x]\n",pc,n_spaces,find_funcname(pc),find_funcname(dnpc),dnpc);
    if (is_tail) {
		insert_tail_rec(pc,dnpc,func_depth-1);
	}
    return;
}
void func_ret(paddr_t pc){
    generateSpaces(func_depth,n_spaces);
    printf("0x%x:%s ret [%s]\n",pc,n_spaces,find_funcname(pc));
    func_depth--;
    TailRecNode *node = tail_rec_head->next;
	if (node != NULL) {
		if (node->depth == func_depth) {
			paddr_t ret_end  = node->pc;
			remove_tail_rec();
			func_ret(ret_end);
		}
	}
    return;
}

char* find_funcname(paddr_t pc){
    for(int i=0;i<func_cnt;i++){
        if(elf_func[i].value<=pc&&pc<(elf_func[i].value+elf_func[i].size)){
            return elf_func[i].func_name;
        }
    }
    return "???";
}
