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
        int num = 45;
        System.out.print(num + "个任务执行耗时: ");

        long start = System.currentTimeMillis();

        CountDownLatch latch = new CountDownLatch(num);


        for (int i = 0; i < num; i++) {
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdown();


        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}
