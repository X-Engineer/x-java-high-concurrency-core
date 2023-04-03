package com.crazymakercircle.mutithread.basic.create3;

import com.crazymakercircle.util.Print;
import com.crazymakercircle.util.RandomUtil;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.crazymakercircle.util.ThreadUtil.sleepMilliSeconds;
import static com.crazymakercircle.util.ThreadUtil.sleepSeconds;

/**
 * Created by 尼恩@疯狂创客圈.
 * 1.newSingleThreadExecutor创建“单线程化线程池”
 * 该方法用于创建一个“单线程化线程池”，也就是只有一个线程的线程池，
 * 所创建的线程池用唯一的工作线程来执行任务，使用此方法创建的线程池能保证所有任务按照指定顺序（如FIFO）执行。
 * <p>
 * 2.newFixedThreadPool创建“固定数量的线程池”
 * 该方法用于创建一个“固定数量的线程池”，其唯一的参数用于设置池中线程的“固定数量”。
 * <p>
 * 3.newCachedThreadPool 创建“可缓存线程池”
 * 该方法用于创建一个“可缓存线程池”，如果线程池内的某些线程无事可干成为空闲线程，“可缓存线程池”可灵活回收这些空闲线程。
 * <p>
 * 4.newScheduledThreadPool创建“可调度线程池”
 * 该方法用于创建一个“可调度线程池”，即一个提供“延时”和“周期性”任务调度功能的ScheduledExecutorService类型的线程池。
 * <p>
 * 为何JUC要提供工厂方法呢？
 * 原因是使用ThreadPoolExecutor、ScheduledThreadPoolExecutor构造器创建普通线程池、可调度线程池比较复杂，这些构造器会涉及大量的复杂参数。
 * 尽管Executors的工厂方法使用方便，但是在生产场景中被很多企业（尤其是大厂）的开发规范所禁用。
 */

public class CreateThreadPoolDemo {

    public static final int SLEEP_GAP = 500;
    public static final int MAX_TURN = 5;

    //异步的执行目标类
    public static class TargetTask implements Runnable {
        static AtomicInteger taskNo = new AtomicInteger(1);
        protected String taskName;

        public TargetTask() {
            taskName = "task-" + taskNo.get();
            taskNo.incrementAndGet();
        }

        public void run() {

            Print.tco("任务：" + taskName + " doing");
            // 线程睡眠一会
            sleepMilliSeconds(SLEEP_GAP);

            Print.tco(taskName + " 运行结束.");
        }

        @Override
        public String toString() {
            return "TargetTask{" + taskName + '}';
        }
    }

    //异步的执行目标类：执行过程中将发生异常
    static class TargetTaskWithError extends TargetTask {
        public void run() {
            super.run();
            throw new RuntimeException("Error from " + taskName);
        }
    }

    //测试用例：只有一条线程的线程池
    @Test
    public void testSingleThreadExecutor() {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        for (int i = 0; i < 5; i++) {
            pool.execute(new TargetTask());
            pool.submit(new TargetTask());
        }
        sleepSeconds(1000);
        //关闭线程池
        pool.shutdown();
    }


