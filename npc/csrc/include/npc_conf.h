

#define CONFIG_ISA_riscv 1
// #define CONFIG_TRACE 1
// #define CONFIG_ITRACE 1
// #define CONFIG_FTRACE 1
// #define CONFIG_MTRACE 1
// #define CONFIG_DIFFTEST 1
#define CONFIG_PC_RESET_OFFSET 0x4
#define DIFFTEST_TO_REF 1
#define DIFFTEST_TO_DUT 0
#define MTRACE_LOGBUF_SIZE 10
#define ITRACE_LOGBUF_SIZE 10
#define DTRACE_LOGBUF_SIZE 10
#define ETRACE_LOGBUF_SIZE 10
//trace最多开128个,因为我只给了128个