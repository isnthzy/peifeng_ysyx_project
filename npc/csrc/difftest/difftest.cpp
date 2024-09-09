#include "../include/difftest/difftest.h"
#include "../include/util/debug.h"
#include "../include/npc/npc_memory.h"
#include "../include/npc/npc_device.h"


#define DIFF_CHECK(addr1, addr2, return,name) if(addr1!=addr2){\
  wLog("The %s is different\nref:0x%08x dut:0x%08x",name,addr1,addr2); \
  return=false;\
}


void Difftest::init_difftest(char *ref_so_file, int port){
  nemu_proxy = new NemuProxy;
  nemu_proxy->init_nemu_proxy(ref_so_file, port);
}

void Difftest::exit_difftest(){
  delete nemu_proxy;
  nemu_proxy = NULL;
}

void Difftest::first_commit(){
  static bool is_first_commit = true;
  if(dut_commit.commit[0].valid&&is_first_commit){
    nemu_proxy->ref_difftest_memcpy(RESET_VECTOR, guest_to_host(RESET_VECTOR), img_size, DIFFTEST_TO_REF);
    nemu_proxy->ref_difftest_regcpy(&dut, DIFFTEST_TO_REF); //同步整个结构体
    is_first_commit=false;
  }
}

/*
NOTE:diffstep的执行顺序
首先算出这次提交的数量和需要跳过的提交数量，提交的时候顺便把这次提交的指令记录到itrace中
以提交数量为根据，计算出提交的最后一个pc（例如提交了两条指令，那么base.pc的结果就为第二条指令的pc）
//TODO:死锁检查
判断是不是无提交指令，无提交指令则直接返回让NPC继续执行
判断是否是第一次提交，如果是第一次提交，同步NPC初始化内容等各项事务
判断是否是退出指令，如果是，则退出
NEMU根据这次提交的指令数量，决定执行几次
发现当前提交的指令是跳过指令，传输diff同步，覆盖掉NEMU提交结果
拉取NEMU的寄存器结果
对比寄存器，进行diff同步
*/


int Difftest::diff_step(){
  //TODO:define返回值，用来判断diff运行的状况
  idx_commit_num=0;
  step_skip_num=0;
  while(idx_commit_num<DIFFTEST_COMMIT_WIDTH&&dut_commit.commit[idx_commit_num].valid){
    deadlock_timer=0;
    IFDEF(CONFIG_DEVICE, device_update(););
    if(dut_commit.commit[idx_commit_num].skip){
      step_skip_num++;
    }
    
    #ifdef CONFIG_TRACE
    static char logbuf[128];
    static char tmp_dis[64];
    uint32_t tmp_inst=dut_commit.commit[idx_commit_num].inst;
    vaddr_t  tmp_pc  =dut_commit.commit[idx_commit_num].pc;

    disassemble(tmp_dis, sizeof(tmp_dis),tmp_pc, (uint8_t*)&tmp_inst,4);
    sprintf(logbuf,"[%ld]\t0x%08x: %08x\t%s",total_inst,tmp_pc,tmp_inst,tmp_dis);
    #ifdef CONFIG_ITRACE
    log_write("%s\n",logbuf);
    enqueueIRingBuffer(&iring_buffer,logbuf); //入队环形缓冲区
    #endif
    wp_trace(logbuf);
    if (g_print_step) { IFDEF(CONFIG_ITRACE,printf("%s\n",logbuf)); }
    #endif

    total_inst++;
    idx_commit_num++;
  }

  if(total_inst>CONFIG_MAX_EXE_INST){
    panic("Too many instructions(Suspected to be in a traploop)");
  }//NOTE:最大边界检测

  //TODO:死锁检查设计在无提交检查前面
  if(idx_commit_num==0){ //NOTE:检测是否有效提交,无效提交返回NPC_NOCOMMIT(不检查)
    if(DEADLOCK_TIME>0){
      deadlock_timer++;
      printf("Deadlock %d\n",deadlock_timer);
      if(deadlock_timer>DEADLOCK_TIME)
      {
        wLog("NPC more than %d clocks were not submitted",DEADLOCK_TIME);
        return NPC_ABORT;
      }
    }
    return NPC_NOCOMMIT;
  }

  if(idx_commit_num > 0){
    dut.base.pc   = dut_commit.commit[idx_commit_num-1].pc;
    dut.base.inst = dut_commit.commit[idx_commit_num-1].inst;
  }

  first_commit(); //当第一条指令提交时，开始同步


  if(idx_commit_num>0&&dut_commit.excp.excp_valid&&dut_commit.excp.exception==0x3){
    for(int i = 0;i<idx_commit_num;i++){
      if(dut_commit.commit[i].pc==dut_commit.excp.exceptionPC){
        if(dut_commit.commit[i].wdata==0x0){
          npc_state.halt_pc = dut_commit.commit[i].pc;
          return NPC_SUCCESS_END;
        }else{
          npc_state.halt_pc = dut_commit.commit[i].pc;
          return NPC_ERROR_END;
        }
      }
    }
  }  

  for(int i=0;i<idx_commit_num;i++){
    nemu_proxy->ref_difftest_exec(1);
    dut_commit.commit[i].valid=false; //因为结构体不像波形你这周期拉高下周期就回去了，所以手动清0
  }//发射了几条指令就执行几次

  if(step_skip_num>0){
    nemu_proxy->ref_difftest_regcpy(&dut, DIFFTEST_TO_REF);
    return NPC_RUNNING;
  }//NOTE:遇到跳过指令，等NEMU执行完后对NEMU的寄存器结果同步，然后返回NPC_RUNNING

  if(dut_commit.excp.excp_valid){
    nemu_proxy->ref_difftest_raise_intr(dut_commit.excp.exception);
    dut_commit.excp.excp_valid=false;
  }

  nemu_proxy->ref_difftest_regcpy(&ref, DIFFTEST_TO_DUT);
  if(!checkregs()){
    display();
    return NPC_ABORT;
  }else{
    return NPC_RUNNING;
  }

}


