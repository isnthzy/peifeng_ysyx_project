
`define DIFFTEST_DPIC_FUNC_NAME(name) \
  v_difftest_``name

`define DIFFTEST_DPIC_FUNC_DECL(name) \
  import "DPI-C" function void `DIFFTEST_DPIC_FUNC_NAME(name)

`define DIFFTEST_MOD_NAME(name)    \
  Difftest``name

`define DIFFTEST_MOD_DECL(name)    \
  module `DIFFTEST_MOD_NAME(name)

`define DIFFTEST_MOD_DPIC_CALL_BEGIN(name) \
  always @(posedge clock) begin            \
    `DIFFTEST_DPIC_FUNC_NAME(name)

`define DIFFTEST_MOD_DPIC_CALL_BEGIN_WITH_EN(enable, name) \
  always @(posedge clock) begin                            \
    if (enable) begin                                      \
      `DIFFTEST_DPIC_FUNC_NAME(name)

`define DIFFTEST_MOD_DPIC_CALL_END(name) \
  ; end

`define DIFFTEST_MOD_DPIC_CALL_END_WITH_EN(name) \
  ; end end

`define DPIC_ARG_BIT  input bit
`define DPIC_ARG_BYTE input byte
`define DPIC_ARG_INT  input int
`define DPIC_ARG_LONG input longint

// DifftestInstrCommit
`DIFFTEST_DPIC_FUNC_DECL(InstrCommit) (
    `DPIC_ARG_BYTE index,
    `DPIC_ARG_BIT  valid,
    `DPIC_ARG_LONG pc,
    `DPIC_ARG_INT  instr,
    `DPIC_ARG_BIT  skip,
    `DPIC_ARG_BIT  wen,
    `DPIC_ARG_BYTE wdest,
    `DPIC_ARG_LONG wdata,
    `DPIC_ARG_BIT  csrRstat,
    `DPIC_ARG_LONG csrData
);
`DIFFTEST_MOD_DECL(InstrCommit)(
    input        clock,
    input [ 7:0] index,
    input        valid,
    input [63:0] pc,
    input [31:0] instr,
    input        skip,
    input        wen,
    input [ 7:0] wdest,
    input [63:0] wdata,
    input        csrRstat,
    input [63:0] csrData
);
    `DIFFTEST_MOD_DPIC_CALL_BEGIN_WITH_EN(valid, InstrCommit) (
        index,
        valid, pc, instr, skip, wen, wdest, wdata, csrRstat, csrData
        ) `DIFFTEST_MOD_DPIC_CALL_END_WITH_EN(InstrCommit)
endmodule

// DifftestExcpEvent
`DIFFTEST_DPIC_FUNC_DECL(ExcpEvent) (
    `DPIC_ARG_BIT  excp_valid,
    `DPIC_ARG_BIT  isMret,
    `DPIC_ARG_INT  intrNo,
    `DPIC_ARG_INT  cause,
    `DPIC_ARG_LONG exceptionPC,
    `DPIC_ARG_INT  exceptionInst
);
`DIFFTEST_MOD_DECL(ExcpEvent) (
    input        clock,
    input        excp_valid,
    input        isMret,
    input [31:0] intrptNo,
    input [31:0] cause,
    input [63:0] exceptionPC,
    input [31:0] exceptionInst
);
    `DIFFTEST_MOD_DPIC_CALL_BEGIN(ExcpEvent) (
        excp_valid, isMret, intrptNo, cause, exceptionPC, exceptionInst
        ) `DIFFTEST_MOD_DPIC_CALL_END(ExcpEvent)
endmodule

