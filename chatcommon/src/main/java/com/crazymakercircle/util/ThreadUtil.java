/**
 * Created by 尼恩@疯狂创客圈
 */
package com.crazymakercircle.util;


import com.crazymakercircle.threadpool.CpuIntenseTargetThreadPoolLazyHolder;
import com.crazymakercircle.threadpool.IoIntenseTargetThreadPoolLazyHolder;
import com.crazymakercircle.threadpool.MixedTargetThreadPoolLazyHolder;
import com.crazymakercircle.threadpool.SeqOrScheduledTargetThreadPoolLazyHolder;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class ThreadUtil {

    /**
     * CPU核数
     **/
    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * 空闲保活时限，单位秒
     */
    public static final int KEEP_ALIVE_SECONDS = 30;
    /**
     * 有界队列size
     */
    public static final int QUEUE_SIZE = 10000;

    /**
     * 混合线程池
     */
    public static final int MIXED_CORE = 0;  //混合线程池核心线程数
    public static final int MIXED_MAX = 128;  //最大线程数
    public static final String MIXED_THREAD_AMOUNT = "mixed.thread.amount";


    /**
     * 核心线程数
     */
    public static final int CORE_POOL_SIZE = 0;
    public static final int MAXIMUM_POOL_SIZE = CPU_COUNT;

    /**
     * 定制的线程工厂
     */

    /**
     * IO线程池最大线程数
     */
    public static final int IO_MAX = Math.max(2, CPU_COUNT * 2);
    /**
     * IO线程池核心线程数
     */
    public static final int IO_CORE = 0;

    public static class CustomThreadFactory implements ThreadFactory {
        //线程池数量
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;

        //线程数量
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String threadTag;

        public CustomThreadFactory(String threadTag) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            this.threadTag = "apppool-" + poolNumber.getAndIncrement() + "-" + threadTag + "-";
        }

        @Override
        public Thread newThread(Runnable target) {
            Thread t = new Thread(group, target,
                    threadTag + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }


    /**
     * 获取执行CPU密集型任务的线程池
     *
     * @return
     */
    public static ThreadPoolExecutor getCpuIntenseTargetThreadPool() {
        return CpuIntenseTargetThreadPoolLazyHolder.getInnerExecutor();
    }


    /**
     * 获取执行IO密集型任务的线程池
     *
     * @return
     */
    public static ThreadPoolExecutor getIoIntenseTargetThreadPool() {
        return IoIntenseTargetThreadPoolLazyHolder.getInnerExecutor();
    }


    /**
     * 获取执行混合型任务的线程池     *
     *
     * @return
     */
    public static ThreadPoolExecutor getMixedTargetThreadPool() {
        return MixedTargetThreadPoolLazyHolder.getInnerExecutor();
    }


    /**
     * 获取可调度线程池（包含提交延迟、定时、周期性、顺序性执行的任务）
     *
     * @return
     */
    public static ScheduledThreadPoolExecutor getSeqOrScheduledExecutorService() {
        return SeqOrScheduledTargetThreadPoolLazyHolder.getInnerExecutor();
    }

    /**
     * 顺序排队执行
     *
     * @param command
     */
    public static void seqExecute(Runnable command) {
        getSeqOrScheduledExecutorService().execute(command);
    }

    /**
     * 延迟执行
     *
     * @param command
     * @param i
     * @param unit
     */
    public static void delayRun(Runnable command, int i, TimeUnit unit) {
        getSeqOrScheduledExecutorService().schedule(command, i, unit);
    }

    /**
     * 固定频率执行
     *
     * @param command
     * @param i
     * @param unit
     */
    public static void scheduleAtFixedRate(Runnable command, int i, TimeUnit unit) {
        getSeqOrScheduledExecutorService().scheduleAtFixedRate(command, i, i, unit);
    }

    /**
     * 线程睡眠
     *
     * @param second 秒
     */
    public static void sleepSeconds(int second) {
        LockSupport.parkNanos(second * 1000L * 1000L * 1000L);
    }

    /**
     * 线程睡眠
     *
     * @param millisecond 毫秒
     */
    public static void sleepMilliSeconds(int millisecond) {
        LockSupport.parkNanos(millisecond * 1000L * 1000L);
    }

    /**
     * 获取当前线程名称
     */
    public static String getCurThreadName() {
        return Thread.currentThread().getName();
    }

    /**
     * 获取当前线程ID
     */
    public static long getCurThreadId() {
        return Thread.currentThread().getId();
    }

    /**
     * 获取当前线程
     */
    public static Thread getCurThread() {
        return Thread.currentThread();
    }

    /**
     * 调用栈中的类名
     *
     * @return
     */
    public static String stackClassName(int level) {
//        Thread.currentThread().getStackTrace()[1]是当前方法 curClassName 执行堆栈
//        Thread.currentThread().getStackTrace()[2]就是 curClassName 的 上一级的方法堆栈 以此类推

        String className = Thread.currentThread().getStackTrace()[level].getClassName();//调用的类名
        return className;

    }

    /**
     * 调用栈中的方法名称
     *
     * @return
     */

    public static String stackMethodName(int level) {
//        Thread.currentThread().getStackTrace()[1]是当前方法 curMethodName 执行堆栈
//        Thread.currentThread().getStackTrace()[2]就是 curMethodName 的 上一级的方法堆栈 以此类推

        String className = Thread.currentThread().getStackTrace()[level].getMethodName();//调用的类名
        return className;
    }

    public static void shutdownThreadPoolGracefully(ExecutorService threadPool) {
        if (!(threadPool instanceof ExecutorService) || threadPool.isTerminated()) {
            return;
        }
        try {
            threadPool.shutdown();   //拒绝接受新任务
        } catch (SecurityException e) {
            return;
        } catch (NullPointerException e) {
            return;
        }
        try {
            // 等待 60 s，等待线程池中的任务完成执行
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                // 调用 shutdownNow 取消正在执行的任务
                threadPool.shutdownNow();
                // 再次等待 60 s，如果还未结束，可以再次尝试，或则直接放弃
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("线程池任务未正常执行结束");
                }
            }
        } catch (InterruptedException ie) {
            // 捕获异常，重新调用 shutdownNow
            threadPool.shutdownNow();
        }
        //任然没有关闭，循环关闭1000次，每次等待10毫秒
        if (!threadPool.isTerminated()) {
            try {
                for (int i = 0; i < 1000; i++) {
                    if (threadPool.awaitTermination(10, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            } catch (Throwable e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public static void yieldThread() {
        // 老版本使用的是  Thread.yield();

//         Thread.yield();

        // yield只是对cpu调度器的一个提示,如果cpu调度器没有忽略这个提示,他会导致线程上下文的切换
        //关键是：cpu调度器, 有可能忽略这个提示


        // sleep() 方法和 yield() 的区别
        //sleep() 会导致当前线程暂停指定的时间，没有CPU时间片的消耗；
        //yield() 只是给CPU提醒该线程愿意暂时释放CPU资源，CPU资源不紧张的时候，会忽略掉这个提醒；若CPU没有忽略掉这个提醒，它会导致线程的应用上下文的切换；
        //sleep() 会导致线程的阻塞（block），会在给定的时间内释放CPU资源；
        //yield() 会导致RUNNING状态的线程进入 RUNNABLE (如果CPU没有忽略掉这个提醒);
        //sleep() 方法会百分之百的执行让线程暂停指定的时间，但 yiled() 方法的时候收到 CPU 资源的影响，并不会百分百的发生；
        //一个线程sleep()另一个线程调用 interrupte 会捕捉到中断的信号，而yield不会;


        try {

            // Thread中sleep函数的作用是让该线程进入休眠状态，让出cpu的执行时间给其他进程，该线程休眠后进入就绪队列和其他线程一起竞争cpu的执行时间。
            //
            //　所以sleep(0)的作用就是让该线程立即从运行阶段进入就绪队列而非等待队列，释放cpu时间，
            // 可以让操作系统切换其他线程来执行，提升效率。
            // 总得来说就是，sleep(0)让当前已完成功能的线程让出自己的资源（时间片）给其他线程，
            // “Thread.Sleep(0)作用,就是“触发操作系统立刻重新进行一次CPU竞争”。　让其他线程有竞争cpu资源的机会（该线程也在就绪队列参与竞争）

            Thread.sleep(0);
        } catch (InterruptedException e) {
            System.err.println( e.fillInStackTrace());
        }
    }
}
