# Todo

- [x] 重写npc单发射处理器，为csr，cache等做考虑，小驼峰命名法
- [x] 重构difftest为类Chiplab，香山？
- [x] 内存difftest完成！
- [x] mtrace!
- [x] 加入死锁检查
- [x] 尝试学习scala语法，在npc上应用(学习了，但是用不了一点)
- [x] npc csrc review&重构
- [x] fst支持！
- [ ] kconfig配置！
- [ ] 保留最后的波形！
- [x] 加入加速仿真支持（二次波形位置追踪）

/*
关于保留最后波形和加速仿真支持的设计思路。
每次执行完npc后都会在log里输出如下信息,建议在xxx clock开启波形
然后再执行后，只需要输入 make XXX WAVE=xxx即可(xxx即为开启波形的位置)
make WAVE=0不开启波形，WAVE=1从开始时开启波形（反正reset那么长）
WAVE的npc config.h总宏默认开启，关闭后WAVE参数失效

*/

- [x] 加入Icache的支持
- [x] 启动soc计划
- [ ] 读NutShell!!!
- [ ] 由于不再是单步运行，因此跳跃可能不再管用了，考虑改成定点跳转（即跳到对应序号的跳转）

