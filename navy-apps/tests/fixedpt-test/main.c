#include <unistd.h>
#include <stdio.h>
// #include <fixedptc.h>
int main() {
  // fixedpt a = fixedpt_rconst(1.2);
  // fixedpt b = fixedpt_fromint(10);
  // int c = 0;
  // if (b > fixedpt_rconst(7.9)) {
  //   c = fixedpt_toint(fixedpt_div(fixedpt_mul(a + FIXEDPT_ONE, b), fixedpt_rconst(2.3)));
  // }
  float a = 1.2;
  float b = 10;
  int c = 0;
  if (b > 7.9) {
    c = (a + 1) * b / 1.1;
  }
  // int test_1=fixedpt_ceil(2.5);
  // printf("test_1 :%d\n",test_1);
  // int test_2=fixedpt_floor(2.5);
  // printf("test_1 :%d\n",test_2);
  // int test_4=fixedpt_ceil(5.0);
  // int test_3=fixedpt_floor(5.0);
  // printf("test_3 :%d\n",test_3);
  // printf("test_4 :%d\n",test_4);
  printf("%d\n",c);
}
