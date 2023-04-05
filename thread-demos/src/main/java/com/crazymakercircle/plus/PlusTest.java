package com.crazymakercircle.plus;

import com.crazymakercircle.util.Print;

import java.util.concurrent.CountDownLatch;

/**
 * Created by 尼恩@疯狂创客圈.
 */
public class PlusTest {
    final int MAX_TREAD = 10;
    final int MAX_TURN = 1000;
    CountDownLatch latch = new CountDownLatch(MAX_TREAD);

    /**
     * 测试用例：测试不安全的累加器
     * 临界区资源表示一种可以被多个线程使用的公共资源或共享数据，但是每一次只能有一个线程使用它。一旦临界区资源被占用，想使用该资源的其他线程则必须等待。
     * 临界区代码段（Critical Section）是每个线程中访问临界资源的那段代码，多个线程必须互斥地对临界区资源进行访问.
     * 竞态条件（Race Conditions）可能是由于在访问临界区代码段时没有互斥地访问而导致的特殊情况。如果多个线程在临界区代码段的并发执行结果可能因为代码的执行顺序不同而不同，我们就说这时在临界区出现了竞态条件问题。
     * amount为临界区资源，selfPlus()可以理解为临界区代码段
     */
    @org.junit.Test
    public void testNotSafePlus() throws InterruptedException {
        NotSafePlus counter = new NotSafePlus();
        Runnable runnable = () ->
        {
            for (int i = 0; i < MAX_TURN; i++) {
                /**
                 * “内存取值”“寄存器增加1”和“存值到内存”这三个JVM指令本身是不可再分的，它们都具备原子性，是线程安全的，也叫原子操作
                 * 但是，两个或者两个以上的原子操作合在一起进行操作就不再具备原子性了。
                 */
                counter.selfPlus();
            }
            latch.countDown();
        };
        for (int i = 0; i < MAX_TREAD; i++) {
            new Thread(runnable).start();
        }
        /**
         * CountDownLatch（倒数闩）是一个非常实用的等待多线程并发的工具类。调用线程可以在倒数闩上进行等待，一直等待倒数闩的次数减少到0，才继续往下执行。
         * 每一个被等待的线程执行完成之后进行一次倒数。所有被等待的线程执行完成之后，倒数闩的次数减少到0，调用线程可以往下执行，从而达到并发等待的效果。
         */
        latch.await();
        Print.tcfo("理论结果：" + MAX_TURN * MAX_TREAD);
        Print.tcfo("实际结果：" + counter.getAmount());
        Print.tcfo("差距是：" + (MAX_TURN * MAX_TREAD - counter.getAmount()));
    }

    /**
     * 测试用例：安全的累加器
     * synchronized关键字是Java的保留字，当使用synchronized关键字修饰一个方法的时候，该方法被声明为同步方法
     * 在方法声明中设置synchronized同步关键字，保证其方法的代码执行流程是排他性的。
     * 任何时间只允许一个线程进入同步方法（临界区代码段），如果其他线程需要执行同一个方法，那么只能等待和排队。
     */
    @org.junit.Test
    public void testSafePlus() throws InterruptedException {
        SafePlus counter = new SafePlus();
        Runnable runnable = () ->
        {
            for (int i = 0; i < MAX_TURN; i++) {
                counter.selfPlus();
            }
            latch.countDown();
        };
        for (int i = 0; i < MAX_TREAD; i++) {
            new Thread(runnable).start();
        }
        latch.await();
        Print.tcfo("理论结果：" + MAX_TURN * MAX_TREAD);
        Print.tcfo("实际结果：" + counter.getAmount());
        Print.tcfo("差距是：" + (MAX_TURN * MAX_TREAD - counter.getAmount()));
    }


}

