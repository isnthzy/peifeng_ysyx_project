.section ssbl, "ax"
.globl _ssbl
.globl _ssbl_copy_loop
.type _ssbl, @function
.type _ssbl_copy_loop, @function

_ssbl:
  la a0, _bootloader_virtual_start
  la a1, _bootloader_load_start
  la a2, _bootloader_size
  li a5, 0
  j  _ssbl_copy_loop

_ssbl_copy_loop:
  add a3, a1, a5
  add a4, a0, a5
  lw a3, 0(a3)
  sw a3, 0(a4)
  addi a5, a5, 4
  bltu a5, a2, _ssbl_copy_loop
  li	a0,0
  ret