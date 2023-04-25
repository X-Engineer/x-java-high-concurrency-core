package com.crazymakercircle.visiable;

/**
 * 有序性问题
 * 所谓程序的有序性，是指程序按照代码的先后顺序执行。如果程序执行的顺序与代码的先后顺序不同，并导致了错误的结果，即发生了有序性问题。
 *
 * 对于下面的程序来说，(0,0)结果是错误的，意味着已经发生了并发的有序性问题。为什么会出现(0,0)结果呢？可能在程序的执行过程中发生了指令重排序（Reordering）。
 * 什么是指令重排：
 * 一般来说，CPU为了提高程序运行效率，可能会对输入代码进行优化，它不保证程序中各个语句的执行顺序同代码中的先后顺序一致，但是它会保证程序最终的执行结果和代码顺序执行的结果是一致的
 * 重排序也是单核时代非常优秀的优化手段，有足够多的措施保证其在单核下的正确性。在多核时代，如果工作线程之间不共享数据或仅共享不可变数据，重排序也是性能优化的利器。
 * 然而，如果工作线程之间共享了可变数据，由于两种重排序的结果都不是固定的，因此会导致工作线程似乎表现出了随机行为。即指令重排序不会影响单个线程的执行，但是会影响多个线程并发执行的正确性。
 *
 * 事实上，输出了乱序的结果，并不代表一定发生了指令重排序，内存可见性问题也会导致这样的输出。但是，指令重排序也是导致乱序的原因之一。
 * 总之，要想并发程序正确地执行，必须要保证原子性、可见性以及有序性。只要有一个没有得到保证，就有可能会导致程序运行不正确。
 *
 */
public class InstructionReorder {
    private /*volatile*/ static int x = 0, y = 0;
    private /*volatile*/ static int a = 0, b = 0;

    public static void main(String[] args) throws InterruptedException {
        int i = 0;
        for (; ; ) {
            i++;
            x = 0;
            y = 0;
            a = 0;
            b = 0;
            Thread one = new Thread(new Runnable() {
                public void run() {
                    a = 1;   //①
                    x = b;    //②
                }
            });

            Thread other = new Thread(new Runnable() {
                public void run() {
                    b = 1;  //③
                    y = a;  //④
                }
            });
            one.start();
            other.start();
            one.join();
            other.join();
            String result = "第" + i + "次 (" + x + "," + y + "）";
            if (x == 0 && y == 0) {
                System.err.println(result);
            }
        }
    }
}