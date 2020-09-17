package com.crazymakercircle.demo.lock;

import com.crazymakercircle.demo.lock.busi.IncrementData;
import com.crazymakercircle.demo.lock.busi.TwoLockDemo;
import com.crazymakercircle.demo.lock.custom.CLHLock;
import com.crazymakercircle.util.Print;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockTest
{


    @org.junit.Test
    public void testLimit()
    {
        // 每条线程的执行轮数
        final int TURNS = 1000;
        // 线程数
        final int THREADS = 10;

        //线程池，用于多线程模拟测试
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);

        Lock lock = new ReentrantLock();

        // 线程同步器
        CountDownLatch countDownLatch = new CountDownLatch(THREADS);
        long start = System.currentTimeMillis();
        for (int i = 0; i < THREADS; i++)
        {
            pool.submit(() ->
            {
                try
                {
                    for (int j = 0; j < TURNS; j++)
                    {
                        IncrementData.lockAndIncrease(lock);
                    }
                    Print.tcfo("本线程累加完成");
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                //等待所有线程结束
                countDownLatch.countDown();

            });
        }
        try
        {
            countDownLatch.await();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        float time = (System.currentTimeMillis() - start) / 1000F;
        //输出统计结果
        Print.tcfo("运行的时长为：" + time);
        Print.tcfo("累加结果为：" + IncrementData.sum);
    }


    /**
     * 公平锁测试用例
     */
    @org.junit.Test
    public void testFairLock() throws InterruptedException
    {
        //创建可重入锁，创建为公平锁的类型
        Lock lock = new ReentrantLock(true);

        //创建Runnable可执行实例
        Runnable r = () -> IncrementData.lockAndIncrease(lock);
        Thread t1 = new Thread(r, "thread-1");  //创建第1条线程
        Thread t2 = new Thread(r, "thread-2");  //创建第2条线程
        Thread t3 = new Thread(r, "thread-3");  //创建第3条线程
        Thread t4 = new Thread(r, "thread-4");  //创建第4条线程

        t1.start(); //启动第1条线程
        t2.start(); //启动第2条线程
        t3.start(); //启动第3条线程
        t4.start(); //启动第4条线程

        Print.tcfo("主线程结束");
        Thread.sleep(Integer.MAX_VALUE);
    }


    /**
     * 非公平锁测试用例
     */
    @org.junit.Test
    public void testNoFairLock() throws InterruptedException
    {
        //创建可重入锁，默认的非公平锁
        Lock lock = new ReentrantLock();

        //创建Runnable可执行实例
        Runnable r = () -> IncrementData.lockAndIncrease(lock);
        Thread t1 = new Thread(r, "thread-1");  //创建第1条线程
        Thread t2 = new Thread(r, "thread-2");  //创建第2条线程
        Thread t3 = new Thread(r, "thread-3");  //创建第3条线程
        Thread t4 = new Thread(r, "thread-4");  //创建第4条线程

        t1.start(); //启动第1条线程
        t2.start(); //启动第2条线程
        t3.start(); //启动第3条线程
        t4.start(); //启动第4条线程

        Print.tcfo("主线程结束");
        Thread.sleep(Integer.MAX_VALUE);
    }

    //测试用例：抢锁过程可中断
    @org.junit.Test
    public void testInterruptLock() throws InterruptedException
    {
        //创建可重入锁，默认的非公平锁
        Lock lock = new ReentrantLock();

        //创建Runnable可执行实例
        Runnable r = () -> IncrementData.lockInterruptiblyAndIncrease(lock);
        Thread t1 = new Thread(r, "thread-1");  //创建第1条线程
        Thread t2 = new Thread(r, "thread-2");  //创建第2条线程
        Thread t3 = new Thread(r, "thread-3");  //创建第3条线程
        Thread t4 = new Thread(r, "thread-4");  //创建第4条线程

        t1.start(); //启动第1条线程
        t2.start(); //启动第2条线程
        t3.start(); //启动第3条线程
        t4.start(); //启动第4条线程

        t3.interrupt(); //中断第3条线程
        t4.interrupt(); //中断第4条线程

        Print.tcfo("主线程结束");
        Thread.sleep(Integer.MAX_VALUE);
    }

    //获取ThreadMXBean
    public static ThreadMXBean mbean = ManagementFactory.getThreadMXBean();

    //测试用例：抢占两把锁，造成死锁，然后进行死锁监测和部分中断
    @org.junit.Test
    public void testTowLock() throws InterruptedException
    {
        //创建可重入锁，默认的非公平锁
        Lock lock1 = new ReentrantLock();
        Lock lock2 = new ReentrantLock();

        //Runnable可执行实例1: 先抢占lock1， 再抢占 lock2
        Runnable r1 = () -> TwoLockDemo.useTowlockInterruptiblyLock(lock1, lock2);

        //Runnable可执行实例2: 先抢占lock2， 再抢占 lock1
        Runnable r2 = () -> TwoLockDemo.useTowlockInterruptiblyLock(lock2, lock1);
        Thread t1 = new Thread(r1, "thread-1");  //创建第1条线程
        Thread t2 = new Thread(r2, "thread-2");  //创建第2条线程
        t1.start(); //启动第1条线程
        t2.start(); //启动第2条线程

        Print.tcfo("主线程结束");

        Print.tcfo("死锁监测和处理");
        //等待一段时间再执行死锁检测
        Thread.sleep(2000);
        //获取到所有死锁线程的id
        long[] deadlockedThreads = mbean.findDeadlockedThreads();
        if (deadlockedThreads.length > 0)
        {
            Print.tcfo("发生了死锁");
            //遍历数组获取所有的死锁线程详细堆栈信息并打印
            for (long pid : deadlockedThreads)
            {
                //此方法获取不带有堆栈跟踪信息的线程数据
                //hreadInfo threadInfo = mbean.getThreadInfo(pid);
                //第二个参数指定转储多少项堆栈跟踪信息,设置为Integer.MAX_VALUE可以转储所有的堆栈跟踪信息
                ThreadInfo threadInfo = mbean.getThreadInfo(pid, Integer.MAX_VALUE);
                Print.tcfo(threadInfo);
            }
            Print.tcfo("中断一条线程，这里是线程：" + t1.getName());
            t1.interrupt();
        }
        Thread.sleep(Integer.MAX_VALUE);
    }

    //测试用例：抢占两把锁，通过限时等待的方式
    @org.junit.Test
    public void testTryTowLock() throws InterruptedException
    {
        //创建可重入锁，默认的非公平锁
        Lock lock1 = new ReentrantLock();
        Lock lock2 = new ReentrantLock();

        //Runnable可执行实例1: 先限时抢占lock1， 再限时抢占 lock2
        Runnable r1 = () -> TwoLockDemo.tryTowLock(lock1, lock2);

        //Runnable可执行实例2: 先限时抢占lock2， 再限时抢占 lock1
        Runnable r2 = () -> TwoLockDemo.tryTowLock(lock2, lock1);
        Thread t1 = new Thread(r1, "thread-1");  //创建第1条线程
        Thread t2 = new Thread(r2, "thread-2");  //创建第2条线程
        t1.start(); //启动第1条线程
        t2.start(); //启动第2条线程

        Print.tcfo("主线程结束");

        Thread.sleep(Integer.MAX_VALUE);
    }

    @org.junit.Test
    public void testCLHLockCapability()
    {
        // 速度对比
        // ReentrantLock  1 000 000 次 0.122 秒
        // CLHLock        1 000 000 次 2.352 秒

        // 每条线程的执行轮数
//        final int TURNS = 100000;
        final int TURNS = 5;

        // 线程数
        final int THREADS = 2;

        //线程池，用于多线程模拟测试
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);

        Lock lock = new CLHLock();

        // 线程同步器
        CountDownLatch countDownLatch = new CountDownLatch(THREADS);
        long start = System.currentTimeMillis();
        for (int i = 0; i < THREADS; i++)
        {
            pool.submit(() ->
            {
                try
                {
                    for (int j = 0; j < TURNS; j++)
                    {
                        // IncrementData.lockAndFastIncrease(lock);
                        IncrementData.lockAndIncrease(lock);
                    }
                    Print.tcfo("本线程累加完成");
                } catch (Exception e)
                {
                    Print.tcfo("本线程累加失败，出现异常");
                    e.printStackTrace();
                }
                //等待所有线程结束
                countDownLatch.countDown();

            });
        }
        try
        {
            countDownLatch.await();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        float time = (System.currentTimeMillis() - start) / 1000F;
        //输出统计结果
        Print.tcfo("运行的时长为：" + time);
        Print.tcfo("累加结果为：" + IncrementData.sum);
    }

}