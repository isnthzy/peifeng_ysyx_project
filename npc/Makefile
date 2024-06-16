BUILD_DIR = ./build
TOPNAME=SimTop
export PATH := $(PATH):$(abspath ./utils)
$(shell mkdir -p $(BUILD_DIR))
IMG?=
ARGS?= --log=$(BUILD_DIR)/npc-log.txt

# NXDC_FILES = constr/SimTop.nxdc

VERILATOR = verilator
VERILATOR_CFLAGS += -MMD --build -cc 
VERILATOR_CFLAGS += --trace --timing
# VERILATOR_CFLAGS += --prof-cfuncs 
# VERILATOR_CFLAGS += -O3 --x-assign fast --x-initial fast --noassert 
#优化加速
				
INC_PATH ?=
OBJ_DIR = $(BUILD_DIR)/obj_dirtop
BIN = $(BUILD_DIR)/$(TOPNAME)

VSRCS = $(shell find $(abspath ./vsrc) -name "*.sv" -or -name "*.v")
CSRCS = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cpp" -or -name "*.cc")
INCFLAGS = $(addprefix -I, $(INC_PATH))
CXXFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\"" 
CXXFLAGS += -Wall -Werror 
LDFLAGS += -lreadline  -lSDL2

# CXXFLAGS += -ggdb -DVL_DEBUG=1 -D_GLIBCXX_DEBUG -fsanitize=address,undefined
# LDFLAGS  += -ggdb -fsanitize=address,undefined
#gdb调试段错误相关代码

##分隔符 llvm用于反汇编itrace
CXXFLAGS += $(filter-out -D__STDC_FORMAT_MACROS, $(shell llvm-config --cxxflags)) -fPIE
LDFLAGS += $(shell llvm-config --libs)
##分隔符 llvm用于反汇编itrace

$(info #building riscv32e-npc)
$(info #IMG=$(IMG))
$(info #ARGS=$(ARGS))

#gtkwave自动保存信号和打开信号
gtk:
	gtkwave dump.vcd allview.gtkw

com: $(VSRCS) $(CSRCS)
	@rm -rf ./build/SimTop
	@$(VERILATOR) $(VERILATOR_CFLAGS) \
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
	-rm -rf ./vsrc/*

cnt_cp:= $(shell find csrc/ -name "*.cpp" -or -name "*.h" | xargs grep -ve "^$$" | wc -l)
cnt_v := $(shell find vsrc/ -name "*.sv" -or -name "*.scala" | xargs grep -ve "^$$" | wc -l)
cnt_s := $(shell find playground/ -name "*.sv" -or -name "*.scala" | xargs grep -ve "^$$" | wc -l)
count:
	@echo $(cnt_cp)-cpp-lines in npc/vsrc	
	@echo $(cnt_v)-sverilog-lines in npc/vsrc	  
	@echo $(cnt_s)--chisel--lines in npc/playground
.PHONY: test verilog help compile bsp reformat checkformat clean count gdb sim

sim:com
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	$(BIN) $(ARGS) $(IMG)

gdb:com
	$(call git_commit, "gdb RTL") # DO NOT REMOVE THIS LINE!!!
	gdb -ex "handle SIGSEGV noprint nostop" -ex "set print thread-events off" $(BIN) $(ARGS) $(IMG)
-include ../Makefile