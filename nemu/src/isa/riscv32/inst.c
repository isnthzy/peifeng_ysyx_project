/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include "local-include/reg.h"
#include <cpu/cpu.h>
#include <cpu/ifetch.h>
#include <cpu/decode.h>
#include "../../monitor/sdb/ftrace.h"
#include "../../cpu/iringbuf.h"
#define Reg(i) gpr(i)
#define Mr vaddr_read
#define Mw vaddr_write
#define XLEN 32 //代整数寄存器的宽度

#ifdef CONFIG_RVE
#define ECALL_REG 15 // a5
#else
#define ECALL_REG 17 // a7
#endif
extern IRingBuffer etrace_buffer;
extern bool ftrace_flag;
void store_commit_queue_push(paddr_t addr,word_t data,int len);
void load_commit_queue_push(paddr_t addr,word_t data,int type);

enum {
  TYPE_I, TYPE_U, TYPE_S,
  TYPE_B, TYPE_R, TYPE_J, 
  TYPE_N,// none
};

#define src1R() do { *src1 = Reg(rs1); } while (0)
#define src2R() do { *src2 = Reg(rs2); } while (0)
#define immI() do { *imm = SEXT(BITS(i, 31, 20), 12); } while(0)
#define immU() do { *imm = SEXT(BITS(i, 31, 12), 20) << 12; } while(0)
#define immS() do { *imm = (SEXT(BITS(i, 31, 25), 7) << 5) | BITS(i, 11, 7); } while(0)
#define immJ() do { *imm = SEXT(BITS(i, 31, 31)<<20 | BITS(i, 19, 12)<<12 \
| BITS(i, 20, 20)<<11 | BITS(i, 30, 21)<<1, 21); } while(0) //sext(immJ)
#define immB() do { *imm = SEXT(BITS(i, 31, 31)<<12 | BITS(i, 7, 7)<<11 \
| BITS(i, 30, 25)<<5 | BITS(i, 11, 8)<<1, 13); } while(0) //sext(immB)

#define MTVEC   0x305
#define MSTATUS 0x300
#define MEPC    0x341
#define MCAUSE  0x342
#define MTVAL   0x343
#define MVENDORID 0xf11
#define MARCHID 0xf12

#ifdef CONFIG_ETRACE
static char* get_csrname(word_t csr_addr){
  switch (csr_addr){
    case MTVEC:   return "mtvec";
    case MSTATUS: return "mstatus";
    case MEPC:    return "mepc";
    case MCAUSE:  return "mcause";
    default:
      wLog("unknow CSR reg: 0x%x",csr_addr);
      panic("访问了未知的CSR寄存器");
      break;
  }
}
#endif
static word_t tran_csr(word_t csr_addr,word_t data,bool is_write){
  word_t tmp_csr;
  switch (csr_addr){
    case MTVEC:
      tmp_csr=cpu.mtvec;
      if(is_write) cpu.mtvec=data;
      break;
    case MSTATUS:
      tmp_csr=cpu.mstatus;
      if(is_write) cpu.mstatus=data;
      break;
    case MEPC:
      tmp_csr=cpu.mepc;
      if(is_write) cpu.mepc=data;
      break;    
    case MCAUSE:
      tmp_csr=0xb;
      if(is_write) cpu.mcause=0xb;
      break; //因为nemu始终为m模式
    case MARCHID:
      tmp_csr=0x23060115;
      break;
    case MVENDORID:
      tmp_csr=0x79737978;
      break;
    default:
      wLog("unknow CSR reg: 0x%x",csr_addr);
      panic("访问了未知的CSR寄存器");
      break;
  }
  return tmp_csr;
}

word_t Rcsr(word_t csr_addr){
  return tran_csr(csr_addr,0,0);
}

void Wcsr(word_t csr_addr,word_t data){
  tran_csr(csr_addr,data,1);
}

void Bit_ctrl(uint32_t* number, int startBit, int width,uint32_t value) { //位操作
  uint32_t mask = ~(((1<<width)-1)<<startBit); // 生成掩码，用于清除指定位段
  *number&=mask; // 清除指定位段
  *number|=(value<<startBit); // 将新值设置到指定位段
}

