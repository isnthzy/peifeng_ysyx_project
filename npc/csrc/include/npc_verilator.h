#ifndef __NPCVERILATOR_H__
#define __NPCVERILATOR_H__
// #include "VSimTop___024root.h"
// #include <VSimTop.h>
// #include "VSimTop__Dpi.h"
#include <verilated.h>
#include <verilated_vcd_c.h>
#include <verilated_fst_c.h>
#include "svdpi.h"


extern VerilatedContext* contextp;
extern VSimTop* top;
#ifdef TRACE_FST
extern VerilatedFstC* tfp;
#else
extern VerilatedVcdC* tfp;
#endif

#endif