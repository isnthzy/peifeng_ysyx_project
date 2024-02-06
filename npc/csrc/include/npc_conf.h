

#define CONFIG_ISA_riscv 1
#define CONFIG_RVE
//isa选择
/*---------------------------------------------------------------------------------*/
#define CONFIG_TRACE 1
#define CONFIG_ITRACE 1
// #define CONFIG_FTRACE 1
#define CONFIG_MTRACE 1
#define CONFIG_DIFFTEST 1
//TRACE开关
/*---------------------------------------------------------------------------------*/
#define TRACE_VCD 1
//波形生成开关
/*---------------------------------------------------------------------------------*/
#define CONFIG_DEVICE 1
#define DEVICE_HAS_KEYBOARD 1
#define CONFIG_HAS_VGA 1
#define CONFIG_VGA_SHOW_SCREEN 1
#define CONFIG_VGA_SIZE_400x300 1
//设备开关
/*---------------------------------------------------------------------------------*/
#define MTRACE_LOGBUF_SIZE 1280
#define ITRACE_LOGBUF_SIZE 10
#define DTRACE_LOGBUF_SIZE 10
#define ETRACE_LOGBUF_SIZE 10
#define IRINGBUF_MAX_NUM 1280
//trace最多开多少IRINGBUF_MAX_NUM给多少
/*---------------------------------------------------------------------------------*/
#define CONFIG_PC_RESET_OFFSET 0x4
//pc重置偏移
/*---------------------------------------------------------------------------------*/
