#include <am.h>
#include <riscv/riscv.h>
#define DEVICE_BASE 0xa0000000
#define SERIAL_PORT (DEVICE_BASE + 0x00003f8)
#define RTC_ADDR    (DEVICE_BASE + 0x0000048)
static uint64_t start_us;
void __am_timer_init() {
  start_us=(uint64_t)inl(RTC_ADDR)+((uint64_t)inl(RTC_ADDR+4)<<32);
}

void __am_timer_uptime(AM_TIMER_UPTIME_T *uptime) {
  uint64_t tmp_us=(uint64_t)inl(RTC_ADDR)+((uint64_t)inl(RTC_ADDR+4)<<32);
  uptime->us = tmp_us-start_us;
}

void __am_timer_rtc(AM_TIMER_RTC_T *rtc) {
  rtc->second = 0;
  rtc->minute = 0;
  rtc->hour   = 0;
  rtc->day    = 0;
  rtc->month  = 0;
  rtc->year   = 1900;
}
