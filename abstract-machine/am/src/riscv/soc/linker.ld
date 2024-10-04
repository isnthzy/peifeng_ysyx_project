ENTRY(_start)
PHDRS {
  text PT_LOAD FLAGS(r);  /* 只读执行段 */
  data PT_LOAD FLAGS(rw); /* 可读写段 */
}
MEMORY {
  mrom (rx) : ORIGIN = 0x20000000, LENGTH = 4K
  flash(rx) : ORIGIN = 0x30000000, LENGTH = 16M
  sram (wx) : ORIGIN = 0x0f000000, LENGTH = 8K
  psram(rwx) : ORIGIN = 0x80000000, LENGTH = 16M
}

REGION_ALIAS("LMA", flash);
REGION_ALIAS("VMA", psram);

SECTIONS {
  . = ORIGIN(LMA);
  .start : {
    *(entry)
  } > LMA AT> LMA : text

  .text : {
    *(.text*)
  } > VMA AT> LMA : data
  etext = .;
  _etext = .;
  .rodata : {
    *(.rodata*)
    *(.srodata*)
  } > VMA AT> LMA : data
  .data : {
    *(.data*)
    *(.sdata*)
  } > VMA AT> LMA : data
  _bootloader_virtual_start = ADDR(.text);
  _bootloader_load_start = LOADADDR(.text);
  _bootloader_size = SIZEOF(.data) + SIZEOF(.rodata) + SIZEOF(.text);
  edata = .;
  _data = .;
  .bss : {
    *(.bss*)
    *(.sbss*)
    *(.scommon)
  } > VMA AT> VMA : data /*这里有坑，定义了多个MEMORY后如果为指定存储区域会分配给sram而不是psram*/
  end = .;
  _end = .;
  _heap_start = _bootloader_virtual_start + _bootloader_size;
  _heap_end   = ORIGIN(VMA) + LENGTH(VMA);
  _stack_top = ORIGIN(sram); /*栈顶，为堆区结束的位置*/
  . = ORIGIN(sram) + LENGTH(sram);  
  _stack_pointer = .;   /*栈底，为VMA结束的位置*/
}
