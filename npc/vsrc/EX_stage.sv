// Generated by CIRCT firtool-1.56.0
module EX_stage(	// @[<stdin>:1279:3]
  input         clock,	// @[<stdin>:1280:11]
                reset,	// @[<stdin>:1281:11]
                EX_IO_valid,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_dpic_bundle_id_inv_flag,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_pc_sel,	// @[playground/src/EX_stage.scala:7:12]
  input  [11:0] EX_IO_bits_csr_addr,	// @[playground/src/EX_stage.scala:7:12]
  input  [31:0] EX_IO_bits_csr_global_mtvec,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_csr_global_mepc,	// @[playground/src/EX_stage.scala:7:12]
  input         EX_IO_bits_b_taken,	// @[playground/src/EX_stage.scala:7:12]
  input  [2:0]  EX_IO_bits_st_type,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_ld_type,	// @[playground/src/EX_stage.scala:7:12]
  input  [4:0]  EX_IO_bits_csr_cmd,	// @[playground/src/EX_stage.scala:7:12]
  input  [1:0]  EX_IO_bits_wb_sel,	// @[playground/src/EX_stage.scala:7:12]
  input  [3:0]  EX_IO_bits_br_type,	// @[playground/src/EX_stage.scala:7:12]
  input         EX_IO_bits_rf_wen,	// @[playground/src/EX_stage.scala:7:12]
  input  [4:0]  EX_IO_bits_rd,	// @[playground/src/EX_stage.scala:7:12]
  input  [3:0]  EX_IO_bits_alu_op,	// @[playground/src/EX_stage.scala:7:12]
  input  [31:0] EX_IO_bits_src1,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_src2,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_rdata1,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_rdata2,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_nextpc,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_pc,	// @[playground/src/EX_stage.scala:7:12]
                EX_IO_bits_inst,	// @[playground/src/EX_stage.scala:7:12]
  input         EX_to_ls_ready,	// @[playground/src/EX_stage.scala:7:12]
                EX_for_ls_WaitloadOk,	// @[playground/src/EX_stage.scala:7:12]
                EX_ar_ready,	// @[playground/src/EX_stage.scala:7:12]
                EX_aw_ready,	// @[playground/src/EX_stage.scala:7:12]
                EX_w_ready,	// @[playground/src/EX_stage.scala:7:12]
                EX_b_valid,	// @[playground/src/EX_stage.scala:7:12]
  input  [1:0]  EX_b_bits_resp,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_IO_ready,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_valid,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_csr_commit_wen,	// @[playground/src/EX_stage.scala:7:12]
  output [11:0] EX_to_ls_bits_csr_commit_waddr,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_ls_bits_csr_commit_wdata,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_ls_bits_csr_commit_exception_wen,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_ls_bits_csr_commit_exception_pc_wb,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_ls_bits_dpic_bundle_id_inv_flag,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_dpic_bundle_ex_func_flag,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_dpic_bundle_ex_is_jal,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_dpic_bundle_ex_is_ret,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_dpic_bundle_ex_is_rd0,	// @[playground/src/EX_stage.scala:7:12]
  output [2:0]  EX_to_ls_bits_dpic_bundle_ex_ld_type,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_dpic_bundle_ex_st_type,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_ls_bits_dpic_bundle_ex_mem_addr,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_dpic_bundle_ex_st_data,	// @[playground/src/EX_stage.scala:7:12]
  output [1:0]  EX_to_ls_bits_addr_low2bit,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_ls_bits_ld_wen,	// @[playground/src/EX_stage.scala:7:12]
  output [2:0]  EX_to_ls_bits_ld_type,	// @[playground/src/EX_stage.scala:7:12]
  output [4:0]  EX_to_ls_bits_csr_cmd,	// @[playground/src/EX_stage.scala:7:12]
  output [1:0]  EX_to_ls_bits_wb_sel,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_ls_bits_rf_wen,	// @[playground/src/EX_stage.scala:7:12]
  output [4:0]  EX_to_ls_bits_rd,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_ls_bits_result,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_nextpc,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_pc,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_ls_bits_inst,	// @[playground/src/EX_stage.scala:7:12]
  output [4:0]  EX_to_id_fw_addr,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_id_fw_data,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_id_csr_ecpt_wen,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_id_csr_ecpt_pc_wb,	// @[playground/src/EX_stage.scala:7:12]
  output [11:0] EX_to_id_csr_waddr,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_id_csr_wen,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_id_csr_wdata,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_id_clog,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_id_flush,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_if_flush,	// @[playground/src/EX_stage.scala:7:12]
                EX_to_preif_epc_taken,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_preif_epc_target,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_preif_Br_B_taken,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_to_preif_Br_B_target,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_to_preif_flush,	// @[playground/src/EX_stage.scala:7:12]
                EX_ar_valid,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_ar_bits_addr,	// @[playground/src/EX_stage.scala:7:12]
  output [2:0]  EX_ar_bits_prot,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_aw_valid,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_aw_bits_addr,	// @[playground/src/EX_stage.scala:7:12]
  output [2:0]  EX_aw_bits_prot,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_w_valid,	// @[playground/src/EX_stage.scala:7:12]
  output [31:0] EX_w_bits_data,	// @[playground/src/EX_stage.scala:7:12]
  output [7:0]  EX_w_bits_strb,	// @[playground/src/EX_stage.scala:7:12]
  output        EX_b_ready	// @[playground/src/EX_stage.scala:7:12]
);

  wire        _Csr_alu_io_wen;	// @[playground/src/EX_stage.scala:192:21]
  wire [31:0] _Csr_alu_io_out;	// @[playground/src/EX_stage.scala:192:21]
  wire [31:0] _Alu_io_result;	// @[playground/src/EX_stage.scala:41:17]
  wire        ld_wen = |EX_IO_bits_ld_type;	// @[playground/src/EX_stage.scala:25:28, :28:30]
  wire        st_wen = |EX_IO_bits_st_type;	// @[playground/src/EX_stage.scala:26:28, :27:30]
  reg         ex_valid;	// @[playground/src/EX_stage.scala:31:33]
  wire        ex_clog;	// @[playground/src/EX_stage.scala:29:29]
  wire        ex_ready_go = ~ex_clog;	// @[playground/src/EX_stage.scala:27:30, :29:29, :32:33, :33:19]
  wire        _EX_IO_ready_output = ~ex_valid | ex_ready_go & EX_to_ls_ready;	// @[playground/src/EX_stage.scala:31:33, :32:33, :34:{18,28,43}]
  wire        _EX_to_preif_Br_B_taken_output = EX_IO_bits_b_taken & ex_valid;	// @[playground/src/EX_stage.scala:31:33, :47:45]
  wire        _EX_to_preif_epc_target_T = EX_IO_bits_csr_cmd == 5'h3;	// @[playground/src/EX_stage.scala:63:32]
  wire        _EX_to_preif_epc_target_T_1 = EX_IO_bits_csr_cmd == 5'h4;	// @[playground/src/EX_stage.scala:64:32]
  wire        to_flush =
    (_EX_to_preif_Br_B_taken_output | _EX_to_preif_epc_target_T
     | _EX_to_preif_epc_target_T_1) & ex_valid;	// @[playground/src/EX_stage.scala:31:33, :47:45, :63:32, :64:{10,32,46}]
  reg         arvalidReg;	// @[playground/src/EX_stage.scala:110:25]
  reg  [31:0] araddrReg;	// @[playground/src/EX_stage.scala:111:24]
  reg         ReadRequstState;	// @[playground/src/EX_stage.scala:114:30]
  wire        WaitReadIdle = ReadRequstState;	// @[playground/src/EX_stage.scala:104:34, :114:30]
  reg  [1:0]  WriteRequstState;	// @[playground/src/EX_stage.scala:144:31]
  reg         awvalidReg;	// @[playground/src/EX_stage.scala:145:25]
  reg  [31:0] awaddrReg;	// @[playground/src/EX_stage.scala:146:24]
  reg         wvalidReg;	// @[playground/src/EX_stage.scala:147:24]
  reg  [31:0] wdataReg;	// @[playground/src/EX_stage.scala:148:23]
  reg  [3:0]  wstrbReg;	// @[playground/src/EX_stage.scala:149:23]
  reg         breadyReg;	// @[playground/src/EX_stage.scala:150:24]
  wire        _EX_b_ready_output = breadyReg;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:150:24]
  wire        WaitWriteIdle = |WriteRequstState;	// @[playground/src/EX_stage.scala:103:35, :144:31, :176:35]
  wire        BrespFire = _EX_b_ready_output & EX_b_valid;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:106:31, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  assign ex_clog =
    (~EX_ar_ready & ld_wen | EX_for_ls_WaitloadOk & ld_wen | WaitWriteIdle & ~BrespFire
     | WaitWriteIdle & st_wen) & ex_valid;	// @[playground/src/EX_stage.scala:25:28, :26:28, :29:29, :31:33, :103:35, :106:31, :186:{14,26}, :187:33, :188:{26,29}, :189:{10,26,36}]
  wire        _EX_to_id_csr_wen_output = _Csr_alu_io_wen & ex_valid;	// @[playground/src/EX_stage.scala:31:33, :192:21, :197:35]
  wire        _EX_to_id_csr_ecpt_wen_output = _EX_to_preif_epc_target_T_1 & ex_valid;	// @[playground/src/EX_stage.scala:31:33, :64:32, :199:58]
  wire        _EX_to_ls_bits_dpic_bundle_ex_is_jal_T = EX_IO_bits_br_type == 4'h7;	// @[playground/src/EX_stage.scala:227:62]
  wire        _GEN = ReadRequstState & EX_ar_ready;	// @[playground/src/EX_stage.scala:114:30, :129:46, :130:22, :131:22]
  wire        _GEN_0 = WriteRequstState == 2'h2 & breadyReg & EX_b_valid;	// @[playground/src/EX_stage.scala:82:49, :144:31, :150:24, :170:{30,47}, :171:32, :172:23]
  wire        _GEN_1 = WriteRequstState == 2'h1;	// @[playground/src/EX_stage.scala:144:31, :155:23, :163:30]
  wire        _GEN_2 = EX_aw_ready & EX_w_ready;	// @[playground/src/EX_stage.scala:164:21]
  wire        _GEN_3 = _GEN_1 & _GEN_2;	// @[playground/src/EX_stage.scala:145:25, :163:{30,47}, :164:{21,34}, :166:17]
  wire        _store_half_sel_T_2 = _Alu_io_result[1:0] == 2'h2;	// @[playground/src/EX_stage.scala:41:17, :81:33, :82:49]
  wire [31:0] ram_addr = _Alu_io_result & 32'hFFFFFFFC;	// @[playground/src/EX_stage.scala:41:17, :94:{30,32}]
  wire        _GEN_4 = ld_wen & ex_valid;	// @[playground/src/EX_stage.scala:25:28, :31:33, :116:16]
  wire        _GEN_5 = ~WaitWriteIdle | BrespFire;	// @[playground/src/EX_stage.scala:103:35, :106:31, :117:26, :118:24, :124:24]
  wire        _GEN_6 = WriteRequstState == 2'h0;	// @[playground/src/EX_stage.scala:144:31, :152:24]
  wire        _GEN_7 = st_wen & ex_valid;	// @[playground/src/EX_stage.scala:26:28, :31:33, :153:16]
  wire        _GEN_8 = _GEN_4 & _GEN_5;	// @[playground/src/EX_stage.scala:114:30, :116:{16,27}, :117:26, :118:24, :124:24]
  always @(posedge clock) begin	// @[<stdin>:1280:11]
    if (reset) begin	// @[<stdin>:1280:11]
      ex_valid <= 1'h0;	// @[playground/src/EX_stage.scala:27:30, :31:33]
      arvalidReg <= 1'h0;	// @[playground/src/EX_stage.scala:27:30, :110:25]
      araddrReg <= 32'h0;	// @[playground/src/EX_stage.scala:111:24]
      ReadRequstState <= 1'h0;	// @[playground/src/EX_stage.scala:27:30, :114:30]
      WriteRequstState <= 2'h0;	// @[playground/src/EX_stage.scala:144:31]
      awvalidReg <= 1'h0;	// @[playground/src/EX_stage.scala:27:30, :145:25]
      awaddrReg <= 32'h0;	// @[playground/src/EX_stage.scala:111:24, :146:24]
      wvalidReg <= 1'h0;	// @[playground/src/EX_stage.scala:27:30, :147:24]
      wdataReg <= 32'h0;	// @[playground/src/EX_stage.scala:111:24, :148:23]
      wstrbReg <= 4'h0;	// @[playground/src/EX_stage.scala:48:61, :149:23]
      breadyReg <= 1'h0;	// @[playground/src/EX_stage.scala:27:30, :150:24]
    end
    else begin	// @[<stdin>:1280:11]
      if (_EX_IO_ready_output)	// @[playground/src/EX_stage.scala:34:28]
        ex_valid <= EX_IO_valid;	// @[playground/src/EX_stage.scala:31:33]
      if (ReadRequstState) begin	// @[playground/src/EX_stage.scala:114:30]
        arvalidReg <= ~_GEN & arvalidReg;	// @[playground/src/EX_stage.scala:110:25, :114:30, :129:46, :130:22, :131:22, :132:17]
        ReadRequstState <= ~_GEN & ReadRequstState;	// @[playground/src/EX_stage.scala:114:30, :129:46, :130:22, :131:22]
      end
      else begin	// @[playground/src/EX_stage.scala:114:30]
        arvalidReg <= _GEN_8 | arvalidReg;	// @[playground/src/EX_stage.scala:110:25, :114:30, :116:27, :117:26]
        ReadRequstState <= _GEN_8 | ReadRequstState;	// @[playground/src/EX_stage.scala:114:30, :116:27, :117:26]
      end
      if (~ReadRequstState & _GEN_4 & _GEN_5)	// @[playground/src/EX_stage.scala:111:24, :114:30, :115:{23,34}, :116:{16,27}, :117:26, :118:24, :124:24]
        araddrReg <= ram_addr;	// @[playground/src/EX_stage.scala:94:30, :111:24]
      if (_GEN_6) begin	// @[playground/src/EX_stage.scala:152:24]
        if (_GEN_7)	// @[playground/src/EX_stage.scala:153:16]
          WriteRequstState <= 2'h1;	// @[playground/src/EX_stage.scala:144:31, :155:23]
        awvalidReg <= _GEN_7 | awvalidReg;	// @[playground/src/EX_stage.scala:145:25, :153:{16,27}, :156:17]
        wvalidReg <= _GEN_7 | wvalidReg;	// @[playground/src/EX_stage.scala:147:24, :153:{16,27}, :159:16]
      end
      else begin	// @[playground/src/EX_stage.scala:152:24]
        if (_GEN_1) begin	// @[playground/src/EX_stage.scala:163:30]
          if (_GEN_2)	// @[playground/src/EX_stage.scala:164:21]
            WriteRequstState <= 2'h2;	// @[playground/src/EX_stage.scala:82:49, :144:31]
          breadyReg <= _GEN_2 | breadyReg;	// @[playground/src/EX_stage.scala:150:24, :164:{21,34}, :168:16]
        end
        else begin	// @[playground/src/EX_stage.scala:163:30]
          if (_GEN_0)	// @[playground/src/EX_stage.scala:144:31, :170:47, :171:32, :172:23]
            WriteRequstState <= 2'h0;	// @[playground/src/EX_stage.scala:144:31]
          breadyReg <= ~_GEN_0 & breadyReg;	// @[playground/src/EX_stage.scala:144:31, :150:24, :170:47, :171:32, :172:23, :173:16]
        end
        awvalidReg <= ~_GEN_3 & awvalidReg;	// @[playground/src/EX_stage.scala:145:25, :163:47, :164:34, :166:17]
        wvalidReg <= ~_GEN_3 & wvalidReg;	// @[playground/src/EX_stage.scala:145:25, :147:24, :163:47, :164:34, :166:17, :167:16]
      end
      if (_GEN_6 & _GEN_7) begin	// @[playground/src/EX_stage.scala:146:24, :152:{24,35}, :153:{16,27}, :157:16]
        awaddrReg <= ram_addr;	// @[playground/src/EX_stage.scala:94:30, :146:24]
        wdataReg <= EX_IO_bits_rdata2;	// @[playground/src/EX_stage.scala:148:23]
        if (EX_IO_bits_st_type == 3'h3)	// @[playground/src/EX_stage.scala:95:46]
          wstrbReg <= 4'hF;	// @[playground/src/EX_stage.scala:95:46, :149:23]
        else if (EX_IO_bits_st_type == 3'h2) begin	// @[playground/src/EX_stage.scala:95:46]
          if ((&(_Alu_io_result[1:0])) | _store_half_sel_T_2)	// @[playground/src/EX_stage.scala:41:17, :81:33, :82:49, :88:49]
            wstrbReg <= 4'hC;	// @[playground/src/EX_stage.scala:88:49, :149:23]
          else	// @[playground/src/EX_stage.scala:88:49]
            wstrbReg <= 4'h3;	// @[playground/src/EX_stage.scala:48:61, :149:23]
        end
        else if (EX_IO_bits_st_type == 3'h1) begin	// @[playground/src/EX_stage.scala:95:46]
          if (&(_Alu_io_result[1:0]))	// @[playground/src/EX_stage.scala:41:17, :81:33, :82:49]
            wstrbReg <= 4'h8;	// @[playground/src/EX_stage.scala:82:49, :149:23]
          else	// @[playground/src/EX_stage.scala:82:49]
            wstrbReg <=
              {1'h0,
               _store_half_sel_T_2
                 ? 3'h4
                 : {1'h0, _Alu_io_result[1:0] == 2'h1 ? 2'h2 : 2'h1}};	// @[playground/src/EX_stage.scala:27:30, :41:17, :81:33, :82:49, :149:23, :155:23]
        end
        else	// @[playground/src/EX_stage.scala:95:46]
          wstrbReg <= 4'h0;	// @[playground/src/EX_stage.scala:48:61, :149:23]
      end
    end
  end // always @(posedge)
  Alu Alu (	// @[playground/src/EX_stage.scala:41:17]
    .io_op     (EX_IO_bits_alu_op),
    .io_src1   (EX_IO_bits_src1),
    .io_src2   (EX_IO_bits_src2),
    .io_result (_Alu_io_result)
  );
  Csr_alu Csr_alu (	// @[playground/src/EX_stage.scala:192:21]
    .io_csr_cmd   (EX_IO_bits_csr_cmd),
    .io_in_csr    (_Alu_io_result),	// @[playground/src/EX_stage.scala:41:17]
    .io_in_rdata1 (EX_IO_bits_rdata1),
    .io_wen       (_Csr_alu_io_wen),
    .io_out       (_Csr_alu_io_out)
  );
  assign EX_IO_ready = _EX_IO_ready_output;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:34:28]
  assign EX_to_ls_valid = ex_valid & ex_ready_go;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:31:33, :32:33, :38:28]
  assign EX_to_ls_bits_csr_commit_wen = _EX_to_id_csr_wen_output;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:197:35]
  assign EX_to_ls_bits_csr_commit_waddr = EX_IO_bits_csr_addr;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_csr_commit_wdata = _Csr_alu_io_out;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:192:21]
  assign EX_to_ls_bits_csr_commit_exception_wen = _EX_to_id_csr_ecpt_wen_output;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:199:58]
  assign EX_to_ls_bits_csr_commit_exception_pc_wb = EX_IO_bits_pc;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_dpic_bundle_id_inv_flag = EX_IO_bits_dpic_bundle_id_inv_flag;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_dpic_bundle_ex_func_flag =
    _EX_to_ls_bits_dpic_bundle_ex_is_jal_T | EX_IO_bits_br_type == 4'h8;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:82:49, :227:{62,72,92}]
  assign EX_to_ls_bits_dpic_bundle_ex_is_jal = _EX_to_ls_bits_dpic_bundle_ex_is_jal_T;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:227:62]
  assign EX_to_ls_bits_dpic_bundle_ex_is_ret = EX_IO_bits_inst == 32'h8067;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:229:55]
  assign EX_to_ls_bits_dpic_bundle_ex_is_rd0 = EX_IO_bits_rd == 5'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:77:24, :230:53]
  assign EX_to_ls_bits_dpic_bundle_ex_ld_type = EX_IO_bits_ld_type;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_dpic_bundle_ex_st_type = EX_IO_bits_st_type;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_dpic_bundle_ex_mem_addr = st_wen | ld_wen ? _Alu_io_result : 32'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:25:28, :26:28, :41:17, :111:24, :234:{45,53}]
  assign EX_to_ls_bits_dpic_bundle_ex_st_data = st_wen ? EX_IO_bits_rdata2 : 32'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:26:28, :111:24, :235:44]
  assign EX_to_ls_bits_addr_low2bit = _Alu_io_result[1:0];	// @[<stdin>:1279:3, playground/src/EX_stage.scala:41:17, :81:33]
  assign EX_to_ls_bits_ld_wen = ld_wen;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:25:28]
  assign EX_to_ls_bits_ld_type = EX_IO_bits_ld_type;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_csr_cmd = EX_IO_bits_csr_cmd;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_wb_sel = EX_IO_bits_wb_sel;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_rf_wen = EX_IO_bits_rf_wen;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_rd = EX_IO_bits_rd;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_result = _Alu_io_result;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:41:17]
  assign EX_to_ls_bits_nextpc = EX_IO_bits_nextpc;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_pc = EX_IO_bits_pc;	// @[<stdin>:1279:3]
  assign EX_to_ls_bits_inst = EX_IO_bits_inst;	// @[<stdin>:1279:3]
  assign EX_to_id_fw_addr = ex_valid & EX_IO_bits_rf_wen ? EX_IO_bits_rd : 5'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:31:33, :77:{24,34}]
  assign EX_to_id_fw_data = _Alu_io_result;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:41:17]
  assign EX_to_id_csr_ecpt_wen = _EX_to_id_csr_ecpt_wen_output;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:199:58]
  assign EX_to_id_csr_ecpt_pc_wb = EX_IO_bits_pc;	// @[<stdin>:1279:3]
  assign EX_to_id_csr_waddr = EX_IO_bits_csr_addr;	// @[<stdin>:1279:3]
  assign EX_to_id_csr_wen = _EX_to_id_csr_wen_output;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:197:35]
  assign EX_to_id_csr_wdata = _Csr_alu_io_out;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:192:21]
  assign EX_to_id_clog = (|EX_IO_bits_ld_type) & ex_valid;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:28:30, :31:33, :73:44]
  assign EX_to_id_flush = to_flush;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:64:46]
  assign EX_to_if_flush = to_flush;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:64:46]
  assign EX_to_preif_epc_taken = EX_IO_bits_pc_sel & ex_valid;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:31:33, :203:54]
  assign EX_to_preif_epc_target =
    _EX_to_preif_epc_target_T
      ? EX_IO_bits_csr_global_mepc
      : _EX_to_preif_epc_target_T_1 ? EX_IO_bits_csr_global_mtvec : 32'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:63:32, :64:32, :111:24, :204:30, :205:28]
  assign EX_to_preif_Br_B_taken = _EX_to_preif_Br_B_taken_output;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:47:45]
  assign EX_to_preif_Br_B_target =
    EX_IO_bits_br_type == 4'h6 | EX_IO_bits_br_type == 4'h5 | EX_IO_bits_br_type == 4'h4
    | EX_IO_bits_br_type == 4'h3 | EX_IO_bits_br_type == 4'h2 | EX_IO_bits_br_type == 4'h1
      ? _Alu_io_result
      : 32'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:41:17, :48:61, :111:24]
  assign EX_to_preif_flush = to_flush;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:64:46]
  assign EX_ar_valid = arvalidReg;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:110:25]
  assign EX_ar_bits_addr = araddrReg;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:111:24]
  assign EX_ar_bits_prot = 3'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:138:18]
  assign EX_aw_valid = awvalidReg;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:145:25]
  assign EX_aw_bits_addr = awaddrReg;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:146:24]
  assign EX_aw_bits_prot = 3'h0;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:138:18]
  assign EX_w_valid = wvalidReg;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:147:24]
  assign EX_w_bits_data = wdataReg;	// @[<stdin>:1279:3, playground/src/EX_stage.scala:148:23]
  assign EX_w_bits_strb = {4'h0, wstrbReg};	// @[<stdin>:1279:3, playground/src/EX_stage.scala:48:61, :149:23, :183:17]
  assign EX_b_ready = _EX_b_ready_output;	// @[<stdin>:1279:3]
endmodule

