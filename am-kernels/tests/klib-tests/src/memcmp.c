#include <klibtest.h>
#define N 32
char src1[N]="Hello world";
char src2[N]=" Hello world";

void test_memcmp(){
  int l;
  for (l = 0; l < 7; l ++){
    if(memcmp(src1+l,src2+l+1,3)!=0){
        assert(0);
    }
  }
}

int main(){
  test_memcmp();
  return 0;
}