package com.crazymakercircle.visiable;

import com.crazymakercircle.util.Print;
import com.crazymakercircle.util.ThreadUtil;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * volatile能保证数据的可见性，但volatile不能完全保证数据的原子性，对于volatile类型的变量进行复合操作（如++），其仍存在线程不安全的问题。
 * volatile变量的复合操作不具备原子性
 */
public class VolatileDemo {
    private volatile long value;

    @Test
    public void testAtomicLong() {
        // 并发任务数
        final int TASK_AMOUNT = 10;

        //线程池，获取CPU密集型任务线程池
        ExecutorService pool = ThreadUtil.getCpuIntenseTargetThreadPool();

        // 每条线程的执行轮数
        final int TURNS = 10000;
        // 线程同步倒数闩
        CountDownLatch countDownLatch = new CountDownLatch(TASK_AMOUNT);
        long start = System.currentTimeMillis();
        for (int i = 0; i < TASK_AMOUNT; i++) {
            pool.submit(() ->
            {
                try {
                    for (int j = 0; j < TURNS; j++) {
                        value++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //倒数闩，倒数一次
                countDownLatch.countDown();
            });
        }

        try {
            //等待倒数闩完成所有的倒数操作
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        float time = (System.currentTimeMillis() - start) / 1000F;
        //输出统计结果
        Print.tcfo("运行的时长为：" + time);
        Print.tcfo("累加结果为：" + value);
        Print.tcfo("与预期相差：" + (TURNS * TASK_AMOUNT - value));
    }

}

