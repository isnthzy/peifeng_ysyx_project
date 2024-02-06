// Generated by CIRCT firtool-1.56.0
module ID_stage(	// @[<stdin>:805:3]
  input         clock,	// @[<stdin>:806:11]
                reset,	// @[<stdin>:807:11]
  input  [31:0] ID_IO_nextpc,	// @[playground/src/ID_stage.scala:7:12]
                ID_IO_pc,	// @[playground/src/ID_stage.scala:7:12]
                ID_IO_inst,	// @[playground/src/ID_stage.scala:7:12]
  input  [4:0]  ID_wb_bus_waddr,	// @[playground/src/ID_stage.scala:7:12]
  input  [31:0] ID_wb_bus_wdata,	// @[playground/src/ID_stage.scala:7:12]
  input         ID_wb_bus_wen,	// @[playground/src/ID_stage.scala:7:12]
  output        ID_to_ex_pc_sel,	// @[playground/src/ID_stage.scala:7:12]
  output [11:0] ID_to_ex_csr_addr,	// @[playground/src/ID_stage.scala:7:12]
  output [4:0]  ID_to_ex_csr_cmd,	// @[playground/src/ID_stage.scala:7:12]
  output [7:0]  ID_to_ex_st_type,	// @[playground/src/ID_stage.scala:7:12]
  output [2:0]  ID_to_ex_ld_type,	// @[playground/src/ID_stage.scala:7:12]
  output        ID_to_ex_ebreak_flag,	// @[playground/src/ID_stage.scala:7:12]
  output [1:0]  ID_to_ex_wb_sel,	// @[playground/src/ID_stage.scala:7:12]
  output [3:0]  ID_to_ex_br_type,	// @[playground/src/ID_stage.scala:7:12]
  output        ID_to_ex_wen,	// @[playground/src/ID_stage.scala:7:12]
  output [4:0]  ID_to_ex_rd,	// @[playground/src/ID_stage.scala:7:12]
  output [3:0]  ID_to_ex_alu_op,	// @[playground/src/ID_stage.scala:7:12]
  output [31:0] ID_to_ex_src1,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_src2,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_rdata1,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_rdata2,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_nextpc,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_pc,	// @[playground/src/ID_stage.scala:7:12]
                ID_to_ex_inst	// @[playground/src/ID_stage.scala:7:12]
);

  wire [31:0] _Regfile_io_rdata1;	// @[playground/src/ID_stage.scala:36:21]
  wire [31:0] _Regfile_io_rdata2;	// @[playground/src/ID_stage.scala:36:21]
  wire        _dc_io_A_sel;	// @[playground/src/ID_stage.scala:12:16]
  wire        _dc_io_B_sel;	// @[playground/src/ID_stage.scala:12:16]
  wire [2:0]  _dc_io_imm_sel;	// @[playground/src/ID_stage.scala:12:16]
  wire [4:0]  _dc_io_csr_cmd;	// @[playground/src/ID_stage.scala:12:16]
  wire        _dc_io_illegal;	// @[playground/src/ID_stage.scala:12:16]
  wire [4:0]  rs2 = ID_IO_inst[24:20];	// @[playground/src/ID_stage.scala:16:25, :29:20]
  wire [4:0]  rs1 = ID_IO_inst[19:15];	// @[playground/src/ID_stage.scala:15:25, :30:20]
  wire [2:0]  funct3 = ID_IO_inst[14:12];	// @[playground/src/ID_stage.scala:17:28, :31:23]
  wire [4:0]  rd = ID_IO_inst[11:7];	// @[playground/src/ID_stage.scala:18:24, :32:19]
  wire [6:0]  opcode = ID_IO_inst[6:0];	// @[playground/src/ID_stage.scala:19:28, :33:23]
  wire [11:0] csr_addr = ID_IO_inst[31:20];	// @[playground/src/ID_stage.scala:20:30, :34:25]
  wire        _ID_to_ex_ebreak_flag_T = _dc_io_csr_cmd == 5'h5;	// @[playground/src/ID_stage.scala:12:16, :38:39]
  wire [31:0] imm;	// @[playground/src/ID_stage.scala:14:25]
  reg         inv_flag;	// @[playground/src/ID_stage.scala:77:19]
  always @(posedge clock)	// @[<stdin>:806:11]
    inv_flag <= _dc_io_illegal & ID_IO_nextpc != 32'h80000000;	// @[playground/src/ID_stage.scala:12:16, :77:19, :78:{27,41}]
  Decode dc (	// @[playground/src/ID_stage.scala:12:16]
    .io_inst    (ID_IO_inst),
    .io_pc_sel  (ID_to_ex_pc_sel),
    .io_A_sel   (_dc_io_A_sel),
    .io_B_sel   (_dc_io_B_sel),
    .io_imm_sel (_dc_io_imm_sel),
    .io_alu_op  (ID_to_ex_alu_op),
    .io_br_type (ID_to_ex_br_type),
    .io_st_type (ID_to_ex_st_type),
    .io_ld_type (ID_to_ex_ld_type),
    .io_wb_sel  (ID_to_ex_wb_sel),
    .io_wb_en   (ID_to_ex_wen),
    .io_csr_cmd (_dc_io_csr_cmd),
    .io_illegal (_dc_io_illegal)
  );
  ImmGen ImmGen (	// @[playground/src/ID_stage.scala:13:20]
    .io_inst (ID_IO_inst),
    .io_sel  (_dc_io_imm_sel),	// @[playground/src/ID_stage.scala:12:16]
    .io_out  (imm)
  );
  RegFile Regfile (	// @[playground/src/ID_stage.scala:36:21]
    .clock     (clock),
    .reset     (reset),
    .io_waddr  (ID_wb_bus_waddr),
    .io_wdata  (ID_wb_bus_wdata),
    .io_raddr1 (rs1),	// @[playground/src/ID_stage.scala:15:25]
    .io_raddr2 (_ID_to_ex_ebreak_flag_T ? 5'hA : rs2),	// @[playground/src/ID_stage.scala:16:25, :38:{25,39}]
    .io_wen    (ID_wb_bus_wen),
    .io_rdata1 (_Regfile_io_rdata1),
    .io_rdata2 (_Regfile_io_rdata2)
  );
  inv_break inv_break (	// @[playground/src/ID_stage.scala:76:23]
    .clock    (clock),
    .reset    (reset),
    .inv_flag (inv_flag),	// @[playground/src/ID_stage.scala:77:19]
    .pc       (ID_IO_nextpc)
  );
  assign ID_to_ex_csr_addr = csr_addr;	// @[<stdin>:805:3, playground/src/ID_stage.scala:20:30]
  assign ID_to_ex_csr_cmd = _dc_io_csr_cmd;	// @[<stdin>:805:3, playground/src/ID_stage.scala:12:16]
  assign ID_to_ex_ebreak_flag = _ID_to_ex_ebreak_flag_T;	// @[<stdin>:805:3, playground/src/ID_stage.scala:38:39]
  assign ID_to_ex_rd = rd;	// @[<stdin>:805:3, playground/src/ID_stage.scala:18:24]
  assign ID_to_ex_src1 = _dc_io_A_sel ? _Regfile_io_rdata1 : ID_IO_pc;	// @[<stdin>:805:3, playground/src/ID_stage.scala:12:16, :36:21, :41:38]
  assign ID_to_ex_src2 = _dc_io_B_sel ? _Regfile_io_rdata2 : imm;	// @[<stdin>:805:3, playground/src/ID_stage.scala:12:16, :14:25, :36:21, :45:38]
  assign ID_to_ex_rdata1 = _Regfile_io_rdata1;	// @[<stdin>:805:3, playground/src/ID_stage.scala:36:21]
  assign ID_to_ex_rdata2 = _Regfile_io_rdata2;	// @[<stdin>:805:3, playground/src/ID_stage.scala:36:21]
  assign ID_to_ex_nextpc = ID_IO_nextpc;	// @[<stdin>:805:3]
  assign ID_to_ex_pc = ID_IO_pc;	// @[<stdin>:805:3]
  assign ID_to_ex_inst = ID_IO_inst;	// @[<stdin>:805:3]
endmodule

