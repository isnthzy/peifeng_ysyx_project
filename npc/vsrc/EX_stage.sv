// Generated by CIRCT firtool-1.56.0
module EX_stage(	// @[<stdin>:1279:3]
  input         clock,	// @[<stdin>:1280:11]
                reset,	// @[<stdin>:1281:11]
                EX_IO_valid,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_dpic_bundle_id_inv_flag,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_pc_sel,	// @[playground/src/EX_stage.scala:6:12]
  input  [11:0] EX_IO_bits_csr_addr,	// @[playground/src/EX_stage.scala:6:12]
  input  [31:0] EX_IO_bits_csr_global_mtvec,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_csr_global_mepc,	// @[playground/src/EX_stage.scala:6:12]
  input         EX_IO_bits_b_taken,	// @[playground/src/EX_stage.scala:6:12]
  input  [3:0]  EX_IO_bits_st_type,	// @[playground/src/EX_stage.scala:6:12]
  input  [2:0]  EX_IO_bits_ld_type,	// @[playground/src/EX_stage.scala:6:12]
  input  [4:0]  EX_IO_bits_csr_cmd,	// @[playground/src/EX_stage.scala:6:12]
  input  [1:0]  EX_IO_bits_wb_sel,	// @[playground/src/EX_stage.scala:6:12]
  input  [3:0]  EX_IO_bits_br_type,	// @[playground/src/EX_stage.scala:6:12]
  input         EX_IO_bits_rf_wen,	// @[playground/src/EX_stage.scala:6:12]
  input  [4:0]  EX_IO_bits_rd,	// @[playground/src/EX_stage.scala:6:12]
  input  [3:0]  EX_IO_bits_alu_op,	// @[playground/src/EX_stage.scala:6:12]
  input  [31:0] EX_IO_bits_src1,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_src2,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_rdata1,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_rdata2,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_nextpc,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_pc,	// @[playground/src/EX_stage.scala:6:12]
                EX_IO_bits_inst,	// @[playground/src/EX_stage.scala:6:12]
  input         EX_to_ls_ready,	// @[playground/src/EX_stage.scala:6:12]
                EX_ar_ready,	// @[playground/src/EX_stage.scala:6:12]
                EX_aw_ready,	// @[playground/src/EX_stage.scala:6:12]
                EX_w_ready,	// @[playground/src/EX_stage.scala:6:12]
                EX_b_valid,	// @[playground/src/EX_stage.scala:6:12]
  input  [1:0]  EX_b_bits_resp,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_IO_ready,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_valid,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_csr_commit_wen,	// @[playground/src/EX_stage.scala:6:12]
  output [11:0] EX_to_ls_bits_csr_commit_waddr,	// @[playground/src/EX_stage.scala:6:12]
  output [31:0] EX_to_ls_bits_csr_commit_wdata,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_to_ls_bits_csr_commit_exception_wen,	// @[playground/src/EX_stage.scala:6:12]
  output [31:0] EX_to_ls_bits_csr_commit_exception_pc_wb,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_to_ls_bits_dpic_bundle_id_inv_flag,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_dpic_bundle_ex_func_flag,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_dpic_bundle_ex_is_jal,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_dpic_bundle_ex_is_ret,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_dpic_bundle_ex_is_rd0,	// @[playground/src/EX_stage.scala:6:12]
  output [2:0]  EX_to_ls_bits_dpic_bundle_ex_ld_type,	// @[playground/src/EX_stage.scala:6:12]
  output [3:0]  EX_to_ls_bits_dpic_bundle_ex_st_type,	// @[playground/src/EX_stage.scala:6:12]
  output [31:0] EX_to_ls_bits_dpic_bundle_ex_mem_addr,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_dpic_bundle_ex_st_data,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_to_ls_bits_ld_wen,	// @[playground/src/EX_stage.scala:6:12]
  output [2:0]  EX_to_ls_bits_ld_type,	// @[playground/src/EX_stage.scala:6:12]
  output [4:0]  EX_to_ls_bits_csr_cmd,	// @[playground/src/EX_stage.scala:6:12]
  output [1:0]  EX_to_ls_bits_wb_sel,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_to_ls_bits_rf_wen,	// @[playground/src/EX_stage.scala:6:12]
  output [4:0]  EX_to_ls_bits_rd,	// @[playground/src/EX_stage.scala:6:12]
  output [31:0] EX_to_ls_bits_result,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_nextpc,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_pc,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_ls_bits_inst,	// @[playground/src/EX_stage.scala:6:12]
  output [4:0]  EX_to_id_fw_addr,	// @[playground/src/EX_stage.scala:6:12]
  output [31:0] EX_to_id_fw_data,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_to_id_csr_ecpt_wen,	// @[playground/src/EX_stage.scala:6:12]
  output [31:0] EX_to_id_csr_ecpt_pc_wb,	// @[playground/src/EX_stage.scala:6:12]
  output [11:0] EX_to_id_csr_waddr,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_to_id_csr_wen,	// @[playground/src/EX_stage.scala:6:12]
  output [31:0] EX_to_id_csr_wdata,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_to_id_clog,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_id_flush,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_if_flush,	// @[playground/src/EX_stage.scala:6:12]
                EX_to_preif_epc_taken,	// @[playground/src/EX_stage.scala:6:12]
  output [31:0] EX_to_preif_epc_target,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_to_preif_Br_B_taken,	// @[playground/src/EX_stage.scala:6:12]
  output [31:0] EX_to_preif_Br_B_target,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_to_preif_flush,	// @[playground/src/EX_stage.scala:6:12]
                EX_ar_valid,	// @[playground/src/EX_stage.scala:6:12]
  output [31:0] EX_ar_bits_addr,	// @[playground/src/EX_stage.scala:6:12]
  output [2:0]  EX_ar_bits_prot,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_aw_valid,	// @[playground/src/EX_stage.scala:6:12]
  output [31:0] EX_aw_bits_addr,	// @[playground/src/EX_stage.scala:6:12]
  output [2:0]  EX_aw_bits_prot,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_w_valid,	// @[playground/src/EX_stage.scala:6:12]
  output [31:0] EX_w_bits_data,	// @[playground/src/EX_stage.scala:6:12]
  output [7:0]  EX_w_bits_strb,	// @[playground/src/EX_stage.scala:6:12]
  output        EX_b_ready	// @[playground/src/EX_stage.scala:6:12]
);

  wire        _Csr_alu_io_wen;	// @[playground/src/EX_stage.scala:173:21]
  wire [31:0] _Csr_alu_io_out;	// @[playground/src/EX_stage.scala:173:21]
  wire [31:0] _Alu_io_result;	// @[playground/src/EX_stage.scala:39:17]
  wire        ld_wen = |EX_IO_bits_ld_type;	// @[playground/src/EX_stage.scala:23:28, :26:30]
  wire        st_wen = |EX_IO_bits_st_type;	// @[playground/src/EX_stage.scala:24:28, :25:30]
  reg         ex_valid;	// @[playground/src/EX_stage.scala:29:33]
  wire        ex_clog;	// @[playground/src/EX_stage.scala:27:29]
  wire        ex_ready_go = ~ex_clog;	// @[playground/src/EX_stage.scala:25:30, :27:29, :30:33, :31:19]
  wire        _EX_IO_ready_output = ~ex_valid | ex_ready_go & EX_to_ls_ready;	// @[playground/src/EX_stage.scala:29:33, :30:33, :32:{18,28,43}]
  wire        _EX_to_preif_Br_B_taken_output = EX_IO_bits_b_taken & ex_valid;	// @[playground/src/EX_stage.scala:29:33, :45:45]
  wire        _EX_to_preif_epc_target_T = EX_IO_bits_csr_cmd == 5'h3;	// @[playground/src/EX_stage.scala:61:32]
  wire        _EX_to_preif_epc_target_T_1 = EX_IO_bits_csr_cmd == 5'h4;	// @[playground/src/EX_stage.scala:62:32]
  wire        to_flush =
    (_EX_to_preif_Br_B_taken_output | _EX_to_preif_epc_target_T
     | _EX_to_preif_epc_target_T_1) & ex_valid;	// @[playground/src/EX_stage.scala:29:33, :45:45, :61:32, :62:{10,32,46}]
  wire [31:0] ram_waddr =
    ({32{EX_IO_bits_st_type != 4'hF}} | 32'hFFFFFFFC) & _Alu_io_result;	// @[playground/src/EX_stage.scala:39:17, :79:{20,39,64}]
  wire [31:0] ram_raddr =
    ({32{EX_IO_bits_ld_type != 3'h1}} | 32'hFFFFFFFC) & _Alu_io_result;	// @[playground/src/EX_stage.scala:39:17, :79:64, :81:{20,39}]
  reg         arvalidReg;	// @[playground/src/EX_stage.scala:93:25]
  reg  [31:0] araddrReg;	// @[playground/src/EX_stage.scala:94:24]
  reg         ReadRequstState;	// @[playground/src/EX_stage.scala:97:30]
  wire        WaitReadIdle = ReadRequstState;	// @[playground/src/EX_stage.scala:87:34, :97:30]
  reg  [1:0]  WriteRequstState;	// @[playground/src/EX_stage.scala:127:31]
  reg         awvalidReg;	// @[playground/src/EX_stage.scala:128:25]
  reg  [31:0] awaddrReg;	// @[playground/src/EX_stage.scala:129:24]
  reg         wvalidReg;	// @[playground/src/EX_stage.scala:130:24]
  reg  [31:0] wdataReg;	// @[playground/src/EX_stage.scala:131:23]
  reg  [3:0]  wstrbReg;	// @[playground/src/EX_stage.scala:132:23]
  reg         breadyReg;	// @[playground/src/EX_stage.scala:133:24]
  wire        _EX_b_ready_output = breadyReg;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:133:24]
  wire        WaitWriteIdle = |WriteRequstState;	// @[playground/src/EX_stage.scala:86:35, :127:31, :158:35]
  wire        BrespFire = _EX_b_ready_output & EX_b_valid;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:89:31, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  assign ex_clog =
    (~EX_ar_ready & ld_wen | WaitWriteIdle & ~BrespFire | WaitWriteIdle & st_wen)
    & ex_valid;	// @[playground/src/EX_stage.scala:23:28, :24:28, :27:29, :29:33, :86:35, :89:31, :168:{14,26}, :169:{26,29}, :170:{10,26,36}]
  wire        _EX_to_id_csr_wen_output = _Csr_alu_io_wen & ex_valid;	// @[playground/src/EX_stage.scala:29:33, :173:21, :178:35]
  wire        _EX_to_id_csr_ecpt_wen_output = _EX_to_preif_epc_target_T_1 & ex_valid;	// @[playground/src/EX_stage.scala:29:33, :62:32, :180:58]
  wire        _EX_to_ls_bits_dpic_bundle_ex_is_jal_T = EX_IO_bits_br_type == 4'h7;	// @[playground/src/EX_stage.scala:208:62]
  wire        _GEN = ReadRequstState & EX_ar_ready;	// @[playground/src/EX_stage.scala:97:30, :112:46, :113:22, :114:22]
  wire        _GEN_0 = WriteRequstState == 2'h2 & breadyReg & EX_b_valid;	// @[playground/src/EX_stage.scala:127:31, :133:24, :147:23, :152:{30,47}, :153:32, :154:23]
  wire        _GEN_1 = WriteRequstState == 2'h1;	// @[playground/src/EX_stage.scala:127:31, :137:23, :145:30]
  wire        _GEN_2 = EX_aw_ready & EX_w_ready;	// @[playground/src/EX_stage.scala:146:21]
  wire        _GEN_3 = _GEN_1 & _GEN_2;	// @[playground/src/EX_stage.scala:128:25, :145:{30,47}, :146:{21,34}, :148:17]
  wire        _GEN_4 = ld_wen & ex_valid;	// @[playground/src/EX_stage.scala:23:28, :29:33, :99:16]
  wire        _GEN_5 = ~WaitWriteIdle | BrespFire;	// @[playground/src/EX_stage.scala:86:35, :89:31, :100:26, :101:24, :107:24]
  wire        _GEN_6 = WriteRequstState == 2'h0;	// @[playground/src/EX_stage.scala:127:31, :135:24]
  wire        _GEN_7 = st_wen & ex_valid;	// @[playground/src/EX_stage.scala:24:28, :29:33, :136:16]
  wire        _GEN_8 = _GEN_4 & _GEN_5;	// @[playground/src/EX_stage.scala:97:30, :99:{16,27}, :100:26, :101:24, :107:24]
  always @(posedge clock) begin	// @[<stdin>:1280:11]
    if (reset) begin	// @[<stdin>:1280:11]
      ex_valid <= 1'h0;	// @[playground/src/EX_stage.scala:25:30, :29:33]
      arvalidReg <= 1'h0;	// @[playground/src/EX_stage.scala:25:30, :93:25]
      araddrReg <= 32'h0;	// @[playground/src/EX_stage.scala:94:24]
      ReadRequstState <= 1'h0;	// @[playground/src/EX_stage.scala:25:30, :97:30]
      WriteRequstState <= 2'h0;	// @[playground/src/EX_stage.scala:127:31]
      awvalidReg <= 1'h0;	// @[playground/src/EX_stage.scala:25:30, :128:25]
      awaddrReg <= 32'h0;	// @[playground/src/EX_stage.scala:94:24, :129:24]
      wvalidReg <= 1'h0;	// @[playground/src/EX_stage.scala:25:30, :130:24]
      wdataReg <= 32'h0;	// @[playground/src/EX_stage.scala:94:24, :131:23]
      wstrbReg <= 4'h0;	// @[playground/src/EX_stage.scala:46:61, :132:23]
      breadyReg <= 1'h0;	// @[playground/src/EX_stage.scala:25:30, :133:24]
    end
    else begin	// @[<stdin>:1280:11]
      if (_EX_IO_ready_output)	// @[playground/src/EX_stage.scala:32:28]
        ex_valid <= EX_IO_valid;	// @[playground/src/EX_stage.scala:29:33]
      if (ReadRequstState) begin	// @[playground/src/EX_stage.scala:97:30]
        arvalidReg <= ~_GEN & arvalidReg;	// @[playground/src/EX_stage.scala:93:25, :97:30, :112:46, :113:22, :114:22, :115:17]
        ReadRequstState <= ~_GEN & ReadRequstState;	// @[playground/src/EX_stage.scala:97:30, :112:46, :113:22, :114:22]
      end
      else begin	// @[playground/src/EX_stage.scala:97:30]
        arvalidReg <= _GEN_8 | arvalidReg;	// @[playground/src/EX_stage.scala:93:25, :97:30, :99:27, :100:26]
        ReadRequstState <= _GEN_8 | ReadRequstState;	// @[playground/src/EX_stage.scala:97:30, :99:27, :100:26]
      end
      if (~ReadRequstState & _GEN_4 & _GEN_5)	// @[playground/src/EX_stage.scala:94:24, :97:30, :98:{23,34}, :99:{16,27}, :100:26, :101:24, :107:24]
        araddrReg <= ram_raddr;	// @[playground/src/EX_stage.scala:81:20, :94:24]
      if (_GEN_6) begin	// @[playground/src/EX_stage.scala:135:24]
        if (_GEN_7)	// @[playground/src/EX_stage.scala:136:16]
          WriteRequstState <= 2'h1;	// @[playground/src/EX_stage.scala:127:31, :137:23]
        awvalidReg <= _GEN_7 | awvalidReg;	// @[playground/src/EX_stage.scala:128:25, :136:{16,27}, :138:17]
        wvalidReg <= _GEN_7 | wvalidReg;	// @[playground/src/EX_stage.scala:130:24, :136:{16,27}, :141:16]
      end
      else begin	// @[playground/src/EX_stage.scala:135:24]
        if (_GEN_1) begin	// @[playground/src/EX_stage.scala:145:30]
          if (_GEN_2)	// @[playground/src/EX_stage.scala:146:21]
            WriteRequstState <= 2'h2;	// @[playground/src/EX_stage.scala:127:31, :147:23]
          breadyReg <= _GEN_2 | breadyReg;	// @[playground/src/EX_stage.scala:133:24, :146:{21,34}, :150:16]
        end
        else begin	// @[playground/src/EX_stage.scala:145:30]
          if (_GEN_0)	// @[playground/src/EX_stage.scala:127:31, :152:47, :153:32, :154:23]
            WriteRequstState <= 2'h0;	// @[playground/src/EX_stage.scala:127:31]
          breadyReg <= ~_GEN_0 & breadyReg;	// @[playground/src/EX_stage.scala:127:31, :133:24, :152:47, :153:32, :154:23, :155:16]
        end
        awvalidReg <= ~_GEN_3 & awvalidReg;	// @[playground/src/EX_stage.scala:128:25, :145:47, :146:34, :148:17]
        wvalidReg <= ~_GEN_3 & wvalidReg;	// @[playground/src/EX_stage.scala:128:25, :130:24, :145:47, :146:34, :148:17, :149:16]
      end
      if (_GEN_6 & _GEN_7) begin	// @[playground/src/EX_stage.scala:129:24, :135:{24,35}, :136:{16,27}, :139:16]
        awaddrReg <= ram_waddr;	// @[playground/src/EX_stage.scala:79:20, :129:24]
        wdataReg <= EX_IO_bits_rdata2;	// @[playground/src/EX_stage.scala:131:23]
        wstrbReg <= EX_IO_bits_st_type;	// @[playground/src/EX_stage.scala:132:23]
      end
    end
  end // always @(posedge)
  Alu Alu (	// @[playground/src/EX_stage.scala:39:17]
    .io_op     (EX_IO_bits_alu_op),
    .io_src1   (EX_IO_bits_src1),
    .io_src2   (EX_IO_bits_src2),
    .io_result (_Alu_io_result)
  );
  Csr_alu Csr_alu (	// @[playground/src/EX_stage.scala:173:21]
    .io_csr_cmd   (EX_IO_bits_csr_cmd),
    .io_in_csr    (_Alu_io_result),	// @[playground/src/EX_stage.scala:39:17]
    .io_in_rdata1 (EX_IO_bits_rdata1),
    .io_wen       (_Csr_alu_io_wen),
    .io_out       (_Csr_alu_io_out)
  );
  assign EX_IO_ready = _EX_IO_ready_output;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:32:28]
  assign EX_to_ls_valid = ex_valid & ex_ready_go;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:29:33, :30:33, :36:28]
  assign EX_to_ls_bits_csr_commit_wen = _EX_to_id_csr_wen_output;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:178:35]
  assign EX_to_ls_bits_csr_commit_waddr = EX_IO_bits_csr_addr;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_csr_commit_wdata = _Csr_alu_io_out;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:173:21]
  assign EX_to_ls_bits_csr_commit_exception_wen = _EX_to_id_csr_ecpt_wen_output;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:180:58]
  assign EX_to_ls_bits_csr_commit_exception_pc_wb = EX_IO_bits_pc;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_dpic_bundle_id_inv_flag = EX_IO_bits_dpic_bundle_id_inv_flag;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_dpic_bundle_ex_func_flag =
    _EX_to_ls_bits_dpic_bundle_ex_is_jal_T | EX_IO_bits_br_type == 4'h8;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:208:{62,72,92}]
  assign EX_to_ls_bits_dpic_bundle_ex_is_jal = _EX_to_ls_bits_dpic_bundle_ex_is_jal_T;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:208:62]
  assign EX_to_ls_bits_dpic_bundle_ex_is_ret = EX_IO_bits_inst == 32'h8067;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:210:55]
  assign EX_to_ls_bits_dpic_bundle_ex_is_rd0 = EX_IO_bits_rd == 5'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:75:24, :211:53]
  assign EX_to_ls_bits_dpic_bundle_ex_ld_type = EX_IO_bits_ld_type;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_dpic_bundle_ex_st_type = EX_IO_bits_st_type;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_dpic_bundle_ex_mem_addr =
    st_wen ? ram_waddr : ld_wen ? ram_raddr : 32'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:23:28, :24:28, :79:20, :81:20, :94:24, :215:45, :216:50]
  assign EX_to_ls_bits_dpic_bundle_ex_st_data = st_wen ? EX_IO_bits_rdata2 : 32'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:24:28, :94:24, :217:44]
  assign EX_to_ls_bits_ld_wen = ld_wen;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:23:28]
  assign EX_to_ls_bits_ld_type = EX_IO_bits_ld_type;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_csr_cmd = EX_IO_bits_csr_cmd;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_wb_sel = EX_IO_bits_wb_sel;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_rf_wen = EX_IO_bits_rf_wen;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_rd = EX_IO_bits_rd;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_result = _Alu_io_result;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:39:17]
  assign EX_to_ls_bits_nextpc = EX_IO_bits_nextpc;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_pc = EX_IO_bits_pc;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_inst = EX_IO_bits_inst;	// @[<stdin>:1279:3]
  assign EX_to_id_fw_addr = ex_valid & EX_IO_bits_rf_wen ? EX_IO_bits_rd : 5'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:29:33, :75:{24,34}]
  assign EX_to_id_fw_data = _Alu_io_result;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:39:17]
  assign EX_to_id_csr_ecpt_wen = _EX_to_id_csr_ecpt_wen_output;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:180:58]
  assign EX_to_id_csr_ecpt_pc_wb = EX_IO_bits_pc;	// @[<stdin>:1279:3]
  assign EX_to_id_csr_waddr = EX_IO_bits_csr_addr;	// @[<stdin>:1279:3]
  assign EX_to_id_csr_wen = _EX_to_id_csr_wen_output;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:178:35]
  assign EX_to_id_csr_wdata = _Csr_alu_io_out;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:173:21]
  assign EX_to_id_clog = (|EX_IO_bits_ld_type) & ex_valid;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:26:30, :29:33, :71:44]
  assign EX_to_id_flush = to_flush;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:62:46]
  assign EX_to_if_flush = to_flush;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:62:46]
  assign EX_to_preif_epc_taken = EX_IO_bits_pc_sel & ex_valid;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:29:33, :184:54]
  assign EX_to_preif_epc_target =
    _EX_to_preif_epc_target_T
      ? EX_IO_bits_csr_global_mepc
      : _EX_to_preif_epc_target_T_1 ? EX_IO_bits_csr_global_mtvec : 32'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:61:32, :62:32, :94:24, :185:30, :186:28]
  assign EX_to_preif_Br_B_taken = _EX_to_preif_Br_B_taken_output;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:45:45]
  assign EX_to_preif_Br_B_target =
    EX_IO_bits_br_type == 4'h6 | EX_IO_bits_br_type == 4'h5 | EX_IO_bits_br_type == 4'h4
    | EX_IO_bits_br_type == 4'h3 | EX_IO_bits_br_type == 4'h2 | EX_IO_bits_br_type == 4'h1
      ? _Alu_io_result
      : 32'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:39:17, :46:61, :94:24]
  assign EX_to_preif_flush = to_flush;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:62:46]
  assign EX_ar_valid = arvalidReg;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:93:25]
  assign EX_ar_bits_addr = araddrReg;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:94:24]
  assign EX_ar_bits_prot = 3'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:121:18]
  assign EX_aw_valid = awvalidReg;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:128:25]
  assign EX_aw_bits_addr = awaddrReg;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:129:24]
  assign EX_aw_bits_prot = 3'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:121:18]
  assign EX_w_valid = wvalidReg;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:130:24]
  assign EX_w_bits_data = wdataReg;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:131:23]
  assign EX_w_bits_strb = {4'h0, wstrbReg};	// @[<stdin>:1279:3, playground/src/EX_stage.scala:46:61, :132:23, :165:17]
  assign EX_b_ready = _EX_b_ready_output;	// @[<stdin>:1279:3]
endmodule

