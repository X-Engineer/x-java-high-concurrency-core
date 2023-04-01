package com.crazymakercircle.mutithread.basic.create.nzc;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @project: x-java-high-concurrency-core
 * @ClassName: CreateThreadByThreadPool
 * @author: nzcer
 * @creat: 2023/3/18 14:36
 * @description:
 */
public class CreateThreadByThreadPool {

    private static ExecutorService executor = new ThreadPoolExecutor(10, 100, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    public static void main(String[] args) {

        for (int i = 0; i < 6000; i++) {
            final int param = i;
            executor.execute(() -> {
                System.out.println(Thread.currentThread().getName() + ", 当前处理：" + param);
                myMethod(param);
                try {
                    // 模拟耗时的任务
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        System.out.println("任务结束");
    }

    private static void myMethod(int i) {
        if (i == 2) {
            return;
        }
        System.out.println(i);
    }
}
