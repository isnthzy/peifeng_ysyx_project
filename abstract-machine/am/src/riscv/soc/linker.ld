ENTRY(_start)
PHDRS {
  start PT_LOAD;
  ssbl  PT_LOAD;
  text  PT_LOAD;  
  rodata PT_LOAD;
  data  PT_LOAD; 
  bss   PT_LOAD;
}

MEMORY {
  mrom (rx) : ORIGIN = 0x20000000, LENGTH = 4K
  flash(rx) : ORIGIN = 0x30000000, LENGTH = 16M
  sram (wx) : ORIGIN = 0x0f000000, LENGTH = 8K
  psram(rwx) : ORIGIN = 0x80000000, LENGTH = 16M
  sdram(rwx) : ORIGIN = 0xa0000000, LENGTH = 32M
}

REGION_ALIAS("LMA", flash);
REGION_ALIAS("VMA", sdram);

SECTIONS {
  . = ORIGIN(LMA);
  .start : {
    *(entry)
  } > LMA AT> LMA : start

  .ssbl : ALIGN(10){
    *(ssbl)
  } > VMA AT> LMA : ssbl
  _ssbl_end = .;

  .text :  ALIGN(10){
    *(.text*)
  } > VMA AT> LMA : text
  etext = .;
  _etext = .;
  .rodata :  ALIGN(10){
    *(.rodata*)
    *(.srodata*)
  } > VMA AT> LMA : rodata
  .data :  ALIGN(10){
    *(.data*)
    *(.sdata*)
  } > VMA AT> LMA : data
  edata = .;
  _data = .;
  .bss :  ALIGN(10){
    *(.bss*)
    *(.sbss*)
    *(.scommon)
  } > VMA AT> LMA : bss  /*这里有坑，定义了多个MEMORY后如果为指定存储区域会分配给sram而不是psram*/
  . = ALIGN(10);
  end = .;
  _end = .;
  _bootloader_virtual_start = ADDR(.text);
  _bootloader_load_start = LOADADDR(.text);
  _bootloader_size = _end - _bootloader_virtual_start;
  _ssbl_virtual_start = ADDR(.ssbl);
  _ssbl_load_start = LOADADDR(.ssbl);
  _ssbl_size = _ssbl_end - _ssbl_virtual_start;
  _heap_start = _bootloader_virtual_start + _bootloader_size;
  _heap_end   = ORIGIN(VMA) + LENGTH(VMA);  
  _stack_top = ORIGIN(sram); /*栈顶，为堆区结束的位置*/
  . = ORIGIN(sram) + LENGTH(sram);  
  _stack_pointer = .;   /*栈底，为VMA结束的位置*/
}
