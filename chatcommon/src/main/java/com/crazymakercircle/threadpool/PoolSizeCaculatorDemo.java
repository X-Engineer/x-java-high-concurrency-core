package com.crazymakercircle.threadpool;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.crazymakercircle.util.ThreadUtil.sleepMilliSeconds;
import static com.crazymakercircle.util.ThreadUtil.sleepSeconds;

public class PoolSizeCaculatorDemo extends PoolSizeCalculator
{
    /**
     * 模拟的混合型任务
     */
    static class MockMixedTask implements Runnable
    {
        @Override
        public void run()
        {
            //模拟的混合型任务的平均执行
            sleepMilliSeconds(800);
        }

    }




    /**
     * 创建线程池中的异步任务
     */
    @Override
    protected Runnable createTask()
    {
        return new MockMixedTask();
    }

    /**
     * 创建线程池中的阻塞队列实例
     */
    @Override
    protected BlockingQueue createWorkQueue()
    {
        return new LinkedBlockingQueue(1000);
    }

    @Override
    protected long getCurrentThreadCPUTime()
    {
        return System.currentTimeMillis();
//        return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
    }

    public static void main(String[] args)
    {
        PoolSizeCalculator poolSizeCalculator = new PoolSizeCaculatorDemo();
        //期望 CPU 利用率为 1.0（即 100%）
        BigDecimal targetUtilization = new BigDecimal(0.8);
        //任务队列总大小不超过 100,000 字节
        BigDecimal targetQueueSizeBytes = new BigDecimal(10000);
        poolSizeCalculator.calculateBoundaries(targetUtilization, targetQueueSizeBytes);
    }
}