    //测试用例：只有3条线程固定大小的线程池
    @Test
    public void testNewFixedThreadPool() {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 5; i++) {
            pool.execute(new TargetTask());
            pool.submit(new TargetTask());
        }
        sleepSeconds(1000);
        //关闭线程池
        pool.shutdown();
    }

    //测试用例：“可缓存线程池”
    @Test
    public void testNewCacheThreadPool() {
        ExecutorService pool = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i++) {
            pool.execute(new TargetTask());
            pool.submit(new TargetTask());
        }
        sleepSeconds(1000);
        //关闭线程池
        pool.shutdown();
    }

    //测试用例：“可调度线程池”
    @Test
    public void testNewScheduledThreadPool() {
        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);
        for (int i = 0; i < 2; i++) {
            scheduled.scheduleAtFixedRate(new TargetTask(),
                    0, 500, TimeUnit.MILLISECONDS);
            //以上的参数中：
            // 0表示首次执行任务的延迟时间，500表示每次执行任务的间隔时间
            //TimeUnit.MILLISECONDS所设置的时间的计时单位为毫秒
        }
        sleepSeconds(1000);
        //关闭线程池
        scheduled.shutdown();
    }

    //测试用例：“可调度线程池2”
    @Test
    public void testNewScheduledThreadPool2() {
        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(0);
        for (int i = 0; i < 2; i++) {
            scheduled.scheduleAtFixedRate(new TargetTask(),
                    0, 500, TimeUnit.MILLISECONDS);
            //以上的参数中：
            // 0表示首次执行任务的延迟时间，500表示每次执行任务的间隔时间
            //TimeUnit.MILLISECONDS所设置的时间的计时单位为毫秒
        }
        sleepSeconds(1000);
        //关闭线程池
        scheduled.shutdown();
    }


    /**
     * 在创建线程池时，如果线程池的参数（如核心线程数量、最大线程数量、BlockingQueue等）配置得不合理，就会出现任务不能被正常调度的问题。
     * 下面是一个错误的线程池配置示例：
     * 下面创建了最大线程数量maximumPoolSize为100的线程池，仅仅向其中提交了5个任务。理论上，这5个任务都会被执行到，奇怪的是示例中只有1个任务在执行，
     * 其他的4个任务都在等待。其他任务被加入到了阻塞队列中，需要等pool-1-thread-1线程执行完第一个任务后，才能依次从阻塞队列取出执行。
     * 但是，实例中的第一个任务是一个永远也没有办法完成的任务，所以其他的4个任务只能永远在阻塞队列中等待着。由于参数配置得不合理，因此出现了以上的奇怪现象。
     */
    @org.junit.Test
    public void testThreadPoolExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, //corePoolSize
                100, //maximumPoolSize
                100, //keepAliveTime
                TimeUnit.SECONDS, //unit
                new LinkedBlockingDeque<>(100));//workQueue
        // 提交5个任务
        for (int i = 0; i < 5; i++) {
            final int taskIndex = i;
            executor.execute(() ->
            {
                Print.tco("taskIndex = " + taskIndex);
                try {
                    // 极端测试：让线程睡眠一个很长的时间
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        while (true) {
            // 每隔一秒输出线程池的工作任务数量、总计的任务数量
            Print.tco("- activeCount:" + executor.getActiveCount() +
                    " - taskCount:" + executor.getTaskCount());
            sleepSeconds(1);
        }
    }

    //一个简单的线程工厂

    /**
     * 自定义简单的线程工厂
     */
    static public class SimpleThreadFactory implements ThreadFactory {
        static AtomicInteger threadNo = new AtomicInteger(1);

        //实现其唯一的创建线程方法
        @Override
        public Thread newThread(Runnable target) {
            String threadName = "simpleThread-" + threadNo.get();
            Print.tco("创建一条线程，名称为：" + threadName);
            threadNo.incrementAndGet();
            //设置线程名称
            Thread thread = new Thread(target, threadName);
            //设置为守护线程
            thread.setDaemon(true);
            return thread;
        }
    }

    /**
     * 测试自定义的线程工厂
     */
    @org.junit.Test
    public void testThreadFactory() {
        //使用自定义线程工厂，快捷创建线程池
        ExecutorService pool =
                Executors.newFixedThreadPool(2, new SimpleThreadFactory());
        for (int i = 0; i < 5; i++) {
            pool.submit(new TargetTask());
        }
        //等待10秒
        sleepSeconds(10);
        Print.tco("关闭线程池");
        pool.shutdown();
    }

    //自定义拒绝策略
    public static class CustomIgnorePolicy implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            // 可做日志记录等
            Print.tco(r + " rejected; " + " - getTaskCount: " + e.getTaskCount());
        }
    }

    /**
     * 自定义一个拒绝策略，实现RejectedExecutionHandler接口的rejectedExecution方法即可。
     */
    @org.junit.Test
    public void testCustomIgnorePolicy() {
        int corePoolSize = 2; //核心线程数
        int maximumPoolSize = 4;  //最大线程数
        long keepAliveTime = 10;
        TimeUnit unit = TimeUnit.SECONDS;
        //最大排队任务数
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(2);
        //线程工厂
        ThreadFactory threadFactory = new SimpleThreadFactory();
        //拒绝和异常策略
        RejectedExecutionHandler policy = new CustomIgnorePolicy();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime, unit,
                workQueue,
                threadFactory,
                policy);

        // 预启动所有核心线程
        pool.prestartAllCoreThreads();
        // 此时因为已经预启动了所有核心线程，所以线程池中的线程数为2
        Print.tco("线程池中的线程数：" + pool.getPoolSize());
        for (int i = 1; i <= 10; i++) {
            pool.execute(new TargetTask());
        }
        //等待10秒
        sleepSeconds(10);
        Print.tco("关闭线程池");
        pool.shutdown();
    }

    //线程本地变量,用于记录线程异步任务的开始执行时间
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    /**
     * 示例代码在beforeExecute（前钩子）方法中通过startTime线程局部变量暂存了异步目标任务（如Runnable实例）的开始执行时间（起始时间），
     * 在afterExecute（后钩子）方法中通过startTime线程局部变量获取了之前暂存的起始时间，
     * 然后计算与系统当前时间（结束时间）之间的时间差，从而得出异步目标任务的执行时长。
     */
    @org.junit.Test
    public void testHooks() {
        ExecutorService pool = new ThreadPoolExecutor(2,
                4, 60,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(2)) {
            @Override
            protected void terminated() {
                Print.tco("调度器已经终止!");
            }

            @Override
            protected void beforeExecute(Thread t, Runnable target) {
                Print.tco(target + "前钩子被执行");
                //记录开始执行时间
                START_TIME.set(System.currentTimeMillis());
                super.beforeExecute(t, target);
            }


            @Override
            protected void afterExecute(Runnable target, Throwable t) {
                super.afterExecute(target, t);
                //计算执行时长
                long time = (System.currentTimeMillis() - START_TIME.get());
                Print.tco(target + " 后钩子被执行, 任务执行时长（ms）：" + time);
                //清空本地变量
                START_TIME.remove();
            }
        };


        pool.execute(new TargetTask());
        pool.execute(new TargetTask());
        pool.execute(new TargetTask());

        //等待10秒
        sleepSeconds(10);
        Print.tco("关闭线程池");
        pool.shutdown();

    }


    @org.junit.Test
    public void testNewFixedThreadPool2() {
        //创建一个固定大小线程池
        ExecutorService fixedExecutorService = Executors.newFixedThreadPool(1);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) fixedExecutorService;
        Print.tco(threadPoolExecutor.getMaximumPoolSize());
        //设置核心线程数
        threadPoolExecutor.setCorePoolSize(8);

        //创建一个单线程化的线程池
        ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();
        //转换成普通线程池， 会抛出运行时异常 java.lang.ClassCastException
        ((ThreadPoolExecutor) singleExecutorService).setCorePoolSize(8);
    }


    //测试用例：提交和执行

    /**
     * 2.通过submit()返回的Future对象捕获异常
     * submit()方法自身并不会传递异常，处理过程中的异常都被包装到Future实例中，调用者在调用Future.get()方法获取执行结果时，
     * 可以捕获异步执行过程中抛出的受检异常和运行时异常，并进行对应的业务处理。演示代码如下：
     */
    @Test
    public void testSubmit() {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
        pool.execute(new TargetTaskWithError());
        /**
         * submit(Runnable x) 返回一个future。可以用这个future来判断任务是否成功完成。请看下面：
         */
        Future future = pool.submit(new TargetTaskWithError());
        try {
            //如果异常抛出，会在调用Future.get()时传递给调用者
            if (future.get() == null) {
                //如果Future的返回为null，任务完成
                Print.tco("任务完成");
            }
        } catch (Exception e) {
            Print.tco(e.getCause().getMessage());
        }


        sleepSeconds(10);
        //关闭线程池
        pool.shutdown();
    }

    //测试用例：获取异步调用的结果

    /**
     * submit()方法自身并不会传递结果，而是返回一个Future异步执行实例，处理过程的结果被包装到Future实例中，
     * 调用者可以通过Future.get()方法获取异步执行的结果。
     */
    @Test
    public void testSubmit2() {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
        Future<Integer> future = pool.schedule(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                //返回200 - 300 之间的随机数
                return RandomUtil.randInRange(200, 300);
            }
        }, 100, TimeUnit.MILLISECONDS);

        try {
            Integer result = future.get();
            Print.tco("异步执行的结果是:" + result);
        } catch (InterruptedException e) {
            Print.tco("异步调用被中断");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Print.tco("异步调用过程中，发生了异常");
            e.printStackTrace();
        }
        sleepSeconds(10);
        //关闭线程池
        pool.shutdown();

    }


    //测试用例：优雅关闭

    /**
     * （1）执行shutdown()方法，拒绝新任务的提交，并等待所有任务有序地执行完毕。
     * （2）执行awaitTermination(long timeout,TimeUnit unit)方法，指定超时时间，判断是否已经关闭所有任务，线程池关闭完成。
     * （3）如果awaitTermination()方法返回false，或者被中断，就调用shutDownNow()方法立即关闭线程池所有任务。
     * （4）补充执行awaitTermination(long timeout,TimeUnit unit)方法，判断线程池是否关闭完成。如果超时，就可以进入循环关闭，循环一定的次数（如1000次），不断关闭线程池，直到其关闭或者循环结束。
     */
    @Test
    public void testShutdownGracefully() {
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(2);
        threadPool.shutdown(); // Disable new tasks from being submitted
        try {
            // 设定最大重试次数
            // 等待 60 s
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                // 调用 shutdownNow 取消正在执行的任务
                threadPool.shutdownNow();
                // 再次等待 60 s，如果还未结束，可以再次尝试，或则直接放弃
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("线程池任务未正常执行结束");
                }
            }
        } catch (InterruptedException ie) {
            // 重新调用 shutdownNow
            threadPool.shutdownNow();
        }
    }


}

