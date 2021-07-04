###1.串行垃圾回收
java -XX:+UseSerialGC -Xms128m -Xmx128m -XX:+PrintGCDetails GCLogAnalysis
java -XX:+UseSerialGC -Xms256m -Xmx256m -XX:+PrintGCDetails GCLogAnalysis
java -XX:+UseSerialGC -Xms512m -Xmx512m -XX:+PrintGCDetails GCLogAnalysis
java -XX:+UseSerialGC -Xms1g -Xmx1g -XX:+PrintGCDetails GCLogAnalysis

Serial GC(串行GC)
Serial GC 对年轻代使用 mark-copy(标记-复制) 算法, 对老年代使用 mark-sweep-compact(标记-清除-整理)算法. 顾名思义, 两者都是单线程的垃圾收集器,不能进行并行处理。两者都会触发全线暂停(STW),停止所有的应用线程。
因此这种GC算法不能充分利用多核CPU。不管有多少CPU内核, JVM 在垃圾收集时都只能使用单个核心。
缺点：资源闲置, 多的CPU资源也不能用来降低延迟,也不能用来增加吞吐量。
###2.并行GC
java -XX:+UseParallelGC -Xms128m -Xmx128m -XX:+PrintGCDetails GCLogAnalysis
java -XX:+UseParallelGC -Xms256m -Xmx256m -XX:+PrintGCDetails GCLogAnalysis
java -XX:+UseParallelGC -Xms512m -Xmx512m -XX:+PrintGCDetails GCLogAnalysis
java -XX:+UseParallelGC -Xms1g -Xmx1g -XX:+PrintGCDetails GCLogAnalysis

Parallel GC(并行GC)
并行垃圾收集器这一类组合, 在年轻代使用 标记-复制(mark-copy)算法, 在老年代使用 标记-清除-整理(mark-sweep-compact)算法。年轻代和老年代的垃圾回收都会触发STW事件,暂停所有的应用线程来执行垃圾收集。两者在执行 标记和 复制/整理阶段时都使用多个线程, 因此得名“(Parallel)”。通过并行执行, 使得GC时间大幅减少。
并行垃圾收集器适用于多核服务器,主要目标是增加吞吐量。因为对系统资源的有效使用,能达到更高的吞吐量:
在GC期间, 所有 CPU 内核都在并行清理垃圾, 所以暂停时间更短
在两次GC周期的间隔期, 没有GC线程在运行,不会消耗任何系统资源
另一方面, 因为此GC的所有阶段都不能中断, 所以并行GC很容易出现长时间的卡顿. 如果延迟是系统的主要目标, 那么就应该选择其他垃圾收集器组合。

###3.CMS
java -XX:+UseConcMarkSweepGC -Xms128m -Xmx128m -XX:+PrintGCDetails GCLogAnalysis
java -XX:+UseConcMarkSweepGC -Xms256m -Xmx256m -XX:+PrintGCDetails GCLogAnalysis
java -XX:+UseConcMarkSweepGC -Xms512m -Xmx512m -XX:+PrintGCDetails GCLogAnalysis
java -XX:+UseConcMarkSweepGC -Xms1g -Xmx1g -XX:+PrintGCDetails GCLogAnalysis

Concurrent Mark and Sweep(并发标记-清除)
CMS的官方名称为 “Mostly Concurrent Mark and Sweep Garbage Collector”(主要并发-标记-清除-垃圾收集器). 其对年轻代采用并行 STW方式的 mark-copy (标记-复制)算法, 对老年代主要使用并发 mark-sweep (标记-清除)算法。
CMS的设计目标是避免在老年代垃圾收集时出现长时间的卡顿。主要通过两种手段来达成此目标。
第一, 不对老年代进行整理, 而是使用空闲列表(free-lists)来管理内存空间的回收。
第二, 在 mark-and-sweep (标记-清除) 阶段的大部分工作和应用线程一起并发执行。
也就是说, 在这些阶段并没有明显的应用线程暂停。但值得注意的是, 它仍然和应用线程争抢CPU时间。默认情况下, CMS 使用的并发线程数等于CPU内核数的 1/4。

CMS会比并行GC的吞吐量差一些，但却可以减少每一次GC停顿的时间。CMS垃圾收集器在减少停顿时间上做了很多给力的工作, 大量的并发线程执行的工作并不需要暂停应用线程。 当然, CMS也有一些缺点,其中最大的问题就是老年代内存碎片问题, 在某些情况下GC会造成不可预测的暂停时间, 特别是堆内存较大的情况下。

###4.G1
java -XX:+UseG1GC -Xms128m -Xmx128m -XX:+PrintGC GCLogAnalysis
java -XX:+UseG1GC -Xms256m -Xmx256m -XX:+PrintGC GCLogAnalysis
java -XX:+UseG1GC -Xms512m -Xmx512m -XX:+PrintGC GCLogAnalysis
java -XX:+UseG1GC -Xms1g -Xmx1g -XX:+PrintGC GCLogAnalysis

G1 解决了 CMS 中的各种疑难问题, 包括暂停时间的可预测性, 并终结了堆内存的碎片化。对单业务延迟非常敏感的系统来说, 如果CPU资源不受限制,那么G1可以说是 HotSpot 中最好的选择, 特别是在最新版本的Java虚拟机中。当然,这种降低延迟的优化也不是没有代价的: 由于额外的写屏障(write barriers)和更积极的守护线程, G1的开销会更大。所以, 如果系统属于吞吐量优先型的, 又或者CPU持续占用100%, 而又不在乎单次GC的暂停时间, 那么CMS是更好的选择。
G1适合大内存,需要低延迟的场景。