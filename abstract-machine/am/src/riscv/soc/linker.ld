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
  . = ORIGIN(LMA);
  .text : {
    *(entry)
    *(.text*)
  } > LMA AT> LMA : text
  etext = .;
  _etext = .;
  .rodata : {
    *(.rodata*)
    *(.srodata*)
  } > LMA
  rodata_end = .;
  .data : AT(rodata_end){
    _data_start = .;
    *(.data*)
    *(.sdata*)
  } > VMA : data
  _data_load_start = LOADADDR(.data);
  _data_size = SIZEOF(.data);
  edata = .;
  _data = .;
  .bss : {
	  _bss_start = .;
    *(.bss*)
    *(.sbss*)
    *(.scommon)
  } > VMA : data /*这里有坑，定义了多个MEMORY后如果为指定存储区域会分配给sram而不是psram*/
  end = .;
  _end = .;
  _heap_start = _data_start + _data_size;
  _heap_end   = ORIGIN(VMA) + 0x1000;
  _stack_top = ALIGN(0x1000);
  . = _stack_top + 0x1000;  /*栈顶，为堆区结束的位置*/
  _stack_pointer = .;   /*栈底，为VMA结束的位置*/
}
