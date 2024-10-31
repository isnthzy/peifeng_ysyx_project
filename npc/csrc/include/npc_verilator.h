#ifndef __NPCVERILATOR_H__
#define __NPCVERILATOR_H__
#define TOP_MODULE_NAME TOP_NAME
#define TOP_MODULE_NAME_H <TOP_NAME.h>
#include TOP_MODULE_NAME_H
#include <verilated.h>
#include <verilated_vcd_c.h>
#include <verilated_fst_c.h>
#include "svdpi.h"


extern VerilatedContext* contextp;
extern TOP_MODULE_NAME* top;
#ifdef TRACE_FST
extern VerilatedFstC* tfp;
#else
extern VerilatedVcdC* tfp;
#endif

#endif