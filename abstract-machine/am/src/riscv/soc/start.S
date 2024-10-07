.section entry, "ax"
.globl _start
.globl _fsbl
.globl _fsbl_copy_loop
.type _start, @function
.type _fsbl, @function
.type _fsbl_copy_loop, @function



_start:
  mv s0, zero
  la sp, _stack_pointer
  jal _fsbl
  la t1, _ssbl
  jalr t1


_fsbl:
  la a0, _ssbl_virtual_start
  la a1, _ssbl_load_start
  la a2, _ssbl_size
  li a5, 0
  j  _fsbl_copy_loop

_fsbl_copy_loop:
  add a3, a1, a5
  add a4, a0, a5
  lw a3, 0(a3)
  sw a3, 0(a4)
  addi a5, a5, 4
  bltu a5, a2, _fsbl_copy_loop
  li	a0,0
  ret