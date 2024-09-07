
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "../include/ftrace.h"

int func_depth=0;
char n_spaces[64];
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
#ifdef CONFIG_FTRACE
    if (elf_file == NULL)
        return;
    // 打开ELF文件
    FILE *file = fopen(elf_file, "rb");
    if(!file){
        Log("文件打开失败!\n");
        assert(0);
    }
    // 读取 ELF 文件的头部信息
    Elf_Ehdr elf_header;
    if (fread(&elf_header, sizeof(Elf_Ehdr), 1, file) <= 0) {
        fclose(file);
        assert(0);
    }

    // 定位到节头表
    fseek(file, elf_header.e_shoff, SEEK_SET);
    Elf_Shdr strtab_header;
    // 读取节头表并寻找字符串表节
    while (1) {
        if (fread(&strtab_header, sizeof(Elf_Shdr), 1, file) <= 0) {
            fclose(file);
            assert(0);
        }
        // 找到到字符串表节
        if (strtab_header.sh_type == SHT_STRTAB) break;
    }

    // 读取字符串表内容
    char string_table[strtab_header.sh_size];
    fseek(file, strtab_header.sh_offset, SEEK_SET);
    if (fread(string_table, strtab_header.sh_size, 1, file) <= 0) {
        fclose(file);
        assert(0);
    }

    // 读取节头表并寻找符号表表节
    Elf_Shdr symtab_header;
    fseek(file, elf_header.e_shoff, SEEK_SET);
    while (1) {
        if (fread(&symtab_header, sizeof(Elf_Shdr), 1, file) <= 0) {
            fclose(file);
            assert(0);
        }
        //找到符号表表节
        if (symtab_header.sh_type == SHT_SYMTAB) break;
    }

    // 计算符号表中的符号数量
    size_t symbol_count = symtab_header.sh_size / symtab_header.sh_entsize;
    // 定位到符号表节
    fseek(file, symtab_header.sh_offset, SEEK_SET);
    Elf_Sym symbols[symbol_count];
    // 读取符号表
    if(fread(symbols, sizeof(Elf_Sym), symbol_count, file)<=0){
        fclose(file);
        assert(0);
    }
    // 遍历符号表，筛选出类型为FUNC的符号
    for (size_t i = 0; i < symbol_count; ++i) {
        if (ELF32_ST_TYPE(symbols[i].st_info) == STT_FUNC) {
            if(symbols[i].st_size==0) continue; //不符合的大小直接略过
            // 获取符号的名称
            char* symbol_name=string_table + symbols[i].st_name;
            strcpy(elf_func[func_cnt].func_name,symbol_name);
            // 获取符号的地址
            elf_func[func_cnt].value=symbols[i].st_value;
            elf_func[func_cnt].size =symbols[i].st_size;
            printf("Function: %s\nAddress: 0x%lx %ld(Dec) %lx(Hec)\n",elf_func[func_cnt].func_name,elf_func[func_cnt].value,elf_func[func_cnt].size,elf_func[func_cnt].size);
            func_cnt++; //func_cnt用于只筛出来符合要求的函数
        }
    }
    fclose(file);
#endif
}
/*ftrace追踪内容*/
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
    static char func_log[128];
    func_depth++;
    if(func_depth<=1) return; //忽略trm_init
    generateSpaces(func_depth,n_spaces);
    sprintf(func_log,"0x%x:%s call[%s->%s@0x%x]\n",pc,n_spaces,find_funcname(pc),find_funcname(dnpc),dnpc);
    log_write("%s",func_log);
    printf("%s",func_log);
    if (is_tail) {
		insert_tail_rec(pc,dnpc,func_depth-1);
	}
    return;
}
void func_ret(paddr_t pc){
    generateSpaces(func_depth,n_spaces);
    func_depth--;
    static char func_log[128];
    sprintf(func_log,"0x%x:%s ret [%s]\n",pc,n_spaces,find_funcname(pc));
    log_write("%s",func_log);
    printf("%s",func_log);
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

const char* find_funcname(paddr_t pc){
    for(int i=0;i<func_cnt;i++){
        if(elf_func[i].value<=pc&&pc<(elf_func[i].value+elf_func[i].size)){
            return elf_func[i].func_name;
        }
    }
    return "???";
}
