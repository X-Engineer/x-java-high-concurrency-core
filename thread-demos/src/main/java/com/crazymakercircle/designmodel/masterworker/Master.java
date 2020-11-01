package com.crazymakercircle.designmodel.masterworker;

import com.crazymakercircle.util.Print;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Master<T extends Task, R>
{
    // 任务的集合
    private LinkedBlockingQueue<T> taskQueue = new LinkedBlockingQueue<>();

    // 所有worker
    private HashMap<String, Worker<T, R>> workers = new HashMap<>();

    //任务处理结果集
    protected Map<String, R> resultMap = new ConcurrentHashMap<>();
    //任务调度线程
    private Thread thread = null;
    public Master(int workerCount)
    {
        // 每个worker对象都需要持有queue的引用, 用于领任务与提交结果
        for (int i = 0; i < workerCount; i++)
        {
            Worker<T, R> worker = new Worker<>();
            workers.put("子节点: " + i, worker);
        }
        thread = new Thread(() -> this.execute());
        thread.start();
    }

    // 提交任务
    public void submit(T task)
    {
        taskQueue.add(task);
    }


    //结果处理的回调函数
    private void resultCallBack(Object o)
    {
        Task<R> task = (Task<R>) o;
        String taskName = "Worker:" + task.getWorkerId() + "-" + "Task:" + task.getId();
//        Print.tco(taskName + ":" + task.getResult());
        R result = task.getResult();
        resultMap.put(taskName, result);
    }

    // 启动所有的子任务
    public void execute()
    {

        for (; ; )
        {
            // 工作节点轮询,  轮流分配任务
            for (Map.Entry<String, Worker<T, R>> entry : workers.entrySet())
            {
                T task = null;
                try
                {
                    task = this.taskQueue.take();
                    Worker worker = entry.getValue();
                    worker.submit(task, this::resultCallBack);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }

    }


    // 获取最终的结果
    public void printResult()
    {
        Print.tco("-----------------------------------");
        for (Map.Entry<String, R> entry : resultMap.entrySet())
        {
            String taskName = entry.getKey();
            Print.fo(taskName + ":" + entry.getValue());
        }

    }

}
