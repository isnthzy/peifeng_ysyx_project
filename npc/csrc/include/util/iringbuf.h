#ifndef _IRINGBUF_H__
#define _IRINGBUF_H__
#include <stdbool.h>
#include "npc_conf.h"
#define MAX_STRING_LENGTH 128
typedef struct {
    char buffer[IRINGBUF_MAX_NUM][MAX_STRING_LENGTH];
    int head;     // 指向队列头部的指针
    int tail;     // 指向队列尾部的指针
    int size;     // 缓冲区有多大
    int num ;     // 缓冲区有多少个元素
    bool full;    // 标志缓冲区是否已满
} IRingBuffer;

// 初始化缓冲区
void initializeIRingBuffer(IRingBuffer* buffer,int size);
// 检查缓冲区是否为空
bool isIRingBufferEmpty(const IRingBuffer* buffer);
// 检查缓冲区是否已满
bool isIRingBufferFull(const IRingBuffer* buffer);
// 入队操作
bool enqueueIRingBuffer(IRingBuffer* buffer, const char* data);
// 出队操作
bool dequeueIRingBuffer(IRingBuffer* buffer, char* data);

void putIringbuf();

void mputIringbuf();
#endif