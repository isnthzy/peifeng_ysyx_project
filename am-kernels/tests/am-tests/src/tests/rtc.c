#include <amtest.h>

void rtc_test() {
  AM_TIMER_RTC_T rtc;
  int sec = 1;
  while (1) {
    while(io_read(AM_TIMER_UPTIME).us / 1000000 < sec) ;
    rtc = io_read(AM_TIMER_RTC);
    char buffer[1024];
    sprintf(buffer,"%d-%d-%d %02d:%02d:%02d GMT (", rtc.year, rtc.month, rtc.day, rtc.hour, rtc.minute, rtc.second);
    putstr(buffer);
    if (sec == 1) {
      printf("%d second).\n", sec);
    } else {
      printf("%d seconds).\n", sec);
    }
    sec ++;
  }
}
