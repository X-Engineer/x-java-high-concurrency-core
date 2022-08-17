package com.crazymakercircle.pool;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PoolSheduler {

    ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 20, 1,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(30));

    @Test
    public void test() {
        int n = 45;
        System.out.print(n + "个任务执行耗时: ");

        long start = System.currentTimeMillis();

        CountDownLatch latch = new CountDownLatch(n);


        // n个任务 并发执行
        for (int i = 0; i < n; i++) {

            //每一个任务 ，封装为一个 runnable， 提交到 pool
            // 异步执行完成之后， 闭锁 做一次 countDown 操作
            executor.execute(() -> {
                try {
                    Thread.sleep(500);
                    latch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        try {
            latch.await();
            // 发起请求的 线程，等待 异步结果
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdown();


        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}
