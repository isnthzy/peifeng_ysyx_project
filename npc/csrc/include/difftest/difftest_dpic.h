#ifndef DIFFTEST_DPIC_H
#define DIFFTEST_DPIC_H

#include <stdint.h>
#include "../npc_conf.h"

#define DIFFTEST_DPIC_FUNC_NAME(name) \
    v_difftest_##name

#define DIFFTEST_DPIC_FUNC_DECL(name) \
    extern "C" void DIFFTEST_DPIC_FUNC_NAME(name)

#define DPIC_ARG_BIT  uint8_t
#define DPIC_ARG_BYTE char
#define DPIC_ARG_INT  uint32_t
#define DPIC_ARG_LONG uint64_t

// v_difftest_InstrCommit
#define INTERFACE_INSTR_COMMIT           \
  DIFFTEST_DPIC_FUNC_DECL(InstrCommit) ( \
    DPIC_ARG_BYTE index,                 \
    DPIC_ARG_BIT  valid,                 \
    DPIC_ARG_LONG pc,                    \
    DPIC_ARG_INT  instr,                 \
    DPIC_ARG_BIT  skip,                  \
    DPIC_ARG_BIT  wen,                   \
    DPIC_ARG_BYTE wdest,                 \
    DPIC_ARG_LONG wdata,                 \
    DPIC_ARG_BIT  csrRstat,             \
    DPIC_ARG_LONG csrData               \
  )

// v_difftest_ExcpEvent
#define INTERFACE_EXCP_EVENT             \
  DIFFTEST_DPIC_FUNC_DECL(ExcpEvent) (   \
    DPIC_ARG_BIT  excp_valid,            \
    DPIC_ARG_BIT  isMret,                \
    DPIC_ARG_INT  intrptNo,                \
    DPIC_ARG_INT  cause,                 \
    DPIC_ARG_LONG exceptionPC,           \
    DPIC_ARG_INT  exceptionInst          \
  )

// v_difftest_StoreEvent
#define INTERFACE_STORE_EVENT            \
  DIFFTEST_DPIC_FUNC_DECL(StoreEvent) (  \
    DPIC_ARG_BYTE index,                 \
    DPIC_ARG_BYTE valid,                 \
    DPIC_ARG_LONG paddr,            \
    DPIC_ARG_LONG vaddr,            \
    DPIC_ARG_LONG data              \
  )

// v_difftest_LoadEvent
#define INTERFACE_LOAD_EVENT             \
  DIFFTEST_DPIC_FUNC_DECL(LoadEvent) (   \
    DPIC_ARG_BYTE index,                 \
    DPIC_ARG_BYTE valid,                 \
    DPIC_ARG_LONG paddr,                 \
    DPIC_ARG_LONG vaddr,                 \
    DPIC_ARG_LONG data                   \
  )

// v_difftest_CSRState
#define INTERFACE_CSRREG_STATE          \
  DIFFTEST_DPIC_FUNC_DECL(CSRRegState) (\
    DPIC_ARG_LONG mstatus,              \
    DPIC_ARG_LONG mtvec,                \
    DPIC_ARG_LONG mepc,                 \
    DPIC_ARG_LONG mcause                \
  )

// v_difftest_GRegState
#define INTERFACE_GREG_STATE \
  DIFFTEST_DPIC_FUNC_DECL(GRegState) (     \
    DPIC_ARG_LONG gpr_0,                 \
    DPIC_ARG_LONG gpr_1,                 \
    DPIC_ARG_LONG gpr_2,                 \
    DPIC_ARG_LONG gpr_3,                 \
    DPIC_ARG_LONG gpr_4,                 \
    DPIC_ARG_LONG gpr_5,                 \
    DPIC_ARG_LONG gpr_6,                 \
    DPIC_ARG_LONG gpr_7,                 \
    DPIC_ARG_LONG gpr_8,                 \
    DPIC_ARG_LONG gpr_9,                 \
    DPIC_ARG_LONG gpr_10,                \
    DPIC_ARG_LONG gpr_11,                \
    DPIC_ARG_LONG gpr_12,                \
    DPIC_ARG_LONG gpr_13,                \
    DPIC_ARG_LONG gpr_14,                \
    DPIC_ARG_LONG gpr_15,                \
    DPIC_ARG_LONG gpr_16,                \
    DPIC_ARG_LONG gpr_17,                \
    DPIC_ARG_LONG gpr_18,                \
    DPIC_ARG_LONG gpr_19,                \
    DPIC_ARG_LONG gpr_20,                \
    DPIC_ARG_LONG gpr_21,                \
    DPIC_ARG_LONG gpr_22,                \
    DPIC_ARG_LONG gpr_23,                \
    DPIC_ARG_LONG gpr_24,                \
    DPIC_ARG_LONG gpr_25,                \
    DPIC_ARG_LONG gpr_26,                \
    DPIC_ARG_LONG gpr_27,                \
    DPIC_ARG_LONG gpr_28,                \
    DPIC_ARG_LONG gpr_29,                \
    DPIC_ARG_LONG gpr_30,                \
    DPIC_ARG_LONG gpr_31                 \
  )

INTERFACE_INSTR_COMMIT;
INTERFACE_EXCP_EVENT;
INTERFACE_STORE_EVENT;
INTERFACE_LOAD_EVENT;
INTERFACE_CSRREG_STATE;
INTERFACE_GREG_STATE;


#endif