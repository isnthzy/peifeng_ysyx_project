#include <amtest.h>

void hello() {
  AM_TIMER_RTC_T rtc = io_read(AM_TIMER_RTC);
  if(rtc.year>0) printf("111111");
  for (int i = 0; i < 10; i ++) {
    putstr("Hello, AM World @ " __ISA__ "\n");
  }
}
