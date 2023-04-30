package com.crazymakercircle.visiable;

/**
 * 多核情况下，所有的CPU操作都会涉及缓存一致性协议（MESI协议）校验，该协议用于保障内存可见性。但是，缓存一致性协议仅仅保障内存弱可见（高速缓存失效），没有保障共享变量的强可见，而且缓存一致性协议更不能禁止CPU重排序，也就是不能确保跨CPU指令的有序执行。
 * 如何保障跨CPU指令重排序之后的程序结果正确呢？需要用到内存屏障。
 *
 * 硬件层常用的内存屏障分为三种：读屏障（Load Barrier）、写屏障（Store Barrier）和全屏障（Full Barrier）。
 * 硬件层的内存屏障的作用
 * （1）阻止屏障两侧的指令重排序
 * 编译器和CPU可能为了使性能得到优化而对指令重排序，但是插入一个硬件层的内存屏障相当于告诉CPU和编译器先于这个屏障的指令必须先执行，后于这个屏障的指令必须后执行。
 * （2）强制让高速缓存的数据失效
 * 硬件层的内存屏障强制把高速缓存中的最新数据写回主存，让高速缓存中相应的脏数据失效。一旦完成写入，任何访问这个变量的线程将会得到最新的值。
 *
 * 下面是一段可能乱序执行的代码：
 * 控制台所输出的x值可能是0或8。为什么x可能会输出0呢？
 * 主要原因是：update()和show()方法可能在两个CPU内核并发执行，语句①和语句②如果发生了重排序，那么show()方法输出的x就可能为0。如果输出的x结果是0，显然不是程序的正常结果。
 *
 * 如何确保ReorderExample的并发运行结果正确呢？可以通过内存屏障进行保障。Java语言没有办法直接使用硬件层的内存屏障，只能使用含有JMM内存屏障语义的Java关键字，这类关键字的典型为volatile。使用volatile关键字对实例中的x进行修饰
 * //class ReorderExample {
 * //    volatile int a = 0;
 * //    boolean flag = false;
 * //
 * //    public void writer() {
 * //        a = 8;                   //1
 * //        flag = true;             //2
 * //    }
 * //
 * //    public void reader() {
 * //        if (flag) //3
 * //        {
 * //            System.out.println(a); //4
 * //        }
 * //    }
 * //}
 * volatile含有JMM全屏障的语义，要求JVM编译器在语句①的后面插入全屏障指令。该全屏障确保x的最新值对所有的后序操作是可见的（含跨CPU场景），并且禁止编译器和处理器对语句①和语句②进行重排序。
 */
class ReorderExample {
    int a = 0;
    boolean flag = false;

    public void writer() {
        a = 8;                   //1
        flag = true;             //2
    }

    public void reader() {
        if (flag) //3
        {
            System.out.println(a); //4
        }
    }
}


