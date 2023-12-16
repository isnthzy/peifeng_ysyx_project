// Generated by CIRCT firtool-1.56.0
module EXE_stage(	// @[<stdin>:573:3]
  input         clock,	// @[<stdin>:574:11]
                reset,	// @[<stdin>:575:11]
                io_d_ebus_is_ebreak,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_data_wen,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_result_is_imm,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_result_is_snpc,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_src_is_sign,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_src1_is_pc,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_src2_is_imm,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_src2_is_shamt_imm,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_src2_is_shamt_src,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_sram_valid,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_sram_wen,	// @[playground/src/EXE_stage.scala:4:12]
  input  [3:0]  io_d_ebus_wmask,	// @[playground/src/EXE_stage.scala:4:12]
  input  [31:0] io_d_ebus_snpc,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_imm,	// @[playground/src/EXE_stage.scala:4:12]
  input  [4:0]  io_d_ebus_src1,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_src2,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_rd,	// @[playground/src/EXE_stage.scala:4:12]
  input         io_d_ebus_alu_op_0,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_alu_op_1,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_alu_op_2,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_alu_op_3,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_alu_op_4,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_alu_op_5,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_alu_op_6,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_alu_op_7,	// @[playground/src/EXE_stage.scala:4:12]
                io_d_ebus_alu_op_8,	// @[playground/src/EXE_stage.scala:4:12]
  input  [31:0] io_pc,	// @[playground/src/EXE_stage.scala:4:12]
  output [31:0] io_result,	// @[playground/src/EXE_stage.scala:4:12]
                io_jalr_taget,	// @[playground/src/EXE_stage.scala:4:12]
  output        io_sram_valid,	// @[playground/src/EXE_stage.scala:4:12]
                io_sram_wen,	// @[playground/src/EXE_stage.scala:4:12]
  output [31:0] io_sram_wdata,	// @[playground/src/EXE_stage.scala:4:12]
  output [3:0]  io_sram_wmask	// @[playground/src/EXE_stage.scala:4:12]
);

  wire [31:0] _alu_io_result;	// @[playground/src/EXE_stage.scala:23:18]
  wire [31:0] _RegFile_io_rdata1;	// @[playground/src/EXE_stage.scala:14:21]
  wire [31:0] _RegFile_io_rdata2;	// @[playground/src/EXE_stage.scala:14:21]
  wire [31:0] _jalr_tmp_T = _alu_io_result + io_d_ebus_imm;	// @[playground/src/EXE_stage.scala:23:18, :36:29]
  RegFile RegFile (	// @[playground/src/EXE_stage.scala:14:21]
    .clock     (clock),
    .reset     (reset),
    .io_waddr  (io_d_ebus_rd),
    .io_wdata  ({27'h0, io_d_ebus_rd}),	// @[playground/src/EXE_stage.scala:39:19]
    .io_raddr1 (io_d_ebus_is_ebreak ? 5'hA : io_d_ebus_src1),	// @[playground/src/EXE_stage.scala:16:25]
    .io_raddr2 (io_d_ebus_is_ebreak ? 5'h0 : io_d_ebus_src2),	// @[playground/src/EXE_stage.scala:17:25]
    .io_wen    (io_d_ebus_data_wen),
    .io_rdata1 (_RegFile_io_rdata1),
    .io_rdata2 (_RegFile_io_rdata2)
  );
  Alu alu (	// @[playground/src/EXE_stage.scala:23:18]
    .io_op
      ({3'h0,
        io_d_ebus_alu_op_8,
        io_d_ebus_alu_op_7,
        io_d_ebus_alu_op_6,
        io_d_ebus_alu_op_5,
        io_d_ebus_alu_op_4,
        io_d_ebus_alu_op_3,
        io_d_ebus_alu_op_2,
        io_d_ebus_alu_op_1,
        io_d_ebus_alu_op_0}),	// @[playground/src/EXE_stage.scala:29:33]
    .io_src1   (io_d_ebus_src1_is_pc ? io_pc : _RegFile_io_rdata1),	// @[playground/src/EXE_stage.scala:14:21, :24:19]
    .io_src2
      (io_d_ebus_src2_is_imm
         ? io_d_ebus_imm
         : io_d_ebus_src2_is_shamt_imm
             ? {26'h0, io_d_ebus_imm[5:0]}
             : io_d_ebus_src2_is_shamt_src
                 ? {26'h0, _RegFile_io_rdata2[5:0]}
                 : _RegFile_io_rdata2),	// @[playground/src/EXE_stage.scala:14:21, :25:19, :26:{20,62}, :27:{22,60}]
    .io_sign   (io_d_ebus_src_is_sign),
    .io_result (_alu_io_result)
  );
  assign io_result =
    io_d_ebus_result_is_imm
      ? io_d_ebus_imm
      : io_d_ebus_result_is_snpc ? io_d_ebus_snpc : _alu_io_result;	// @[<stdin>:573:3, playground/src/EXE_stage.scala:23:18, :34:17, :35:18]
  assign io_jalr_taget = {_jalr_tmp_T[31:1], 1'h0};	// @[<stdin>:573:3, playground/src/EXE_stage.scala:17:25, :36:29, :37:{21,30}]
  assign io_sram_valid = io_d_ebus_sram_valid;	// @[<stdin>:573:3]
  assign io_sram_wen = io_d_ebus_sram_wen;	// @[<stdin>:573:3]
  assign io_sram_wdata = {27'h0, io_d_ebus_src2};	// @[<stdin>:573:3, playground/src/EXE_stage.scala:39:19, :42:16]
  assign io_sram_wmask = io_d_ebus_wmask;	// @[<stdin>:573:3]
endmodule

