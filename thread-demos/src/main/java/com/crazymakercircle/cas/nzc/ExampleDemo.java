package com.crazymakercircle.cas.nzc;

import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @project: x-java-high-concurrency-core
 * @ClassName: Example
 * @author: nzcer
 * @creat: 2023/3/31 12:45
 * @description:
 *
 */
public class ExampleDemo {
    public static void main(String[] args) throws InterruptedException {
        PlainExample plainExample = new PlainExample();
        SynchronizedExample sycExample = new SynchronizedExample();
        CASExample casExample = new CASExample();
        // 1.自己设置参数的 ThreadPoolExecutor
        //ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new DefaultThreadFactory("my-pool-thread"));

        // 1.自定义的 ThreadPool 类
        int taskNum = 1000000;
        ThreadPoolExecutor threadPoolExecutor = new CounterPoolExecutor(5, 10, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), taskNum);

        for (int i = 0; i < taskNum; i++) {
            // 改用线程池提交任务
            //threadPoolExecutor.execute(plainExample);
            threadPoolExecutor.execute(sycExample);
            //threadPoolExecutor.execute(casExample);
        }
        System.out.println("Main 线程结束");
    }
}

/**
 * 无任何同步操作，多线程环境中，对于同一个共享变量进行操作会存在原子性问题
 */
class PlainExample implements Runnable {
    private int state = 0;

    public void changeState() {
        state++;
        if (state == 80) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(Thread.currentThread().getName() + ": state = " + state);
    }

    @Override
    public void run() {
        changeState();
    }
}

/**
 * 使用 Synchronized 同步锁
 */
class SynchronizedExample implements Runnable {
    // 方法一: 简单使用 Synchronized 同步锁来解决
    // 它的语义是保证同一段代码同一时间只能有一个线程在执行。
    // 1000000 个 task 耗时
    // 4447
    // 4474
    // 4518
    // 4578
    // 4538
    private volatile int state = 0;

    public synchronized void changeState() {
        state++;
        if (state == 80) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(Thread.currentThread().getName() + ": state = " + state);
    }

    @Override
    public void run() {
        changeState();
    }
}

/**
 * 使用 CAS 保证原子性
 */
class CASExample implements Runnable {
    // 方法二: CAS 机制，原子类具体实现
    // 1000000 个 task 耗时
    // 4821
    // 4844
    // 4862
    // 4785
    // 4743
    private AtomicInteger state = new AtomicInteger(0);

    public void changeState() {
        System.out.println(Thread.currentThread().getName() + ": state = " + state.incrementAndGet());
        if (state.get() == 80) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void run() {
        changeState();
    }
}

/**
 * 自定义线程池，用于统计所有提交到线程池中的任务完成，需要消耗多少时间
 */
class CounterPoolExecutor extends ThreadPoolExecutor {
    private AtomicInteger count = new AtomicInteger(0);//统计执行次数
    private long startTime = System.currentTimeMillis();
    private String funcname = "";
    private int COUNT;

    public CounterPoolExecutor(int corePoolSize, int maximumPoolSize,
                               long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue, int COUNT) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new DefaultThreadFactory("my-pool-thread"));
        this.COUNT = COUNT;
    }


    @Override
    protected void afterExecute(Runnable r, Throwable t) {//线程执行结束时
        int l = count.addAndGet(1);
        if (l == COUNT) {
            System.out.println(r.getClass().getSimpleName() + " spend time:" + (System.currentTimeMillis() - startTime));
        }
    }
}