// DifftestStoreEvent
`DIFFTEST_DPIC_FUNC_DECL(StoreEvent) (
    `DPIC_ARG_BYTE index,
    `DPIC_ARG_BYTE valid,
    `DPIC_ARG_LONG paddr,
    `DPIC_ARG_LONG vaddr,
    `DPIC_ARG_LONG data
);
`DIFFTEST_MOD_DECL(StoreEvent)(
    input        clock,
    input [ 7:0] index,
    input [ 7:0] valid,
    input [63:0] paddr,
    input [63:0] vaddr,
    input [63:0] data
);
    `DIFFTEST_MOD_DPIC_CALL_BEGIN(StoreEvent) (
        index, valid, paddr, vaddr, data
        )
    `DIFFTEST_MOD_DPIC_CALL_END(StoreEvent)
endmodule

// DifftestLoadEvent
`DIFFTEST_DPIC_FUNC_DECL(LoadEvent) (
    `DPIC_ARG_BYTE index,
    `DPIC_ARG_BYTE valid,
    `DPIC_ARG_LONG paddr,
    `DPIC_ARG_LONG vaddr,
    `DPIC_ARG_LONG data ,
);
`DIFFTEST_MOD_DECL(LoadEvent)(
    input        clock,
    input [ 7:0] index,
    input [ 7:0] valid,
    input [63:0] paddr,
    input [63:0] vaddr,
    input [63:0] data
);
    `DIFFTEST_MOD_DPIC_CALL_BEGIN(LoadEvent) (
         index, valid, paddr, vaddr, data
        ) `DIFFTEST_MOD_DPIC_CALL_END(LoadEvent)
endmodule

// DifftestCSRRegState
`DIFFTEST_DPIC_FUNC_DECL(CSRRegState) (
    `DPIC_ARG_LONG mstatus,
    `DPIC_ARG_LONG mtvec,
    `DPIC_ARG_LONG mepc,
    `DPIC_ARG_LONG mcause
);
`DIFFTEST_MOD_DECL(CSRRegState)(
    input        clock,
    input [63:0] mstatus,
    input [63:0] mtvec,
    input [63:0] mepc,
    input [63:0] mcause
);
    `DIFFTEST_MOD_DPIC_CALL_BEGIN(CSRRegState) (
        mstatus, mtvec, mepc, mcause
        ) `DIFFTEST_MOD_DPIC_CALL_END(CSRRegState)
endmodule

// DifftestGRegState
`DIFFTEST_DPIC_FUNC_DECL(GRegState) (
    `DPIC_ARG_LONG gpr_0,
    `DPIC_ARG_LONG gpr_1,
    `DPIC_ARG_LONG gpr_2,
    `DPIC_ARG_LONG gpr_3,
    `DPIC_ARG_LONG gpr_4,
    `DPIC_ARG_LONG gpr_5,
    `DPIC_ARG_LONG gpr_6,
    `DPIC_ARG_LONG gpr_7,
    `DPIC_ARG_LONG gpr_8,
    `DPIC_ARG_LONG gpr_9,
    `DPIC_ARG_LONG gpr_10,
    `DPIC_ARG_LONG gpr_11,
    `DPIC_ARG_LONG gpr_12,
    `DPIC_ARG_LONG gpr_13,
    `DPIC_ARG_LONG gpr_14,
    `DPIC_ARG_LONG gpr_15,
    `DPIC_ARG_LONG gpr_16,
    `DPIC_ARG_LONG gpr_17,
    `DPIC_ARG_LONG gpr_18,
    `DPIC_ARG_LONG gpr_19,
    `DPIC_ARG_LONG gpr_20,
    `DPIC_ARG_LONG gpr_21,
    `DPIC_ARG_LONG gpr_22,
    `DPIC_ARG_LONG gpr_23,
    `DPIC_ARG_LONG gpr_24,
    `DPIC_ARG_LONG gpr_25,
    `DPIC_ARG_LONG gpr_26,
    `DPIC_ARG_LONG gpr_27,
    `DPIC_ARG_LONG gpr_28,
    `DPIC_ARG_LONG gpr_29,
    `DPIC_ARG_LONG gpr_30,
    `DPIC_ARG_LONG gpr_31
);
`DIFFTEST_MOD_DECL(GRegState)(
    input         clock,
    input [63:0]  gpr_0,
    input [63:0]  gpr_1,
    input [63:0]  gpr_2,
    input [63:0]  gpr_3,
    input [63:0]  gpr_4,
    input [63:0]  gpr_5,
    input [63:0]  gpr_6,
    input [63:0]  gpr_7,
    input [63:0]  gpr_8,
    input [63:0]  gpr_9,
    input [63:0]  gpr_10,
    input [63:0]  gpr_11,
    input [63:0]  gpr_12,
    input [63:0]  gpr_13,
    input [63:0]  gpr_14,
    input [63:0]  gpr_15,
    input [63:0]  gpr_16,
    input [63:0]  gpr_17,
    input [63:0]  gpr_18,
    input [63:0]  gpr_19,
    input [63:0]  gpr_20,
    input [63:0]  gpr_21,
    input [63:0]  gpr_22,
    input [63:0]  gpr_23,
    input [63:0]  gpr_24,
    input [63:0]  gpr_25,
    input [63:0]  gpr_26,
    input [63:0]  gpr_27,
    input [63:0]  gpr_28,
    input [63:0]  gpr_29,
    input [63:0]  gpr_30,
    input [63:0]  gpr_31
);
    `DIFFTEST_MOD_DPIC_CALL_BEGIN(GRegState) (
        gpr_0,  gpr_1,  gpr_2,  gpr_3,  gpr_4,  gpr_5,  gpr_6,  gpr_7,
        gpr_8,  gpr_9,  gpr_10, gpr_11, gpr_12, gpr_13, gpr_14, gpr_15,
        gpr_16, gpr_17, gpr_18, gpr_19, gpr_20, gpr_21, gpr_22, gpr_23,
        gpr_24, gpr_25, gpr_26, gpr_27, gpr_28, gpr_29, gpr_30, gpr_31
        ) `DIFFTEST_MOD_DPIC_CALL_END(GRegState)
endmodule
