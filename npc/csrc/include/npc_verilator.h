#ifndef __NPCVERILATOR_H__
#define __NPCVERILATOR_H__
#ifndef TOP_NAME
#define TOP_NAME VSimTop
#endif
#define TOP_MODULE_NAME TOP_NAME
#define TOP_MODULE_NAME_H <TOP_NAME.h>
#include TOP_MODULE_NAME_H
#include <verilated.h>


extern VerilatedContext* contextp;
extern TOP_MODULE_NAME* top;
#endif