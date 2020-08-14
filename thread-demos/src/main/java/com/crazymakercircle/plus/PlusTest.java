package com.crazymakercircle.plus;

import com.crazymakercircle.plus.busi.NotSafePlus;
import com.crazymakercircle.plus.busi.SafePlus;
import com.crazymakercircle.util.Print;
import org.openjdk.jol.vm.VM;

import java.util.concurrent.CountDownLatch;

import static com.crazymakercircle.util.ThreadUtil.sleepMilliSeconds;

/**
 * Created by 尼恩@疯狂创客圈.
 */
public class PlusTest
{
    final int MAX_TREAD = 10;
    final int MAX_TURN = 1000;
    CountDownLatch latch = new CountDownLatch(MAX_TREAD);

    /**
     * 测试用例：测试不安全的累加器
     */
    @org.junit.Test
    public void testNotSafePlus() throws InterruptedException
    {
        NotSafePlus counter = new NotSafePlus();
        Runnable runnable = () ->
        {
            for (int i = 0; i < MAX_TURN; i++)
            {
                counter.selfPlus();
            }
            latch.countDown();
        };
        for (int i = 0; i < MAX_TREAD; i++)
        {
            new Thread(runnable).start();
        }
        latch.await();
        Print.tcfo("理论结果：" + MAX_TURN * MAX_TREAD);
        Print.tcfo("实际结果：" + counter.getAmount());
        Print.tcfo("差距是：" + (MAX_TURN * MAX_TREAD - counter.getAmount()));
    }

    /**
     * 测试用例：安全的累加器
     */
    @org.junit.Test
    public void testSafePlus() throws InterruptedException
    {
        SafePlus counter = new SafePlus();
        Runnable runnable = () ->
        {
            for (int i = 0; i < MAX_TURN; i++)
            {
                counter.increase();
            }
            latch.countDown();
        };
        for (int i = 0; i < MAX_TREAD; i++)
        {
            new Thread(runnable).start();
        }
        latch.await();
        Print.tcfo("理论结果：" + MAX_TURN * MAX_TREAD);
        Print.tcfo("实际结果：" + counter.getAmount());
        Print.tcfo("差距是：" + (MAX_TURN * MAX_TREAD - counter.getAmount()));
    }


    @org.junit.Test
    public void showNoLockObject() throws InterruptedException
    {
        Print.tcfo(VM.current().details());
        SafePlus counter = new SafePlus();

        Print.tcfo("object status: ");
        counter.printSelf();
    }


    @org.junit.Test
    public void showBiasedLock() throws InterruptedException
    {
        Print.tcfo(VM.current().details());
        //JVM延迟偏向锁
        sleepMilliSeconds(5000);

        SafePlus counter = new SafePlus();

        Print.tcfo("抢占锁前, counter 的状态: ");
        counter.printSelfWithThreadID();

        sleepMilliSeconds(5000);
        CountDownLatch latch = new CountDownLatch(1);
        Runnable runnable = () ->
        {
            for (int i = 0; i < MAX_TURN; i++)
            {
                synchronized (counter)
                {
                    counter.increase();
                    if (i == MAX_TURN / 2)
                    {

                        Print.tcfo("占有锁, counter 的状态: ");
                        counter.printSelfWithThreadID();

                    }
                }
                //每一次循环等待10ms
                sleepMilliSeconds(10);
            }
            latch.countDown();
        };
        new Thread(runnable).start();
        //等待加锁线程执行完成
        latch.await();
        sleepMilliSeconds(5000);
        Print.tcfo("释放锁后, counter 的状态: ");
        counter.printSelfWithThreadID();
    }

    @org.junit.Test
    public void showLightweightLock() throws InterruptedException
    {

        Print.tcfo(VM.current().details());
        //JVM延迟偏向锁
        sleepMilliSeconds(5000);

        SafePlus counter = new SafePlus();

        Print.tcfo("抢占锁前, counter 的状态: ");
        counter.printSelfWithThreadID();

        sleepMilliSeconds(5000);
        CountDownLatch latch = new CountDownLatch(2);
        Runnable runnable = () ->
        {
            for (int i = 0; i < MAX_TURN; i++)
            {
                synchronized (counter)
                {
                    counter.increase();
                    if (i == 1)
                    {
                        Print.tcfo("第一个线程占有锁, counter 的状态: ");
                        counter.printSelfWithThreadID();
                    }
                }

            }
            //循环完毕
            latch.countDown();

            //线程虽然释放锁，但是一直存在
            for (int j = 0; ; j++)
            {
                //每一次循环等待1ms
                sleepMilliSeconds(1);
            }
        };
        new Thread(runnable).start();


        sleepMilliSeconds(1000); //等待1s

        Runnable lightweightRunnable = () ->
        {
            for (int i = 0; i < MAX_TURN; i++)
            {
                synchronized (counter)
                {
                    counter.increase();
                    if (i == MAX_TURN / 2)
                    {
                        Print.tcfo("第二个线程占有锁, counter 的状态: ");
                        counter.printSelfWithThreadID();
                    }
                    //每一次循环等待1ms
                    sleepMilliSeconds(1);
                }
            }
            //循环完毕
            latch.countDown();
        };
        new Thread(lightweightRunnable).start();
        //等待加锁线程执行完成
        latch.await();
        sleepMilliSeconds(2000);  //等待2s
        Print.tcfo("释放锁后, counter 的状态: ");
        counter.printSelfWithThreadID();
    }

    @org.junit.Test
    public void showHeavyweightLock() throws InterruptedException
    {

        Print.tcfo(VM.current().details());
        //JVM延迟偏向锁
        sleepMilliSeconds(5000);

        SafePlus counter = new SafePlus();

        Print.tcfo("抢占锁前, counter 的状态: ");
        counter.printSelfWithThreadID();

        sleepMilliSeconds(5000);
        CountDownLatch latch = new CountDownLatch(3);
        Runnable runnable = () ->
        {
            for (int i = 0; i < MAX_TURN; i++)
            {
                synchronized (counter)
                {
                    counter.increase();
                    if (i == 0)
                    {
                        Print.tcfo("第一个线程占有锁, counter 的状态: ");
                        counter.printSelfWithThreadID();
                    }
                }

            }
            //循环完毕
            latch.countDown();

            //线程虽然释放锁，但是一直存在
            for (int j = 0; ; j++)
            {
                //每一次循环等待1ms
                sleepMilliSeconds(1);
            }
        };
        new Thread(runnable).start();


        sleepMilliSeconds(1000); //等待2s

        Runnable lightweightRunnable = () ->
        {
            for (int i = 0; i < MAX_TURN; i++)
            {
                synchronized (counter)
                {
                    counter.increase();
                    if (i == 0)
                    {
                        Print.tcfo("占有锁, counter 的状态: ");
                        counter.printSelfWithThreadID();
                    }
                    //每一次循环等待10ms
                    sleepMilliSeconds(1);
                }
            }
            //循环完毕
            latch.countDown();
        };
        new Thread(lightweightRunnable).start();
        sleepMilliSeconds(100);  //等待2s
        new Thread(lightweightRunnable).start();

        //等待加锁线程执行完成
        latch.await();
        sleepMilliSeconds(2000);  //等待2s
        Print.tcfo("释放锁后, counter 的状态: ");
        counter.printSelfWithThreadID();
    }

}

