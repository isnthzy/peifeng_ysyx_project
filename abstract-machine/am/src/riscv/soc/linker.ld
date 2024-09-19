ENTRY(_start)
PHDRS { text PT_LOAD; data PT_LOAD; }
MEMORY {
  mrom (rx) : ORIGIN = 0x20000000, LENGTH = 4K
  sram (wx) : ORIGIN = 0x0f000000, LENGTH = 8K
}

SECTIONS {
  /* _pmem_start and _entry_offset are defined in LDFLAGS */
  . = ORIGIN(mrom);
  .text : {
    *(entry)
    *(.text*)
  } > mrom AT> mrom : text
  etext = .;
  _etext = .;
  .rodata : {
    *(.rodata*)
  }
  .data : {
    _data_start = .;
    *(.data)
  } > sram AT> mrom : data
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
  _heap_end   = ORIGIN(sram) + 0x1000;
  _stack_top = ALIGN(0x1000);
  . = _stack_top + 0x1000;  /*栈顶，为堆区结束的位置*/
  _stack_pointer = .;   /*栈底，为sram结束的位置*/
  /* end = .;
  . = 0x0f000000;
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
