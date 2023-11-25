BUILD_DIR = ./build
TOPNAME=SimTop
export PATH := $(PATH):$(abspath ./utils)
$(shell mkdir -p $(BUILD_DIR))
IMG?=

NXDC_FILES = constr/SimTop.nxdc

VERILATOR = verilator
VERILATOR_CFLAGS += -MMD --build -cc  
VERILATOR_CFLAGS += --timing --trace

# VERILATOR_CFLAGS += -O3 --x-assign fast --x-initial fast --noassert 
#优化加速
				
INC_PATH ?=
OBJ_DIR = $(BUILD_DIR)/obj_dirtop
BIN = $(BUILD_DIR)/$(TOPNAME)

VSRCS = $(shell find $(abspath ./vsrc) -name "*.sv")
CSRCS = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
INCFLAGS = $(addprefix -I, $(INC_PATH))
CXXFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""
$(info #building riscv32e-npc)
$(info #IMG=$(IMG))

$(BIN): $(VSRCS) $(CSRCS)
	@rm -rf $(OBJ_DIR)
	$(VERILATOR) $(VERILATOR_CFLAGS) \
		--top-module $(TOPNAME) $^ \
		$(addprefix -CFLAGS , $(CXXFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))

test:
	mill -i __.test

verilog:
	$(call git_commit, "generate verilog")
	rm -rf ./vsrc/*
	mkdir -p ./vsrc
	mill -i __.test.runMain TopMain -td ./vsrc

help:
	mill -i __.test.runMain TopMain --help

compile:
	mill -i __.compile

bsp:
	mill -i mill.bsp.BSP/install

reformat:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat

clean:
	-rm -rf $(BUILD_DIR)

cnt_v := $(shell find vsrc/ -name "*.sv" -or -name "*.scala" | xargs grep -ve "^$$" | wc -l)
cnt_s := $(shell find playground/ -name "*.sv" -or -name "*.scala" | xargs grep -ve "^$$" | wc -l)
count:
	@echo $(cnt_v)-sverilog-lines in npc/vsrc	  
	@echo $(cnt_s)--chisel--lines in npc/playground
.PHONY: test verilog help compile bsp reformat checkformat clean count

sim:$(BIN)
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@$^ $(IMG)

-include ../Makefile