AM_SRCS := riscv/soc/start.S \
					 riscv/soc/trm.c


CFLAGS    += -fdata-sections -ffunction-sections
LDFLAGS   += -T $(AM_HOME)/am/src/riscv/soc/linker.ld \
						 --defsym=_pmem_start=0x20000000 --defsym=_entry_offset=0x0
CFLAGS += -DMAINARGS=\"$(mainargs)\"

NPCFLAGS+=-l $(shell dirname $(IMAGE).elf)/npc-log.txt
NPCFLAGS+=-b 

.PHONY: $(AM_HOME)/am/src/riscv/npc/trm.c

image: $(IMAGE).elf
	@$(OBJDUMP) -d $(IMAGE).elf > $(IMAGE).txt
	@echo + OBJCOPY "->" $(IMAGE_REL).bin
	@$(OBJCOPY) -S --set-section-flags .bss=alloc,contents -O binary $(IMAGE).elf $(IMAGE).bin

run: image
	$(MAKE) -C $(NPC_HOME) sim ARGS="$(NPCFLAGS)" IMG=$(IMAGE).bin