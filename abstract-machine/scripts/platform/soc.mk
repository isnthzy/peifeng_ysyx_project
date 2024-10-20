AM_SRCS := riscv/soc/start.S \
					 riscv/soc/ssbl.S  \
					 riscv/soc/trm.c	 \
					 riscv/soc/ioe.c   \
					 riscv/soc/timer.c \
					 riscv/soc/cte.c   \
					 riscv/soc/gpu.c   \
					 riscv/soc/vme.c   \
					 riscv/soc/mpe.c   \
					 riscv/soc/input.c \
					 riscv/soc/trap.S  


CFLAGS    += -fdata-sections -ffunction-sections
ifeq ($(NAME),rtthread)
	LDFLAGS   += -T $(AM_HOME)/am/src/riscv/soc/rtt-linker.ld 
else
	LDFLAGS   += -T $(AM_HOME)/am/src/riscv/soc/linker.ld 
endif

LDFLAGS   += --gc-sections -e _start
CFLAGS 		+= -DMAINARGS=\"$(mainargs)\"

NPCFLAGS+=-l $(shell dirname $(IMAGE).elf)/npc-log.txt
NPCFLAGS+=--diff=$(NEMU_HOME)/build/riscv32-nemu-interpreter-so
NPCFLAGS+=-b 

.PHONY: $(AM_HOME)/am/src/riscv/npc/trm.c

image: $(IMAGE).elf
	@$(OBJDUMP) -d $(IMAGE).elf > $(IMAGE).txt
	@echo + OBJCOPY "->" $(IMAGE_REL).bin
	@$(OBJCOPY) -S --set-section-flags .bss=alloc,contents -O binary $(IMAGE).elf $(IMAGE).bin

run: image
	$(MAKE) -C $(NPC_HOME) sim ARGS="$(NPCFLAGS)" IMG=$(IMAGE).bin