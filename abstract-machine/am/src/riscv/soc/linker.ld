ENTRY(_start)
PHDRS { text PT_LOAD; data PT_LOAD; }
MEMORY {
  mrom (rx) : ORIGIN = 0x20000000, LENGTH = 4K
  flash(rx) : ORIGIN = 0x30000000, LENGTH = 16M
  sram (wx) : ORIGIN = 0x0f000000, LENGTH = 8K
  psram(wx) : ORIGIN = 0x80000000, LENGTH = 16M
}

REGION_ALIAS("LMA", flash);
REGION_ALIAS("VMA", psram);

SECTIONS {
  /* _pmem_start and _entry_offset are defined in LDFLAGS */
  . = ORIGIN(LMA);
  .text : {
    *(entry)
    *(.text*)
  } > LMA AT> LMA : text
  etext = .;
  _etext = .;
  .rodata : {
    *(.rodata*)
  } > LMA
  rodata_end = .;
  .data : AT(rodata_end){
    _data_start = .;
    *(.data*)
    *(.sdata*)
    *(.srodata*)
  } > VMA : data
  _data_load_start = LOADADDR(.data);
  edata = .;
  _data = .;
  .bss : {
	_bss_start = .;
    *(.bss*)
    *(.sbss*)
    *(.scommon)
  }
  end = .;
  _data_size = . - _data_start;
  _end = .;
  _heap_start = _data_start + _data_size;
  _heap_end   = ORIGIN(VMA) + 0x1000;
  _stack_top = ALIGN(0x1000);
  . = _stack_top + 0x1000;  /*栈顶，为堆区结束的位置*/
  _stack_pointer = .;   /*栈底，为VMA结束的位置*/
  /* end = .;
  . = ORIGIN(VMA);
  _stack_top = ALIGN(0x1000); 
  . = _stack_top + 0x8000;
  _stack_pointer = .;         
  
  _end = .;
  _heap_start = ALIGN(0x1000); */

  /* _stack_top = ALIGN(0x1000);
  . = _stack_top + 0x8000;
  _stack_pointer = .;
  end = .;
  _end = .;
  _heap_start = ALIGN(0x1000); */
}
