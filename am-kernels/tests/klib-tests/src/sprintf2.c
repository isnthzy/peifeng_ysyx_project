#include <klibtest.h>
#include <limits.h>

int main()
{
    char buffer1[50];
    char buffer2[50];
    char* s = "wangzhixin";
 
    // 读取字符串并存储在 buffer 中
    int j1 = sprintf(buffer1,"%s 666\n", s);
    int j2 = snprintf(buffer2,5,"%s", s);
 
    // 输出 buffer及字符数
    printf("string:\n%s\ncharacter count = %d\n", buffer1, j1);
    printf("string:\n%s\ncharacter count = %d\n", buffer2, j2);
    return 0;
}