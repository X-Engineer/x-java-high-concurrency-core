package com.crazymakercircle.mutithread.basic.threadlocal;

import com.crazymakercircle.util.Print;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

import static com.crazymakercircle.util.ThreadUtil.sleepMilliSeconds;
import static com.crazymakercircle.util.ThreadUtil.sleepSeconds;

/**
 * Created by 尼恩@疯狂创客圈.
 */
public class ThreadLocalTest2
{
    /**
     * 模拟业务方法
     */
    public void serviceMethod()
    {
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
    public void daoMethod()
    {
        //睡眠400ms,模拟执行耗时
        sleepMilliSeconds(400);

        //记录上一个点p1到这里p2的耗时
        SpeedLog.logPoint("point-2 dao");

    }

    /**
     * 模拟RPC远程业务方法
     */
    public void rpcMethod()
    {
        //睡眠400ms,模拟执行耗时
        sleepMilliSeconds(600);

        //记录上一个点p2到这里p3的耗时
        SpeedLog.logPoint("point-3 rpc");

    }

    /**
     * 测试用例：线程方法调用的耗时
     */
    @org.junit.Test
    public void testSpeedLog() throws InterruptedException
    {
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
    public void testSpeedLogThreadPool() throws InterruptedException
    {
        SpeedLogThreadPool threadPool = new SpeedLogThreadPool();
        CountDownLatch latch = new CountDownLatch(2);

        Runnable runnable = () ->
        {
            //调用模拟业务方法
            serviceMethod();
            latch.countDown();
        };

        for (int i = 0; i < 2; i++)
        {
            threadPool.submit(runnable);
        }
        latch.await();
    }


    @org.junit.Test
    public void testWeakReference() throws InterruptedException
    {
        Foo foo = new Foo();
        WeakReference<Foo> reference = new WeakReference<Foo>(foo);
        foo = null; // help for gc
        System.gc();

        if (reference.get() == null)
        {
            Print.tco(" reference value 已经被GC回收");
        } else
        {
            Print.tco(" reference value =" + reference.get());
        }

    }

    @org.junit.Test
    public void testMemLeak() throws InterruptedException
    {
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

