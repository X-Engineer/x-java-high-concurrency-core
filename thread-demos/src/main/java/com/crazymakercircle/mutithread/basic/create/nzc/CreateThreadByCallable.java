package com.crazymakercircle.mutithread.basic.create.nzc;

import com.crazymakercircle.util.Print;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @project: x-java-high-concurrency-core
 * @ClassName: CreateThreadByCallable
 * @author: nzcer
 * @creat: 2023/3/18 14:23
 * @description:
 */
public class CreateThreadByCallable {
    static class ReturnableTask implements Callable<Long> {

        @Override
        public Long call() throws Exception {
            long st = System.currentTimeMillis();
            Thread.sleep(1000);
            long ed = System.currentTimeMillis();
            return ed - st;
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ReturnableTask call = new ReturnableTask();
        FutureTask<Long> futureTask = new FutureTask<>(call);
        Thread thread = new Thread(futureTask, "FutureTask-1");
        thread.start();
        Print.cfo(Thread.currentThread().getName() + "做一点自己的事情");
        Thread.sleep(5000);
        Print.cfo(Thread.currentThread().getName() + "获取并发执行的任务结果");
        Print.cfo(thread.getName() + "线程占用时间:" + futureTask.get());
    }
}
