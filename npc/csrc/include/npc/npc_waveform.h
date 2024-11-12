#ifndef NPC_WAVEFORM_H
#define NPC_WAVEFORM_H
#include <cstdint>
class Waveform{
  private:
    uint64_t total_wave_dump=0;
    long int suggest_savewave=1;
    void save_suggest_dump_time();
  public:
    void init_waveform();
    void exit_waveform();
    void dump_waveform();
    void total_wave_dump_plus_one(){
      total_wave_dump++;
    }
};

#ifdef TRACE_FST
#include <verilated_fst_c.h>
extern VerilatedFstC* tfp;
#else
#include <verilated_vcd_c.h>
extern VerilatedVcdC* tfp;
#endif

extern Waveform* waveform;
#endif