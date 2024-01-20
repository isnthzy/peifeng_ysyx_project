#include <proc.h>
#include <elf.h>

#ifdef __LP64__
# define Elf_Ehdr Elf64_Ehdr
# define Elf_Phdr Elf64_Phdr
#else
# define Elf_Ehdr Elf32_Ehdr
# define Elf_Phdr Elf32_Phdr
#endif

#if defined(__ISA_X86__)
#define EXPECT_TYPE EM_X86_64
#elif defined(__ISA_MIPS32__)
#define EXPECT_TYPE EM_MIPS
#elif defined(__riscv)
#define EXPECT_TYPE EM_RISCV
#elif defined(__ISA_AM_NATIVE__)
#define EXPECT_TYPE EM_X86_64
#else
#error Unsupported ISA
#endif
size_t ramdisk_read(void *buf, size_t offset, size_t len);

static uintptr_t loader(PCB *pcb, const char *filename) {
  Elf_Ehdr elf_header;
  ramdisk_read(&elf_header,0,sizeof(Elf_Ehdr));
  if(*(uint32_t *)elf_header.e_ident != 0x464c457f) panic("elf文件异常error:1 这不是elf文件");
  if(elf_header.e_machine!=EXPECT_TYPE) panic("elf文件异常error:2 传入了错误架构的elf或使用错误的架构启动");
  Elf_Phdr program_header[elf_header.e_phnum];
  if(elf_header.e_phnum==0) panic("elf文件异常error:3 这个elf文件有点问题");
  ramdisk_read(program_header,elf_header.e_phoff,sizeof(Elf_Phdr)*elf_header.e_phnum);
  for(int i=0;i<elf_header.e_phnum;i++){
    if(program_header[i].p_type==PT_LOAD&&program_header[i].p_memsz>0){
      size_t offset= program_header[i].p_offset;
      size_t memsz = program_header[i].p_memsz;
      size_t filesz= program_header[i].p_filesz;
      ramdisk_read((void *)program_header[i].p_vaddr, offset, memsz);   
      //需要将[&ramdisk_start+offset,&ramdisk_start+offset]地址的数据拷贝到地址[VirtAddr,VirtAddr+size]上
      memset((void *)(program_header[i].p_vaddr+filesz),0,memsz-filesz);
      //再将[VirtAddr + FileSiz, VirtAddr + MemSiz)对应的物理区间清零
      /*.bss This section holds uninitialized data that contributes to the
       program's memory image.  By definition, the system  initializes 
       the data with zeros when the program begins to run.  This section is 
       of type SHT_NOBITS.  The attribute types are SHF_ALLOC and SHF_WRITE.
      */
    }
  }
  return elf_header.e_entry; //首地址 _start 程序的入口虚拟地址
}

void naive_uload(PCB *pcb, const char *filename) {
  uintptr_t entry = loader(pcb, filename);
  Log("Jump to entry = %p", entry);
  ((void(*)())entry) ();
  /*e_entry : This member gives the virtual address to which the system
              first transfers control, thus starting the process.  If
              the file has no associated entry point, this member holds
              zero. 通过ftrace我们确实可以得出程序从_start的结论
  */
}

