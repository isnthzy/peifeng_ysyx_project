#ifndef _IRINGBUF_H__
#define _IRINGBUF_H__
#include <stdbool.h>

#define BUFFER_SIZE 10
#define MAX_STRING_LENGTH 100

typedef struct {
    char buffer[BUFFER_SIZE][MAX_STRING_LENGTH];
    int head;     // 指向队列头部的指针
    int tail;     // 指向队列尾部的指针
    bool full;    // 标志缓冲区是否已满
} IRingBuffer;

// 初始化缓冲区
void initializeIRingBuffer(IRingBuffer* buffer);
// 检查缓冲区是否为空
bool isIRingBufferEmpty(const IRingBuffer* buffer);
// 检查缓冲区是否已满
bool isIRingBufferFull(const IRingBuffer* buffer);
// 入队操作
bool enqueueIRingBuffer(IRingBuffer* buffer, const char* data);
// 出队操作
bool dequeueIRingBuffer(IRingBuffer* buffer, char* data);
#endif