static void decode_operand(Decode *s, int *rd, word_t *src1, word_t *src2, word_t *imm, int type,word_t *csr) {
  uint32_t i = s->isa.inst.val;
  int rs1 = BITS(i, 19, 15);
  int rs2 = BITS(i, 24, 20);
  *csr    = BITS(i, 32, 20);
  *rd     = BITS(i, 11, 7);
  switch (type) {
    case TYPE_I: src1R();          immI(); break;
    case TYPE_U:                   immU(); break;
    case TYPE_S: src1R(); src2R(); immS(); break;
    case TYPE_B: src1R(); src2R(); immB(); break;
    case TYPE_R: src1R(); src2R();         break;
    case TYPE_J:                   immJ(); break;
  }
}

static int decode_exec(Decode *s) {
  int rd = 0;
  word_t src1 = 0, src2 = 0, imm = 0 , csr = 0;
  s->dnpc = s->snpc;

#define INSTPAT_INST(s) ((s)->isa.inst.val)
#define INSTPAT_MATCH(s, name, type, ... /* execute body */ ) { \
  decode_operand(s, &rd, &src1, &src2, &imm, concat(TYPE_, type),&csr); \
  __VA_ARGS__ ; \
}

  INSTPAT_START();
  INSTPAT("??????? ????? ????? ??? ????? 00101 11", auipc  , U, Reg(rd) = s->pc + imm);
  INSTPAT("??????? ????? ????? ??? ????? 01101 11", lui    , U, Reg(rd) = imm);
  INSTPAT("??????? ????? ????? 111 ????? 00100 11", andi   , I, Reg(rd) = src1 & imm);
  INSTPAT("??????? ????? ????? 000 ????? 00100 11", addi   , I, Reg(rd) = src1 + imm);
  INSTPAT("??????? ????? ????? 000 ????? 11001 11", jalr   , I, Reg(rd) = s->pc+4;s->dnpc=(src1+imm)&~1;
                                                                IFDEF(CONFIG_FTRACE,if(ftrace_flag==true){
                                                                  if(s->isa.inst.val==0x00008067){
                                                                    func_ret(s->pc);
                                                                  }else if(rd==1){ //jalr是跳转,jr不是(jr被编译器优化为尾调用)
                                                                    func_call(s->pc,s->dnpc,false);
                                                                  }
                                                                  else if(rd==0&&imm==0){
                                                                    func_call(s->pc,s->dnpc,true);
                                                                  }
                                                                }));
  INSTPAT("??????? ????? ????? 010 ????? 00000 11", lw     , I, Reg(rd) = Mr(src1 + imm, 4);
                                                                IFDEF(CONFIG_TARGET_SHARE,load_commit_queue_push(src1+imm,Reg(rd),4)););
  INSTPAT("??????? ????? ????? 000 ????? 00000 11", lb     , I, Reg(rd) = SEXT(Mr(src1 + imm, 1),8);
                                                                IFDEF(CONFIG_TARGET_SHARE,load_commit_queue_push(src1+imm,Reg(rd),1)););
  INSTPAT("??????? ????? ????? 100 ????? 00000 11", lbu    , I, Reg(rd) = Mr(src1 + imm, 1);
                                                                IFDEF(CONFIG_TARGET_SHARE,load_commit_queue_push(src1+imm,Reg(rd),1)););
  INSTPAT("??????? ????? ????? 101 ????? 00000 11", lhu    , I, Reg(rd) = Mr(src1 + imm, 2);
                                                                IFDEF(CONFIG_TARGET_SHARE,load_commit_queue_push(src1+imm,Reg(rd),2)););
  INSTPAT("??????? ????? ????? 001 ????? 00000 11", lh     , I, Reg(rd) = SEXT(Mr(src1 + imm, 2),16);
                                                                IFDEF(CONFIG_TARGET_SHARE,load_commit_queue_push(src1+imm,Reg(rd),2)););
  INSTPAT("??????? ????? ????? 100 ????? 00100 11", xori   , I, Reg(rd) = src1 ^ imm);
  INSTPAT("000000? ????? ????? 001 ????? 00100 11", slli   , I, Reg(rd) = src1<<BITS(imm,5,0));
  INSTPAT("??????? ????? ????? 010 ????? 00100 11", slti   , I, Reg(rd) = (sword_t)src1<(sword_t)imm);
  INSTPAT("??????? ????? ????? 011 ????? 00100 11", sltiu  , I, Reg(rd) = src1<imm);
  INSTPAT("000000? ????? ????? 101 ????? 00100 11", srli   , I, Reg(rd) = src1>>BITS(imm,5,0)); //src本身是无符号的,为逻辑右移
  INSTPAT("010000? ????? ????? 101 ????? 00100 11", srai   , I, Reg(rd) = (sword_t)src1>>BITS(imm,5,0));
  INSTPAT("0000000 ????? ????? 000 ????? 01100 11", add    , R, Reg(rd) = src1 + src2); //测试difftest的时候把src2改成src1
  INSTPAT("0100000 ????? ????? 000 ????? 01100 11", sub    , R, Reg(rd) = src1 - src2);
  INSTPAT("0000001 ????? ????? 000 ????? 01100 11", mul    , R, Reg(rd) = src1 * src2);
  INSTPAT("0000001 ????? ????? 100 ????? 01100 11", div    , R, Reg(rd) = (sword_t)src1 / (sword_t)src2);
  INSTPAT("0000001 ????? ????? 101 ????? 01100 11", divu   , R, Reg(rd) = src1 / src2);
  INSTPAT("0000000 ????? ????? 100 ????? 01100 11", xor    , R, Reg(rd) = src1 ^ src2);
  INSTPAT("0000000 ????? ????? 110 ????? 01100 11", or     , R, Reg(rd) = src1 | src2);
  INSTPAT("0000000 ????? ????? 111 ????? 01100 11", and    , R, Reg(rd) = src1 & src2);
  INSTPAT("0100000 ????? ????? 101 ????? 01100 11", sra    , R, Reg(rd) = (sword_t)src1>>BITS(src2,5,0));
  INSTPAT("0000000 ????? ????? 001 ????? 01100 11", sll    , R, Reg(rd) = src1<<BITS(src2,5,0));
  INSTPAT("0000000 ????? ????? 101 ????? 01100 11", srl    , R, Reg(rd) = src1>>BITS(src2,5,0));
  INSTPAT("0000000 ????? ????? 010 ????? 01100 11", slt    , R, Reg(rd) = (sword_t)src1 < (sword_t)src2);
  INSTPAT("0000000 ????? ????? 011 ????? 01100 11", sltu   , R, Reg(rd) = src1<src2);
  INSTPAT("0000001 ????? ????? 110 ????? 01100 11", rem    , R, Reg(rd) = (sword_t)src1 % (sword_t)src2);
  INSTPAT("0000001 ????? ????? 111 ????? 01100 11", remu   , R, Reg(rd) = src1 % src2);
  INSTPAT("??????? ????? ????? 110 ????? 00100 11", ori    , I, Reg(rd) = src1 | imm);
  INSTPAT("0000001 ????? ????? 001 ????? 01100 11", mulh   , R, Reg(rd) = ((SEXT(src1, 32) * SEXT(src2, 32)) >> 32));
  INSTPAT("0000001 ????? ????? 011 ????? 01100 11", mulhu  , R, Reg(rd) = ((uint64_t)((uint64_t)src1 * (uint64_t)src2) >> 32));
  INSTPAT("??????? ????? ????? ??? ????? 11011 11", jal    , J, Reg(rd) = s->pc+4;s->dnpc=s->pc+imm;
  IFDEF(CONFIG_FTRACE,if(ftrace_flag==true&&rd==1){ //jal是跳转,j不是
    func_call(s->pc,s->dnpc,false);
  }));
  INSTPAT("??????? ????? ????? 000 ????? 01000 11", sb     , S, Mw(src1 + imm, 1, src2);
                                                                IFDEF(CONFIG_TARGET_SHARE,store_commit_queue_push(src1+imm,src2,1)););
  INSTPAT("??????? ????? ????? 001 ????? 01000 11", sh     , S, Mw(src1 + imm, 2, src2);
                                                                IFDEF(CONFIG_TARGET_SHARE,store_commit_queue_push(src1+imm,src2,2)););
  INSTPAT("??????? ????? ????? 010 ????? 01000 11", sw     , S, Mw(src1 + imm, 4, src2);
                                                                IFDEF(CONFIG_TARGET_SHARE,store_commit_queue_push(src1+imm,src2,4)););
  INSTPAT("??????? ????? ????? 000 ????? 11000 11", beq    , B, if(src1==src2) s->dnpc=s->pc+imm);
  INSTPAT("??????? ????? ????? 001 ????? 11000 11", bne    , B, if(src1!=src2) s->dnpc=s->pc+imm);
  INSTPAT("??????? ????? ????? 100 ????? 11000 11", blt    , B, if((sword_t)src1< (sword_t)src2) s->dnpc=s->pc+imm);
  INSTPAT("??????? ????? ????? 110 ????? 11000 11", bltu   , B, if(src1< src2) s->dnpc=s->pc+imm);
  INSTPAT("??????? ????? ????? 101 ????? 11000 11", bge    , B, if((sword_t)src1>=(sword_t)src2) s->dnpc=s->pc+imm);
  INSTPAT("??????? ????? ????? 111 ????? 11000 11", bgeu   , B, if(src1>=src2) s->dnpc=s->pc+imm);

  INSTPAT("??????? ????? ????? 010 ????? 11100 11", csrrs  , I, int t=Rcsr(csr); Wcsr(csr,t|src1); Reg(rd)=t;
                                                                IFDEF(CONFIG_ETRACE,
                                                                  char etrace_logbuf[128]; 
                                                                  sprintf(etrace_logbuf,"pc:0x%08x csrrs reg(%d)<-0x%08x Wcsr:0x%08x->%s"\
                                                                  ,cpu.pc,rd,t,t|src1,get_csrname(csr)); 
                                                                  wLog("\t%s",etrace_logbuf);
                                                                  enqueueIRingBuffer(&etrace_buffer,etrace_logbuf);
                                                                ));
  INSTPAT("??????? ????? ????? 001 ????? 11100 11", csrrw  , I, int t=Rcsr(csr); Wcsr(csr,  src1); Reg(rd)=t;
                                                                IFDEF(CONFIG_ETRACE,
                                                                  char etrace_logbuf[128]; 
                                                                  sprintf(etrace_logbuf,"pc:0x%08x csrrw reg(%d)<-0x%08x Wcsr:0x%08x->%s"\
                                                                  ,cpu.pc,rd,t,  src1,get_csrname(csr));
                                                                  wLog("\t%s",etrace_logbuf); 
                                                                  enqueueIRingBuffer(&etrace_buffer,etrace_logbuf);
                                                                ));
  INSTPAT("0011000 00010 00000 000 00000 11100 11", mret   , R, s->dnpc=cpu.mepc;
                                                                IFDEF(CONFIG_ETRACE, 
                                                                  char etrace_logbuf[128]; 
                                                                  sprintf(etrace_logbuf,"pc:0x%08x mret dnpc<-mepc:0x%08x",cpu.pc,cpu.mepc); 
                                                                  wLog("\t%s",etrace_logbuf);
                                                                  enqueueIRingBuffer(&etrace_buffer,etrace_logbuf);
                                                                )); //mret没有实现完毕
  INSTPAT("0000000 00000 00000 000 00000 11100 11", ecall  , I, s->dnpc=isa_raise_intr(Reg(ECALL_REG),s->pc);
                                                                IFDEF(CONFIG_ETRACE, 
                                                                  char etrace_logbuf[128]; 
                                                                  sprintf(etrace_logbuf,"pc:0x%08x ecall!!!",cpu.pc); 
                                                                  wLog("\t%s",etrace_logbuf);
                                                                  enqueueIRingBuffer(&etrace_buffer,etrace_logbuf);
                                                                ));
  INSTPAT("0000000 00000 00000 001 00000 00011 11", fence.i, N, );
  INSTPAT("0000000 00001 00000 000 00000 11100 11", ebreak , N, NEMUTRAP(s->pc, Reg(10))); // R(10) is $a0
  INSTPAT("??????? ????? ????? ??? ????? ????? ??", inv    , N, INV(s->pc));
  INSTPAT_END();

  Reg(0) = 0; // reset $zero to 0

  return 0;
}

int isa_exec_once(Decode *s) {
  s->isa.inst.val = inst_fetch(&s->snpc, 4);
  return decode_exec(s);
}
