
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
    Elf64_Ehdr ehdr;
    result=fread(&ehdr, sizeof(Elf64_Ehdr), 1, file);

    // 定位到字符串表节头部
    Elf64_Shdr strtab_shdr;
    fseek(file, ehdr.e_shoff + (ehdr.e_shstrndx * sizeof(Elf64_Shdr)), SEEK_SET);
    result=fread(&strtab_shdr, sizeof(Elf64_Shdr), 1, file);

    // 读取字符串表
    char* strtab = (char*)malloc(strtab_shdr.sh_size);
    fseek(file, strtab_shdr.sh_offset, SEEK_SET);
    result=fread(strtab, strtab_shdr.sh_size, 1, file);

    // 遍历节头部，寻找符号表节
    for (int i = 0; i < ehdr.e_shnum; i++) {
        Elf64_Shdr shdr;
        fseek(file, ehdr.e_shoff + (i * sizeof(Elf64_Shdr)), SEEK_SET);
        result=fread(&shdr, sizeof(Elf64_Shdr), 1, file);

        if (shdr.sh_type == SHT_SYMTAB) {
            // 读取符号表
            Elf64_Sym* symtab = (Elf64_Sym*)malloc(shdr.sh_size);
            fseek(file, shdr.sh_offset, SEEK_SET);
            result=fread(symtab, shdr.sh_size, 1, file);
            if(result==0) assert(0);
            // 访问符号表中的符号名称
            for (int j = 0; j < shdr.sh_size / sizeof(Elf64_Sym); j++) {
                Elf64_Sym* sym = &symtab[j];
                const char* symbol_name = strtab + sym->st_name;
                printf("Symbol Name: %s\n", symbol_name);
            }

            free(symtab);
            break;
        }
    }

    // 释放内存并关闭文件
    free(strtab);
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
