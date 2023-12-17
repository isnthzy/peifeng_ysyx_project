
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
    Elf32_Ehdr ehdr;
    result=fread(&ehdr, sizeof(Elf32_Ehdr), 1, file);

    // 定位到符号表的偏移量和数量
    Elf32_Shdr shdr;
    fseek(file, ehdr.e_shoff + (ehdr.e_shstrndx * sizeof(Elf32_Shdr)), SEEK_SET);
    result=fread(&shdr, sizeof(Elf32_Shdr), 1, file);

    // 读取字符串表的偏移量和大小
    Elf32_Off strtab_offset = shdr.sh_offset;
    Elf32_Word strtab_size = shdr.sh_size;

    // 定位到符号表的偏移量和数量
    fseek(file, ehdr.e_shoff, SEEK_SET);
    for (int i = 0; i < ehdr.e_shnum; i++) {
        result=fread(&shdr, sizeof(Elf32_Shdr), 1, file);
        if (shdr.sh_type == SHT_SYMTAB) {
            break;
        }
    }

    // 读取符号表的偏移量和数量
    Elf32_Off symtab_offset = shdr.sh_offset;
    Elf32_Word symtab_size = shdr.sh_size;

    // 读取字符串表
    char* strtab = (char*)malloc(strtab_size);
    fseek(file, strtab_offset, SEEK_SET);
    result=fread(strtab, strtab_size, 1, file);

    // 读取符号表
    Elf32_Sym* symtab = (Elf32_Sym*)malloc(symtab_size);
    fseek(file, symtab_offset, SEEK_SET);
    result=fread(symtab, symtab_size, 1, file);
    if(result==0) assert(0);
    // 访问符号表中的符号名称
    for (int i = 0; i < symtab_size / sizeof(Elf32_Sym); i++) {
        Elf32_Sym* sym = &symtab[i];
        const char* symbol_name = strtab + sym->st_name;
        strcpy(elf_func[func_cnt].func_name,symbol_name);
        // 获取符号的地址
        elf_func[func_cnt].value=sym->st_value;
        elf_func[func_cnt].size =sym->st_size;
        printf("Function: %s\nAddress: 0x%lx %ld(Dec) %lx(Hec)\n",elf_func[func_cnt].func_name,elf_func[func_cnt].value,elf_func[func_cnt].size,elf_func[func_cnt].size);
        func_cnt++; //func_cnt用于只筛出来符合要求的函数
        // printf("Symbol Name: %s\n", symbol_name);
    }

    // 释放内存并关闭文件
    free(strtab);
    free(symtab);
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
