#include <klibtest.h>
#include <limits.h>

int main()
{
    char buffer[50];
    char* s = "wangzhixin";
 
    // 读取字符串并存储在 buffer 中
    int j = sprintf(buffer,"%s 666\n", s);
 
    // 输出 buffer及字符数
    printf("string:\n%s\ncharacter count = %d\n", buffer, j);
 
    return 0;
}