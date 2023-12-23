
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
    result=fread(section_headers, sizeof(Elf_Shdr), section_header_entry_count, file);
    // 定位到字符串表节
    Elf_Shdr string_table_header = section_headers[elf_header.e_shstrndx];
    fseek(file, string_table_header.sh_offset, SEEK_SET);
    // 读取字符串表内容
    char string_table[string_table_header.sh_size];
    result=fread(string_table, string_table_header.sh_size, 1, file);
    // 查找符号表节和字符串表节
    Elf_Shdr symtab_header={};
    for (int i = 0; i < section_header_entry_count; ++i) {
        if (section_headers[i].sh_type == SHT_SYMTAB) {
            symtab_header = section_headers[i];
        }
    }
    // 定位到符号表节
    fseek(file, symtab_header.sh_offset, SEEK_SET);
    // 计算符号表中的符号数量
    size_t symbol_count = symtab_header.sh_size / symtab_header.sh_entsize;
    // 读取符号表
    // Elf_Sym symbols[symbol_count];
    // result=fread(symbols, sizeof(Elf32_Sym), symbol_count, file);

    Elf32_Sym symbol;
    for (size_t i = 0; i < symbol_count; ++i) {
        if (fread(&symbol, sizeof(Elf32_Sym), 1,file) <= 0 ) {
            fclose(file);
            exit(EXIT_FAILURE);
        }
        // 判断符号是否为函数，并且函数的大小不为零
        if (ELF64_ST_TYPE(symbol.st_info) == STT_FUNC && symbol.st_size != 0) {
            if(symbol.st_size==0) continue; //不符合的大小直接略过
            // 获取符号的名称
            char* symbol_name=string_table + symbol.st_name;
            strcpy(elf_func[func_cnt].func_name,symbol_name);
            // 获取符号的地址
            elf_func[func_cnt].value=symbol.st_value;
            elf_func[func_cnt].size =symbol.st_size;
            printf("Function: %s\nAddress: 0x%lx %ld(Dec) %lx(Hec)\n",elf_func[func_cnt].func_name,elf_func[func_cnt].value,elf_func[func_cnt].size,elf_func[func_cnt].size);
            func_cnt++; //func_cnt用于只筛出来符合要求的函数
        }
    }
    // 遍历符号表，筛选出类型为FUNC的符号
    // for (size_t i = 0; i < symbol_count; ++i) {
    //     if (ELF32_ST_TYPE(symbols[i].st_info) == STT_FUNC) {
    //         if(symbols[i].st_size==0) continue; //不符合的大小直接略过
    //         // 获取符号的名称
    //         char* symbol_name=string_table + symbols[i].st_name;
    //         strcpy(elf_func[func_cnt].func_name,symbol_name);
    //         // 获取符号的地址
    //         elf_func[func_cnt].value=symbols[i].st_value;
    //         elf_func[func_cnt].size =symbols[i].st_size;
    //         printf("Function: %s\nAddress: 0x%lx %ld(Dec) %lx(Hec)\n",elf_func[func_cnt].func_name,elf_func[func_cnt].value,elf_func[func_cnt].size,elf_func[func_cnt].size);
    //         func_cnt++; //func_cnt用于只筛出来符合要求的函数
    //     }
    // }
    fclose(file);
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
    spaces[0] = '\0'; 
    char space[] = " "; // 单个空格字符
    while (--length > 0) {
        strcat(spaces, space);
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
