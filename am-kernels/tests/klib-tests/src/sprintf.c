#include <klibtest.h>
#include <limits.h>
char src1[128];
char *src2[]={"0","126322567","2147483647","-2147483648","-2147483647","252645135","126322567","-1","1900"};
int data[] = {0, INT_MAX / 17, INT_MAX, INT_MIN, INT_MIN + 1,
              UINT_MAX / 17, INT_MAX / 17, UINT_MAX,1900};
int main() {
    for(int i=0;i<sizeof(data)/sizeof(data[0]);i++){
        sprintf(src1, "%d", data[i]);
        putstr(src1);
        // putch(' ');
        if(strcmp(src1,src2[i])!= 0){
            assert(0);
        }
        // printf("%d\n",data[i]);
    }
	return 0;
}