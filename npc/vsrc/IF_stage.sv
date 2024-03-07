// Generated by CIRCT firtool-1.56.0
module IF_stage(	// @[<stdin>:80:3]
  input         clock,	// @[<stdin>:81:11]
                reset,	// @[<stdin>:82:11]
                IF_IO_valid,	// @[playground/src/IF_stage.scala:7:12]
  input  [31:0] IF_IO_bits_nextpc,	// @[playground/src/IF_stage.scala:7:12]
                IF_IO_bits_pc,	// @[playground/src/IF_stage.scala:7:12]
  input         IF_to_id_ready,	// @[playground/src/IF_stage.scala:7:12]
                IF_for_id_flush,	// @[playground/src/IF_stage.scala:7:12]
                IF_for_ex_flush,	// @[playground/src/IF_stage.scala:7:12]
                IF_r_valid,	// @[playground/src/IF_stage.scala:7:12]
  input  [31:0] IF_r_bits_data,	// @[playground/src/IF_stage.scala:7:12]
  output        IF_IO_ready,	// @[playground/src/IF_stage.scala:7:12]
                IF_to_id_valid,	// @[playground/src/IF_stage.scala:7:12]
  output [31:0] IF_to_id_bits_nextpc,	// @[playground/src/IF_stage.scala:7:12]
                IF_to_id_bits_pc,	// @[playground/src/IF_stage.scala:7:12]
                IF_to_id_bits_inst	// @[playground/src/IF_stage.scala:7:12]
);

  reg         if_inst_ok_buffer;	// @[playground/src/IF_stage.scala:19:42]
  reg  [31:0] if_inst_buffer;	// @[playground/src/IF_stage.scala:20:39]
  reg         if_use_inst_buffer;	// @[playground/src/IF_stage.scala:21:43]
  wire        if_flush = IF_for_ex_flush | IF_for_id_flush;	// @[playground/src/IF_stage.scala:22:30, :23:30]
  reg         if_valid;	// @[playground/src/IF_stage.scala:25:33]
  wire        if_inst_ok;	// @[playground/src/IF_stage.scala:18:32]
  wire        if_clog = ~if_inst_ok & if_valid;	// @[playground/src/IF_stage.scala:16:29, :18:32, :25:33, :27:{13,24}]
  wire        if_ready_go = ~if_clog;	// @[playground/src/IF_stage.scala:16:29, :19:42, :26:33, :28:19]
  wire        _IF_IO_ready_output = ~if_valid | if_ready_go & IF_to_id_ready;	// @[playground/src/IF_stage.scala:25:33, :26:33, :29:{17,27,42}]
  wire [31:0] if_inst = if_use_inst_buffer ? if_inst_buffer : IF_r_bits_data;	// @[playground/src/IF_stage.scala:17:36, :20:39, :21:43, :35:15]
  assign if_inst_ok = IF_r_valid | if_inst_ok_buffer;	// @[playground/src/IF_stage.scala:18:32, :19:42, :36:13, :52:18, :56:15]
  wire        _GEN = _IF_IO_ready_output & IF_IO_valid;	// @[playground/src/IF_stage.scala:29:27, src/main/scala/chisel3/util/Decoupled.scala:52:35]
  always @(posedge clock) begin	// @[<stdin>:81:11]
    if (reset) begin	// @[<stdin>:81:11]
      if_inst_ok_buffer <= 1'h0;	// @[playground/src/IF_stage.scala:19:42]
      if_inst_buffer <= 32'h0;	// @[playground/src/IF_stage.scala:17:36, :20:39]
      if_use_inst_buffer <= 1'h0;	// @[playground/src/IF_stage.scala:19:42, :21:43]
      if_valid <= 1'h0;	// @[playground/src/IF_stage.scala:19:42, :25:33]
    end
    else begin	// @[<stdin>:81:11]
      if_inst_ok_buffer <= IF_r_valid | ~_GEN & if_inst_ok_buffer;	// @[playground/src/IF_stage.scala:19:42, :21:43, :37:19, :38:23, :39:22, :52:18, :55:22, src/main/scala/chisel3/util/Decoupled.scala:52:35]
      if (IF_r_valid)	// @[playground/src/IF_stage.scala:7:12]
        if_inst_buffer <= IF_r_bits_data;	// @[playground/src/IF_stage.scala:20:39]
      if_use_inst_buffer <= IF_r_valid | ~_GEN & if_use_inst_buffer;	// @[playground/src/IF_stage.scala:21:43, :37:19, :38:23, :52:18, :54:23, src/main/scala/chisel3/util/Decoupled.scala:52:35]
      if (_IF_IO_ready_output)	// @[playground/src/IF_stage.scala:29:27]
        if_valid <= IF_IO_valid;	// @[playground/src/IF_stage.scala:25:33]
    end
  end // always @(posedge)
  assign IF_IO_ready = _IF_IO_ready_output;	// @[<stdin>:80:3, playground/src/IF_stage.scala:29:27]
  assign IF_to_id_valid = ~if_flush & if_valid & if_ready_go;	// @[<stdin>:80:3, playground/src/IF_stage.scala:22:30, :25:33, :26:33, :33:22]
  assign IF_to_id_bits_nextpc = IF_IO_bits_nextpc;	// @[<stdin>:80:3]
  assign IF_to_id_bits_pc = IF_IO_bits_pc;	// @[<stdin>:80:3]
  assign IF_to_id_bits_inst = if_inst;	// @[<stdin>:80:3, playground/src/IF_stage.scala:17:36]
endmodule

