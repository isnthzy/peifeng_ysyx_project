#include <stdio.h>
#include <stdbool.h>
#include <string.h>
#include "../include/iringbuf.h"

void initializeIRingBuffer(IRingBuffer* buffer,int size) {
    memset(buffer->buffer, 0, sizeof(buffer->buffer));
    buffer->size = size;
    buffer->head = 0;
    buffer->tail = 0;
    buffer->num = 0;
    buffer->full = false;
}

// 检查缓冲区是否为空
bool isIRingBufferEmpty(const IRingBuffer* buffer) {
    return (!buffer->full && buffer->head == buffer->tail);
}

// 检查缓冲区是否已满
bool isIRingBufferFull(const IRingBuffer* buffer) {
    return buffer->full;
}

// 入队操作
bool enqueueIRingBuffer(IRingBuffer* buffer, const char* data) {
    if (strlen(data) >= MAX_STRING_LENGTH) {
        return false;  // 字符串长度超过最大长度，无法入队
    }
    if (isIRingBufferFull(buffer)) {
        // 缓冲区已满，需要先出队一个字符串
        buffer->tail = (buffer->tail + 1) % buffer->size;
        buffer->num--;
    }
    printf("enqueueIRingBuffer: %s\n", data);
    buffer->num++;
    strcpy(buffer->buffer[buffer->head], data);
    buffer->head = (buffer->head + 1) % buffer->size;
    buffer->full = (buffer->head == buffer->tail);
    printf("enqueueIRingBuffer: %s\n", data);
    return true;
}

// 出队操作
bool dequeueIRingBuffer(IRingBuffer* buffer, char* data) {
    if (isIRingBufferEmpty(buffer)) {
        return false;  // 缓冲区为空，无法出队
    }
    buffer->num--;
    strcpy(data, buffer->buffer[buffer->tail]);
    buffer->tail = (buffer->tail + 1) % buffer->size;
    buffer->full = false;

    return true;
}
