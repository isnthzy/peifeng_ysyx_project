#define CONFIG_ISA_riscv 1
#define CONFIG_RVE 1
//isa选择,请注意，当nemu没有开启E扩展而npc开启时，会因为寄存器大小不同导致与nemu的difftest初始化通信拷贝异常而无法正确启动(初始化)
//因此仅仅是使用ARCH=riscv32e这个行为是无效的 使用前请确保nemu和npc的E扩展都开启！

/*---------------------------------------------------------------------------------*/

#define CONFIG_TRACE 1
#define CONFIG_ITRACE 1
#define CONFIG_FTRACE 1
#define CONFIG_MTRACE 1
#define CONFIG_DIFFTEST 1

#define NPCLOG_NUM 1000000 //trace最多记录多少个log
//TRACE开关

/*---------------------------------------------------------------------------------*/

#define CONFIG_MEM_RANDOM 1
//随机化内存开关,可能会导致与ref_difftest无法通过(例如rt-thread)

/*---------------------------------------------------------------------------------*/

// #define TRACE_VCD 1
/* 波形生成开关 */

/*---------------------------------------------------------------------------------*/

#define CONFIG_DEVICE 1
// #define DEVICE_HAS_KEYBOARD 1
// #define CONFIG_HAS_VGA 1
// #define CONFIG_VGA_SIZE_400x300 1
//设备开关,默认串口输出打开

/*---------------------------------------------------------------------------------*/

#define MTRACE_LOGBUF_SIZE 10
#define ITRACE_LOGBUF_SIZE 10
#define DTRACE_LOGBUF_SIZE 10
#define ETRACE_LOGBUF_SIZE 10
#define IRINGBUF_MAX_NUM 128
//trace最多开多少IRINGBUF_MAX_NUM给多少

/*---------------------------------------------------------------------------------*/

#define CONFIG_PC_RESET_OFFSET 0x0
//pc重置偏移
#define CONFIG_MAX_EXE_INST 50000000
//指令执行最大次数，拦截可能发生的死循环
/*---------------------------------------------------------------------------------*/
