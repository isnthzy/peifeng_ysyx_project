.section entry, "ax"
.globl _start
.type _start, @function

_start:
  mv s0, zero
  la sp, _stack_pointer
  jal _bootloader
  la t1, _trm_init
  jalr t1

_bootloader:
  la a0, _bootloader_virtual_start
  la a1, _bootloader_load_start
  la a2, _bootloader_size
  li a5, 0
  j  _copy_loop

_copy_loop:
  add a3, a1, a5
  add a4, a0, a5
  lbu a3, 0(a3)
  sb a3, 0(a4)
  addi a5, a5, 1
  bltu a5, a2, _copy_loop
  li	a0,0
  ret