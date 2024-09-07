#ifndef NPC_MONITOR_H
#define NPC_MONITOR_H

void step_and_dump_wave();
void init_difftest(char *ref_so_file, long img_size, int port);
void init_device();
void init_mem();
void init_traces();

#endif