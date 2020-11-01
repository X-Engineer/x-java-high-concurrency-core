package com.crazymakercircle.designmodel.masterworker;

import com.crazymakercircle.util.ThreadUtil;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Worker<T extends Task, R>
{
    // 任务的队列
    private LinkedBlockingQueue<T> taskQueue = new LinkedBlockingQueue<>();

    static AtomicInteger index = new AtomicInteger(1);
    private int workerId;
    //任务调度线程
    private Thread thread = null;

    public Worker()
    {
        this.workerId = index.getAndIncrement();
        thread = new Thread(() -> this.run());
        thread.start();
    }

    private T task;

    /**
     * 轮询执行任务
     */
    public void run()
    {
        try
        {
            execute();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    // 轮询启动所有的子任务
    public void execute() throws InterruptedException
    {
        for (; ; )
        {

            T task = this.taskQueue.take();
            //获取io密集型任务线程池
            ThreadPoolExecutor poolExecutor = ThreadUtil.getIoIntenseTargetThreadPool();
            poolExecutor.submit(() ->
                    {
                        task.setWorkerId(workerId);
                        task.execute();

                    }
            );


        }

    }

    public void submit(T task, Consumer<R> action)
    {
        task.resultAction = action;
        try
        {
            this.taskQueue.put(task);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

    }

    public boolean finished()
    {
        return false;
    }

    public int getResult()
    {
        return 0;
    }
}
