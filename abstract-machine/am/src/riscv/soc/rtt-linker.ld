PHDRS {
  start PT_LOAD;
  ssbl  PT_LOAD;
  text  PT_LOAD;  
  rodata PT_LOAD;
  data  PT_LOAD; 
  data.extra PT_LOAD;
  bss   PT_LOAD;
  bss.extra PT_LOAD;
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
  } > LMA AT> LMA : start

  .ssbl : ALIGN(10){
    *(.ssbl*)
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
  .data.extra : ALIGN(10){
    /* section information for finsh shell */
    __fsymtab_start = .;
    KEEP(*(FSymTab))
    __fsymtab_end = .;
    . = ALIGN(8);
    __vsymtab_start = .;
    KEEP(*(VSymTab))
    __vsymtab_end = .;
    . = ALIGN(8);

    /* section information for initial. */
    . = ALIGN(8);
    __rt_init_start = .;
    KEEP(*(SORT(.rti_fn*)))
    __rt_init_end = .;
    . = ALIGN(8);

    __rt_utest_tc_tab_start = .;
    KEEP(*(UtestTcTab))
    __rt_utest_tc_tab_end = .;

    . = ALIGN(8);
    __am_apps_data_start = .;
    *(__am_apps.data*)
    *(__am_apps.sdata*)
    __am_apps_data_end = .;
    . = ALIGN(8);
  } > VMA AT> LMA : data.extra
  .data :  ALIGN(10){
    *(.data*)
    *(.sdata*)
  } > VMA AT> LMA : data
  edata = .;
  _data = .;
  .bss.extra : ALIGN(10){
    . = ALIGN(8);
    __am_apps_bss_start = .;
    *(__am_apps.bss*)
    *(__am_apps.sbss*)
    *(__am_apps.scommon*)
    __am_apps_bss_end = .;
    . = ALIGN(8);
  } > VMA AT> LMA : bss.extra
  .bss :  ALIGN(10){
    *(.bss*)
    *(.sbss*)
    *(.scommon)
  } > VMA AT> LMA : bss  
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