bool Difftest::checkregs(){
  bool check_result=true;
  for(int i=0;i<MUXDEF(CONFIG_RVE, 16, 32);i++){
    if(ref.regs.gpr[i]!=dut.regs.gpr[i]){
      wLog("The reg:%s(rf_%d) is different\nref:0x%08x dut:0x%08x",
        regs[i],i,ref.regs.gpr[i],dut.regs.gpr[i]);
      check_result=false;
    }
  }
  DIFF_CHECK(ref.base.pc     ,dut.base.pc     ,check_result ,"pc");
  DIFF_CHECK(ref.csr.mtvec   ,dut.csr.mtvec   ,check_result ,"mtvec");
  DIFF_CHECK(ref.csr.mepc    ,dut.csr.mepc    ,check_result ,"mepc ");
  DIFF_CHECK(ref.csr.mstatus ,dut.csr.mstatus ,check_result ,"mstatus"); //mret实现不完整
  if(!check_result){
    wLog("at pc:0x%08x",ref.base.pc);
  }
  return check_result;
}

void Difftest::display(){
  fflush(NULL);
  wLog("=================================  DUT  Regs  =================================");
  for (int i = 0; i < MUXDEF(CONFIG_RVE, 16, 32); i += 4) {
    wLog("%s(r%2d): 0x%08x %s(r%2d): 0x%08x %s(r%2d): 0x%08x %s(r%2d): 0x%08x", 
      regs[i]  , i  , dut_regs_ptr[i]  ,regs[i+1], i+1, dut_regs_ptr[i+1],
      regs[i+2], i+2, dut_regs_ptr[i+2],regs[i+3], i+3, dut_regs_ptr[i+3]);
  }
  wLog("pc: 0x%08x inst: 0x%08x", dut.base.pc, dut.base.inst);
  wLog("MSTATUS: 0x%08x, MTVEC: 0x%08x, MEPC: 0x%08x", dut.csr.mstatus, dut.csr.mtvec, dut.csr.mepc);
  wLog(" MCAUSE: 0x%08x", dut.csr.mcause);
  fflush(NULL);

  nemu_proxy->ref_reg_display();
  fflush(NULL);
}