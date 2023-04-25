package com.crazymakercircle.visiable;

/**
 * 由于需要尽可能释放CPU的能力，因此在CPU上不断增加内核和缓存。内核是越加越多，从之前的单核演变成8核、32核甚至更多。缓存也不止一层，可能是2层、3层甚至更多。
 * 随着CPU内核和缓存的增加，导致了并发编程的可见性和有序性问题。
 *
 * 原子性问题
 * 所谓原子操作，就是“不可中断的一个或一系列操作”，是指不会被线程调度机制打断的操作。这种操作一旦开始，就一直运行到结束，中间不会有任何线程的切换。
 *
 * javap -c CounterSample.class
 * 输出：
 *Compiled from "CounterSample.java"
 * class com.crazymakercircle.visiable.CounterSample {
 *   int sum;
 *
 *   com.crazymakercircle.visiable.CounterSample();
 *     Code:
 *        0: aload_0
 *        1: invokespecial #1                  // Method java/lang/Object."<init>":()V
 *        4: aload_0
 *        5: iconst_0
 *        6: putfield      #2                  // Field sum:I
 *        9: return
 *
 *   public void increase();
 *     Code:
 *        0: aload_0
 *        1: dup
 *        2: getfield      #2     // Field sum:I    ①
 *        5: iconst_1                                  ②
 *        6: iadd                                       ③
 *        7: putfield      #2     // Field sum:I    ④
 *       10: return
 * }
 *
 * ① 获取当前sum变量的值，并且放入栈顶。
 * ② 将常量1放入栈顶。
 * ③ 将当前栈顶中的两个值（sum的值和1）相加，并把结果放入栈顶。
 * ④ 把栈顶的结果再赋值给sum变量。
 */
class CounterSample {
    int sum = 0;

    public void increase() {
        sum++;           //①②③⑤⑦④
    }
}

