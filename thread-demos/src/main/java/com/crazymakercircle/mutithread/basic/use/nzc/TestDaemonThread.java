package com.crazymakercircle.mutithread.basic.use.nzc;

import java.util.concurrent.TimeUnit;

/**
 * @project: x-java-high-concurrency-core
 * @ClassName: TestDaemonThread
 * @author: nzcer
 * @creat: 2023/3/31 13:36
 * @description: 测试主线程结束，子线程会跟着结束吗？
 */
public class TestDaemonThread {
    public static void main(String[] args) {
        Thread mainThread = Thread.currentThread();
        ChildThread childThread = new ChildThread(mainThread);
        childThread.testThreadExitWithOutDaemon(); // 默认，主线程结束，子线程不会跟着结束
        //childThread.testThreadExitWithDaemon(); // 子线程设置为守护线程后，主线程结束，子线程结束
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class ChildThread {
    private Thread main;

    public ChildThread(Thread thread) {
        this.main = thread;
    }

    /**
     * Java 多线程默认不是守护线程
     * 将 main 主方法线程传递入 ChildThread 类中，再在 testThreadExit 方法中将 main 主方法线程停止掉，
     * 这里为了能立刻生效所以使用了stop方法（stop 方法是不安全的，不推荐使用，可以换成interrupt方法中断，但它不会立刻生效）
     */
    public void testThreadExitWithOutDaemon() {
        Thread thread = new Thread(() -> {
            try {
                System.out.println("结束 main 线程");
                // 这里为了能立刻生效所以使用了stop方法（stop 方法是不安全的，不推荐使用，可以换成interrupt方法中断，但它不会立刻生效）
                main.interrupt();
                // 保证主线程已经退出
                TimeUnit.SECONDS.sleep(4);
                System.out.println(main.getName() + " state: " + main.getState());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(Thread.currentThread().getName() + " state: " + Thread.currentThread().getState());
        });
        // Java 多线程默认不是守护线程
        thread.setDaemon(false);
        thread.start();

        /**
         * 打印结果
         * 结束 main 线程
         * Exception in thread "main" java.lang.RuntimeException: java.lang.InterruptedException: sleep interrupted
         * 	at com.crazymakercircle.mutithread.basic.use.nzc.TestDaemonThread.main(TestDaemonThread.java:20)
         * Caused by: java.lang.InterruptedException: sleep interrupted
         * 	at java.lang.Thread.sleep(Native Method)
         * 	at java.lang.Thread.sleep(Thread.java:340)
         * 	at java.util.concurrent.TimeUnit.sleep(TimeUnit.java:386)
         * 	at com.crazymakercircle.mutithread.basic.use.nzc.TestDaemonThread.main(TestDaemonThread.java:18)
         * main state: TERMINATED
         * Thread-0 state: RUNNABLE
         *
         * 可以看出 main 主线程已经直接停止了，而子线程还在继续执行，程序并没有直接退出
         */
    }

    /**
     * Java 多线程默认不是守护线程
     * 将 main 主方法线程传递入 ChildThread 类中，再在 testThreadExit 方法中将 main 主方法线程停止掉，
     * 这里为了能立刻生效所以使用了stop方法（stop 方法是不安全的，不推荐使用，可以换成interrupt方法中断，但它不会立刻生效）
     */
    public void testThreadExitWithDaemon() {
        Thread thread = new Thread(() -> {
            try {
                System.out.println("结束 main 线程");
                // 这里为了能立刻生效所以使用了stop方法（stop 方法是不安全的，不推荐使用，可以换成interrupt方法中断，但它不会立刻生效）
                main.interrupt();
                // 保证主线程已经退出
                TimeUnit.SECONDS.sleep(4);
                System.out.println(main.getName() + " state: " + main.getState());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(Thread.currentThread().getName() + " state: " + Thread.currentThread().getState());
        });
        // Java 多线程默认不是守护线程，此处将子线程设置为守护线程
        // 守护线程的特点是：
        // - 在 JVM 中，只有守护线程存在时，JVM 才会退出。因此，守护线程通常被用于执行一些后台任务，如垃圾回收、内存管理等。
        // - 守护线程通常不需要进行资源清理和释放，因为当其他非守护线程结束时，JVM 会自动关闭守护线程并释放它们占用的资源。
        // - 守护线程不能持有任何会影响 JVM 关闭的资源，如打开文件、数据库连接等，因为当 JVM 尝试关闭这些资源时，它们可能会导致 JVM 无法正常关闭。
        // - 守护线程和普通线程一样，可以通过 Thread 类的 setDaemon(boolean on) 方法来设置它是否为守护线程。默认情况下，所有线程都是非守护线程。
        thread.setDaemon(true);
        thread.start();
        /**
         * 打印结果：
         * 结束 main 线程
         * Exception in thread "main" java.lang.RuntimeException: java.lang.InterruptedException: sleep interrupted
         * 	at com.crazymakercircle.mutithread.basic.use.nzc.TestDaemonThread.main(TestDaemonThread.java:21)
         * Caused by: java.lang.InterruptedException: sleep interrupted
         * 	at java.lang.Thread.sleep(Native Method)
         * 	at java.lang.Thread.sleep(Thread.java:340)
         * 	at java.util.concurrent.TimeUnit.sleep(TimeUnit.java:386)
         * 	at com.crazymakercircle.mutithread.basic.use.nzc.TestDaemonThread.main(TestDaemonThread.java:19)
         *
         * Process finished with exit code 1
         *
         */
    }
}