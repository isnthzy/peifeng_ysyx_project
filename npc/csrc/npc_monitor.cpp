
#include "include/npc/npc_waveform.h"
#include "include/npc_verilator.h"
#include "include/difftest/difftest.h"
#include "include/npc/npc_memory.h"
#include "include/npc/npc_sdb.h"
#include "include/npc/npc_exe.h"
#include "include/util/utils.h"
#ifdef  CONFIG_DEVICE
#include "include/npc/npc_device.h"
#endif
Difftest* difftest;
Waveform* waveform;
IRingBuffer mtrace_buffer;
IRingBuffer iring_buffer;
extern uint64_t wavebegin;
extern bool difftest_flag;
extern uint64_t g_nr_guest_inst;
bool ftrace_flag=false;


static const uint32_t defaultImg [] = {
  0x00000413, //00
  0x00009117, //04
  0xffc10113, //08
  0x018000ef, //0c
  0x00000513, //10
  0x00008067, //14
  0x00050513, //18
  0x00100073, //1c
  0x0000006f, //20
  0xff010113, //24
  0x00112623, //28
  0x00000517, //2c
  0x01050513, //30
  0xfddff0ef, //34
  0xfe1ff0ef, //38

}; //defaultImg本质其实是个dummy

//log模块
FILE *log_fp = NULL;

void init_log(const char *log_file) {
  log_fp = stdout;
  if (log_file == NULL){
    printf_red("need parse --log={npclog}.txt\n");
    panic("Can not open '%s'", log_file);
  }else{
    FILE *fp = fopen(log_file, "w");
    Assert(fp, "Can not open '%s'", log_file);
    log_fp = fp;
  }
  Log("Log is written to %s", log_file ? log_file : "stdout");
}
bool log_enable() {
  return MUXDEF(CONFIG_TRACE, (g_nr_guest_inst >= 0) &&
         (g_nr_guest_inst <= NPCLOG_NUM), false); //限制log的输出数，最多输出NPCLOG_NUM条
}
//分割线

void init_elf(const char *elf_file);
void init_sdb();
void init_disasm(const char *triple);
void init_waveform();

static void welcome() {
  Log("Trace: %s", MUXDEF(CONFIG_TRACE, ANSI_FMT("ON", ANSI_FG_GREEN), ANSI_FMT("OFF", ANSI_FG_RED)));
  IFDEF(CONFIG_TRACE, Log("If trace is enabled, a log file will be generated "
        "to record the trace. This may lead to a large log file. "
        "If it is not necessary, you can disable it in menuconfig"));
  Log("Build time: %s, %s", __TIME__, __DATE__);
  #ifdef CONFIG_WAVEFORM
  if(wavebegin==0){
    printf_red("Waveform is closed\n");
  }else{
    printf_green("Waveform is open at %ld dump time\n",wavebegin);
  }
  #else
  printf_red("Waveform is closed\n");
  #endif
  printf("Welcome to %s-NPC!\n", ANSI_FMT(str(riscv32e), ANSI_FG_YELLOW ANSI_BG_RED));
  printf("For help, type \"help\"\n");
}

void init_reset(int n){
  top->reset=1;
  top->clock=0;
  step_and_dump_wave();
  while (n-->0){
    top->clock=1;
    step_and_dump_wave();
    if(n==0) top->reset=0;
    top->clock=0;
    step_and_dump_wave();
  }
}





#include <getopt.h>



static char *log_file = NULL;
static char *diff_so_file = NULL;
static char *img_file = NULL;
static char *elf_file = NULL;
static int difftest_port = 1234;

static long load_img() {
  if (img_file == NULL) {
    printf("\033[0m\033[1;31m img=NULL -> use init_img \033[0m\n");
    memcpy(guest_to_host(START_ADDR),defaultImg, sizeof(defaultImg));
    return 4096; // built-in image size
  }

  FILE *fp = fopen(img_file, "rb");
  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);
  printf("The image is %s, size = %ld\n", img_file, size);
  fseek(fp, 0, SEEK_SET);
  int ret = fread(guest_to_host(START_ADDR), size, 1, fp);
  assert(ret == 1);
  fclose(fp);
  return size;
}

static int parse_args(int argc, char *argv[]) {
  const struct option table[] = {
    {"batch"    , no_argument      , NULL, 'b'},
    {"log"      , required_argument, NULL, 'l'},
    {"diff"     , required_argument, NULL, 'd'},
    {"port"     , required_argument, NULL, 'p'},
    {"help"     , no_argument      , NULL, 'h'},
    {"ftrace"   , required_argument, NULL, 'f'},
    {"wavebegin", required_argument, NULL, 'w'},
    {0          , 0                , NULL,  0 },
  };
  int o;
  while ( (o = getopt_long(argc, argv, "-bhl:d:p:f:w:", table, NULL)) != -1) {
    switch (o) {
      case 'b': sdb_set_batch_mode(); break;
      case 'p': sscanf(optarg, "%d", &difftest_port); break;
      case 'l': log_file = optarg; break;
      case 'd': diff_so_file = optarg; MUXDEF(CONFIG_DIFFTEST,difftest_flag=true,difftest_flag=false); break;
      case 'f': elf_file = optarg; ftrace_flag=true;  break;
      case 'w': wavebegin = atoi(optarg); break;
      case 1: img_file = optarg; return 0;
      default:
        printf("Usage: %s [OPTION...] IMAGE [args]\n\n", argv[0]);
        printf("\t-b,--batch              run with batch mode\n");
        printf("\t-l,--log=FILE           output log to FILE\n");
        printf("\t-d,--diff=REF_SO        run DiffTest with reference REF_SO\n");
        printf("\t-p,--port=PORT          run DiffTest with port PORT\n");
        printf("\t-f,--ftrace=ELF         use ${IMAGE}.elf to ftrance\n");
        printf("\n");
        exit(0);
    }
  }
  return 0;
}

void init_monitor(int argc, char *argv[]) {
  /* Perform some global initialization. */

  /* Parse arguments. */
  parse_args(argc, argv);

  /* init waveform*/
#ifdef CONFIG_WAVEFORM
  waveform = new Waveform();
  if(wavebegin!=0){
    waveform->init_waveform();
  }
#endif

  /* Open the log file. */
  init_log(log_file);
  
  /* init the mem. */
  //把物理内存初始化为随机数，方便快速定位访问的问题
  //这可能会导致与ref（difftest）通信不一致
  init_mem();


  long img_size=load_img();
  //读入镜像文件

  init_traces();
  //初始化traces

  init_reset(42);
  //初始化reset


  // /* Open the ${IMAGE}.elf file */
  IFDEF(CONFIG_FTRACE,init_elf(elf_file));

  /* Initialize device. */
  IFDEF(CONFIG_DEVICE, init_device());

  /* Initialize differential testing. */
  //NOTE:未传入difftest参数时，difftest的nemu_proxy为不可用状态
  difftest = new Difftest();
  difftest->init_difftest(diff_so_file, difftest_port);
  difftest->set_img_size(img_size);

  /* Initialize the simple debugger. */
  init_sdb();

  IFDEF(CONFIG_TRACE, init_disasm(
    MUXDEF(CONFIG_ISA_riscv,
      MUXDEF(CONFIG_RV64,      "riscv64",
                               "riscv32"),
                               "bad") "-pc-linux-gnu"
  ));//初始化llvm实现itrace
  /* Display welcome message. */
  welcome();

}