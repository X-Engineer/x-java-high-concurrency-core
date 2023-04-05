package com.crazymakercircle.mutithread.basic.threadlocal;

import com.crazymakercircle.util.Print;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

import static com.crazymakercircle.util.ThreadUtil.sleepMilliSeconds;
import static com.crazymakercircle.util.ThreadUtil.sleepSeconds;

/**
 * Created by 尼恩@疯狂创客圈.
 * 由于ThreadLocal使用不当会导致严重的内存泄漏问题，所以为了更好地避免内存泄漏问题的发生，我们使用ThreadLocal时遵守以下两个原则：
 * （1）尽量使用private static final修饰ThreadLocal实例。使用private与final修饰符主要是为了尽可能不让他人修改、变更ThreadLocal变量的引用，使用static修饰符主要是为了确保ThreadLocal实例的全局唯一。
 * （2）ThreadLocal使用完成之后务必调用remove()方法。这是简单、有效地避免ThreadLocal引发内存泄漏问题的方法。
 * 使用ThreadLocal能实现每个线程都有一份变量的本地值，其原因是每个线程都有自己独立的ThreadLocalMap空间，本质上属于以空间换时间的设计思路，该设计思路属于另一种意义的“无锁编程”。
 */
public class ThreadLocalTest2 {
    /**
     * 模拟业务方法
     */
    public void serviceMethod() {
        //睡眠500ms,模拟执行耗时
        sleepMilliSeconds(500);

        //记录开始调用到这个点p1的耗时
        SpeedLog.logPoint("point-1 service");

        //调用下一层方法：模拟dao业务方法
        daoMethod();

        //调用下一层方法：模拟RPC远程业务方法
        rpcMethod();

    }

    /**
     * 模拟dao业务方法
     */
    public void daoMethod() {
        //睡眠400ms,模拟执行耗时
        sleepMilliSeconds(400);

        //记录上一个点p1到这里p2的耗时
        SpeedLog.logPoint("point-2 dao");

    }

    /**
     * 模拟RPC远程业务方法
     */
    public void rpcMethod() {
        //睡眠400ms,模拟执行耗时
        sleepMilliSeconds(600);

        //记录上一个点p2到这里p3的耗时
        SpeedLog.logPoint("point-3 rpc");

    }

    /**
     * 测试用例：线程方法调用的耗时
     */
    @org.junit.Test
    public void testSpeedLog() throws InterruptedException {
        Runnable runnable = () ->
        {
            //开始耗时记录
            SpeedLog.beginSpeedLog();

            //调用模拟业务方法
            serviceMethod();

            //打印耗时
            SpeedLog.printCost();

            //结束耗时记录
            SpeedLog.endSpeedLog();

        };
        new Thread(runnable).start();
        sleepSeconds(10);//等待10s看结果
    }


    @org.junit.Test
    public void testSpeedLogThreadPool() throws InterruptedException {
        SpeedLogThreadPool threadPool = new SpeedLogThreadPool();
        CountDownLatch latch = new CountDownLatch(2);

        Runnable runnable = () ->
        {
            //调用模拟业务方法
            serviceMethod();
            latch.countDown();
        };

        for (int i = 0; i < 2; i++) {
            threadPool.submit(runnable);
        }
        latch.await();
    }


    @org.junit.Test
    public void testWeakReference() throws InterruptedException {
        Foo foo = new Foo();
        WeakReference<Foo> reference = new WeakReference<Foo>(foo);
        foo = null; // help for gc
        System.gc();

        if (reference.get() == null) {
            Print.tco(" reference value 已经被GC回收");
        } else {
            Print.tco(" reference value =" + reference.get());
        }

    }

    @org.junit.Test
    public void testMemLeak() throws InterruptedException {
        Foo foo = new Foo();
        ThreadLocal<Foo> fooLocalRef = foo.fooThreadLocal;
        fooLocalRef.get();
        Thread threadRef = Thread.currentThread();

        Print.tcfo("GC前 key => " +
                Foo.threadLocalKey(threadRef));
        Print.tcfo("GC前 threadLocal object => " +
                Foo.threadLocalGet(threadRef));

        // 设置为null，然后触发垃圾回收
        fooLocalRef = null;
        foo.fooThreadLocal = null;
        System.gc();//触发垃圾回收

        Print.tcfo("GC后 key  => " +
                Foo.threadLocalKey(threadRef));
        Print.tcfo("GC后 threadLocal object  => " +
                Foo.threadLocalGet(threadRef));

    }
}

