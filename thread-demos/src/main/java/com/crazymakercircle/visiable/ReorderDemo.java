package com.crazymakercircle.visiable;

/**
 * 为了重复释放硬件的高性能，编译器、CPU会优化待执行的指令序列，包括调整某些指令的顺序执行。优化的结果，指令执行顺序会与代码顺序略有不同，可能会导致代码执行出现有序性问题。
 * 内存屏障又称内存栅栏（Memory Fences），是一系列的CPU指令，它的作用主要是保证特定操作的执行顺序，保障并发执行的有序性。在编译器和CPU都进行指令的重排优化时，可以通过在指令间插入一个内存屏障指令，告诉编译器和CPU，禁止在内存屏障指令前（或后）执行指令重排序。
 *
 * 编译器和CPU常常会对指令进行重排序。重排序主要分为两类：编译器重排序和CPU重排序
 * - 编译器重排序：编译器重排序指的是在代码编译阶段进行指令重排，不改变程序执行结果的情况下，为了提升效率，编译器对指令进行乱序（Out-of-Order）的编译。
 * - CPU 重排序：流水线（Pipeline）和乱序执行（Out-of-Order Execution）是现代CPU基本都具有的特性。为了CPU的执行效率，流水线都是并行处理的，在不影响语义的情况下。处理次序（Process Ordering，机器指令在CPU实际执行时的顺序）和程序次序（Program Ordering，程序代码的逻辑执行顺序）是允许不一致的，只要满足As-if-Serial规则即可
 * CPU重排序包括两类：指令级重排序和内存系统重排序。
 *
 * As-if-Serial规则的具体内容为：无论如何重排序，都必须保证代码在单线程下运行正确。
 * JIT 是Just In Time的缩写，也就是“即时编译器”。JVM读入“.class”文件的字节码后，默认情况下是解释执行的。但是对于运行频率很高（如大于5000次）的字节码，JVM采用了JIT技术，将直接编译为机器指令，以提高性能。
 *
 * 虽然编译器和CPU遵守了As-if-Serial规则，无论如何，也只能在单CPU执行的情况下保证结果正确。在多核CPU并发执行的场景下，由于CPU的一个内核无法清晰分辨其他内核上指令序列中的数据依赖关系，因此可能出现乱序执行，从而导致程序运行结果错误。
 * 所以，As-if-Serial 规则只能保障单内核指令重排序之后的执行结果正确，不能保障多内核以及跨CPU指令重排序之后的执行结果正确。
 */
class ReorderDemo {
    int value = 10;
    boolean flag = false;
    int doubleValue = 0;

    public void write() {
        value = 100;      //①
        flag = true;    //②
    }

    public void changeValue() {
        if (flag)    //③
        {
            doubleValue = value + value;//④
        }
    }

    public static void main(String[] args)
            throws InterruptedException {
        int i = 0;
        for (; ; ) {
            i++;

            ReorderDemo reorderDemo = new ReorderDemo();
            Thread one = new Thread(new Runnable() {
                public void run() {
                    reorderDemo.write();
                }
            });
            Thread two = new Thread(new Runnable() {
                public void run() {
                    reorderDemo.changeValue();
                }
            });
            one.start();
            two.start();
            one.join();
            two.join();

            if (reorderDemo.doubleValue == 20) {
                //第2770518次操作发生了指令重排
                String result = "第" + i + "次操作发生了指令重排";
                System.err.println(result);
            }
        }
    }
}
