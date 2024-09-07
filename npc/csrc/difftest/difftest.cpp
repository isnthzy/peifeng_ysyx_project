#include "../include/difftest/difftest.h"
#include "../include/util/debug.h"
#include "../include/npc/npc_memory.h"
#include "../include/npc/npc_device.h"


#define DIFF_CHECK(addr1, addr2, atpc,name) if(addr1!=addr2){\
  wLog("The %s is different\nref:0x%08x dut:0x%08x",name,addr1,addr2); \
  wLog("at pc:0x%08x",atpc); \
  return false;\
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
int Difftest::diff_step(){
  //TODO:define返回值，用来判断diff运行的状况
  idx_commit_num=0;
  step_skip_num=0;
  while(idx_commit_num<DIFFTEST_COMMIT_WIDTH&&dut_commit.commit[idx_commit_num].valid){
    total_inst+=1;
    idx_commit_num++;
    IFDEF(CONFIG_DEVICE, device_update(););
    if(dut_commit.commit[idx_commit_num].skip){
      step_skip_num++;
    }
    
    #ifdef CONFIG_TRACE
    static char logbuf[128];
    static char tmp_dis[64];
    uint32_t tmp_inst=dut_commit.commit[idx_commit_num].inst;
    vaddr_t tmp_pc=dut_commit.commit[idx_commit_num].pc;
    disassemble(tmp_dis, sizeof(tmp_dis),tmp_pc, (uint8_t*)&tmp_inst,4);
    sprintf(logbuf,"[%ld]\t0x%08x: %08x\t%s",total_inst,tmp_pc,tmp_inst,tmp_dis);
    #ifdef CONFIG_ITRACE
    log_write("%s\n",logbuf);
    enqueueIRingBuffer(&iring_buffer,logbuf); //入队环形缓冲区
    #endif
    wp_trace(logbuf);
    if (g_print_step) { IFDEF(CONFIG_ITRACE,printf("%s\n",logbuf)); }
    #endif
  }

  if(step_skip_num>0){
    nemu_proxy->ref_difftest_regcpy(&dut, DIFFTEST_TO_REF);
    return NPC_RUNNING;
  }//NOTE:跳过指令

  if(total_inst>CONFIG_MAX_EXE_INST){
    panic("Too many instructions(Suspected to be in a traploop)");
  }//NOTE:最大边界检测

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
  }//发射了几条指令就执行几次

  if(dut_commit.excp.excp_valid){
    nemu_proxy->ref_difftest_raise_intr(dut_commit.excp.exception);
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
  for(int i=0;i<MUXDEF(CONFIG_RVE, 16, 32);i++){
    if(ref.regs.gpr[i]!=dut.regs.gpr[i]){
      wLog("The reg:%s(rf_%d) is different\nref:0x%08x dut:0x%08x",
        regs[i],i,ref.regs.gpr[i],dut.regs.gpr[i]);
      wLog("at pc:0x%08x",ref.base.pc);
      return false;
    }
  }
  DIFF_CHECK(ref.base.pc     ,dut.base.pc     ,  ref.base.pc,"pc");
  DIFF_CHECK(ref.csr.mtvec   ,dut.csr.mtvec   ,  ref.base.pc,"mtvec");
  DIFF_CHECK(ref.csr.mepc    ,dut.csr.mepc    ,  ref.base.pc,"mepc ");
  DIFF_CHECK(ref.csr.mstatus ,dut.csr.mstatus ,  ref.base.pc,"mstatus"); //mret实现不完整
  // DIFF_CHECK(ref_r->mcause ,cpu.mcause , pc,"mcause");
  return true;
}

void Difftest::display(){
  fflush(NULL);
  wLog("\n==============  DUT Regs  ==============");
  for (int i = 0; i < 32; i += 4) {
    wLog("%s(r%2d): 0x%08x %s(r%2d): 0x%08x %s(r%2d): 0x%08x %s(r%2d): 0x%08x", 
      regs[i]  , i  , dut_regs_ptr[i]  ,regs[i+1], i+1, dut_regs_ptr[i+1],
      regs[i+2], i+2, dut_regs_ptr[i+2],regs[i+3], i+3, dut_regs_ptr[i+3]);
  }
  wLog("pc: 0x%08x inst: 0x%08x", dut.base.pc, dut.base.inst);
  wLog("MSTATUS: 0x%08x, MTVEC: 0x%08x, MEPC: 0x%08x", dut.csr.mstatus, dut.csr.mtvec, dut.csr.mepc);
  wLog(" MCAUSE: 0x%08x", dut.csr.mcause);
  wLog("*******************************************************************************");
  wLog("\n==============  REF Regs  ==============");
  fflush(NULL);

  nemu_proxy->ref_reg_display();
  fflush(NULL);
}