package com.crazymakercircle.demo.lock;

import com.crazymakercircle.util.Print;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class SemaphoreTest
{

    @org.junit.Test
    public void testShareLock() throws InterruptedException
    {
        // 排队总人数（请求总数）
        final int CLIENT_TOTAL = 10;
        // 可同时受理业务的窗口数量（同时并发执行的线程数）
        final int THREAD_TOTAL = 2;
        //线程池，用于多线程模拟测试
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_TOTAL);
        //创建并发信号量
        final Semaphore semaphore = new Semaphore(THREAD_TOTAL);
        final CountDownLatch countDownLatch = new CountDownLatch(CLIENT_TOTAL);
        for (int i = 0; i < CLIENT_TOTAL; i++)
        {
            final int count = i;
            threadPool.execute(() ->
            {
                try
                {
                    //抢占一个信号
                    semaphore.acquire(1);
                    //模拟业务操作： 处理排队业务
                    process(count);
                    //释放一个信号
                    semaphore.release(1);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        threadPool.shutdown();
    }

    /**
     * 模拟业务操作： 处理排队业务
     */
    private static void process(int i) throws InterruptedException
    {
        Print.tcfo("受理处理中。。。,服务号: " + i);
        Thread.sleep(1000);
    }


}
