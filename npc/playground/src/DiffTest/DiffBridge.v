module DiffBridge(
    input        clock,
    input [ 7:0] index,
    input        instrValid,
    input [63:0] the_pc,
    input [31:0] instr,
    input        skip,
    input        wen,
    input [ 7:0] wdest,
    input [63:0] wdata,
    input        csrRstat,
    input [63:0] csrData,

    input        excp_valid,
    input        isMret,
    input [31:0] intrptNo,
    input [31:0] cause,
    input [63:0] exceptionPC,
    input [31:0] exceptionInst,

    input [ 7:0] storeIndex,
    input [ 7:0] storeValid,
    input [63:0] storePaddr,
    input [63:0] storeVaddr,
    input [63:0] storeData,
    input [ 7:0] storeLen,

    input [ 7:0] loadIndex,
    input [ 7:0] loadValid,
    input [63:0] loadPaddr,
    input [63:0] loadVaddr,
    input [63:0] loadData,
    input [ 7:0] loadLen,

    input [63:0] mstatus,
    input [63:0] mtvec,
    input [63:0] mepc,
    input [63:0] mcause,

    input [63:0] REG_0,
    input [63:0] REG_1,
    input [63:0] REG_2,
    input [63:0] REG_3,
    input [63:0] REG_4,
    input [63:0] REG_5,
    input [63:0] REG_6,
    input [63:0] REG_7,
    input [63:0] REG_8,
    input [63:0] REG_9,
    input [63:0] REG_10,
    input [63:0] REG_11,
    input [63:0] REG_12,
    input [63:0] REG_13,
    input [63:0] REG_14,
    input [63:0] REG_15,
    input [63:0] REG_16,
    input [63:0] REG_17,
    input [63:0] REG_18,
    input [63:0] REG_19,
    input [63:0] REG_20,
    input [63:0] REG_21,
    input [63:0] REG_22,
    input [63:0] REG_23,
    input [63:0] REG_24,
    input [63:0] REG_25,
    input [63:0] REG_26,
    input [63:0] REG_27,
    input [63:0] REG_28,
    input [63:0] REG_29,
    input [63:0] REG_30,
    input [63:0] REG_31
);

DifftestInstrCommit DifftestInstrCommit(
    .clock              (clock          ),
    .index              (index          ),
    .valid              (instrValid     ),
    .pc                 (the_pc         ),
    .instr              (instr          ),
    .skip               (skip           ),
    .wen                (wen            ),
    .wdest              (wdest          ),
    .wdata              (wdata          ),
    .csrRstat           (csrRstat       ),
    .csrData            (csrData        )
);


DifftestStoreEvent DifftestStoreEvent(
    .clock              (clock          ),
    .index              (storeIndex     ),
    .valid              (storeValid     ),
    .paddr              (storePaddr     ),
    .vaddr              (storeVaddr     ),
    .data               (storeData      ),
    .len                (storeLen       )
);

DifftestLoadEvent DifftestLoadEvent(
    .clock              (clock          ),
    .index              (loadIndex      ),
    .valid              (loadValid      ),
    .paddr              (loadPaddr      ),
    .vaddr              (loadVaddr      ),
    .data               (loadData       ),
    .len                (loadLen       )
);


DifftestExcpEvent DifftestExcpEvent(
    .clock              (clock          ),
    .excp_valid         (excp_valid     ),
    .isMret             (isMret         ),
    .intrptNo           (intrptNo       ),
    .cause              (cause          ),
    .exceptionPC        (exceptionPC    ),
    .exceptionInst      (exceptionInst  )
);

DifftestCSRRegState DifftestCSRRegState(
    .clock              (clock              ),
    .mstatus            (mstatus            ),
    .mtvec              (mtvec              ),
    .mepc               (mepc               ),
    .mcause             (mcause             )
);

DifftestGRegState DifftestGRegState(
    .clock              (clock     ),
    .gpr_0              (0         ),
    .gpr_1              (REG_1     ),
    .gpr_2              (REG_2     ),
    .gpr_3              (REG_3     ),
    .gpr_4              (REG_4     ),
    .gpr_5              (REG_5     ),
    .gpr_6              (REG_6     ),
    .gpr_7              (REG_7     ),
    .gpr_8              (REG_8     ),
    .gpr_9              (REG_9     ),
    .gpr_10             (REG_10    ),
    .gpr_11             (REG_11    ),
    .gpr_12             (REG_12    ),
    .gpr_13             (REG_13    ),
    .gpr_14             (REG_14    ),
    .gpr_15             (REG_15    ),
    .gpr_16             (REG_16    ),
    .gpr_17             (REG_17    ),
    .gpr_18             (REG_18    ),
    .gpr_19             (REG_19    ),
    .gpr_20             (REG_20    ),
    .gpr_21             (REG_21    ),
    .gpr_22             (REG_22    ),
    .gpr_23             (REG_23    ),
    .gpr_24             (REG_24    ),
    .gpr_25             (REG_25    ),
    .gpr_26             (REG_26    ),
    .gpr_27             (REG_27    ),
    .gpr_28             (REG_28    ),
    .gpr_29             (REG_29    ),
    .gpr_30             (REG_30    ),
    .gpr_31             (REG_31    )
);

endmodule