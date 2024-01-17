

#define CONFIG_ISA_riscv 1
//isa选择

// #define CONFIG_TRACE 1
// #define CONFIG_ITRACE 1
// #define CONFIG_FTRACE 1
// #define CONFIG_MTRACE 1
// #define CONFIG_DIFFTEST 1

// #define TRACE_VCD 1
//波形生成开关

#define CONFIG_DEVICE 1
#define DEVICE_HAS_KEYBOARD 1
//设备开关

#define MTRACE_LOGBUF_SIZE 10
#define ITRACE_LOGBUF_SIZE 10
#define DTRACE_LOGBUF_SIZE 10
#define ETRACE_LOGBUF_SIZE 10
//trace最多开128个,因为我只给了128个

#define CONFIG_PC_RESET_OFFSET 0x4
//pc重置偏移
#define DIFFTEST_TO_REF 1
#define DIFFTEST_TO_DUT 0
//固定好的宏配置
