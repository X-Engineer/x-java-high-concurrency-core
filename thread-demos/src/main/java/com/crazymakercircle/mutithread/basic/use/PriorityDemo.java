package com.crazymakercircle.mutithread.basic.use;

import com.crazymakercircle.util.Print;

import java.util.concurrent.locks.LockSupport;

/**
 * Java中使用抢占式调度模型进行线程调度。priority实例属性的优先级越高，线程获得CPU时间片的机会就越多，但也不是绝对的。
 * Created by 尼恩@疯狂创客圈.
 */

public class PriorityDemo {
    public static final int SLEEP_GAP = 1000;

    static class PrioritySetThread extends Thread {
        static int threadNo = 1;

        public PrioritySetThread() {
            super("thread-" + threadNo);
            threadNo++;
        }

        public long opportunities = 0;

        public void run() {
            for (int i = 0; ; i++) {
                opportunities++;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

        PrioritySetThread[] threads = new PrioritySetThread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new PrioritySetThread();
            //优先级的设置，从1-10
            threads[i].setPriority(i + 1);
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();

        }

        Thread.sleep(SLEEP_GAP);

        /**
         * Thread类的stop()实例方法是一个过时的方法，也是一个不安全的方法。这里的安全指的是系统资源（文件、网络连接等）的安全——stop()实例方法可能导致资源状态不一致，
         * 或者说资源出现问题时很难定位。在实际开发过程中，不建议使用stop()实例方法。
         */
        for (int i = 0; i < threads.length; i++) {
            threads[i].stop();
        }

        for (int i = 0; i < threads.length; i++) {
            Print.cfo(threads[i].getName() +
                    ";优先级为-" + threads[i].getPriority() +
                    ";机会值为-" + threads[i].opportunities
            );
        }

    }
}