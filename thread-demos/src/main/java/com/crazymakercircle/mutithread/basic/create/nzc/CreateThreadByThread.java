package com.crazymakercircle.mutithread.basic.create.nzc;

import com.crazymakercircle.util.Print;

/**
 * @project: x-java-high-concurrency-core
 * @ClassName: CreateThreadByThread
 * @author: nzcer
 * @creat: 2023/3/18 13:57
 * @description:
 */

public class CreateThreadByThread {
    static int MAX_TURN = 5;
    static int threadNo = 1;

    static class MyThread extends Thread {
        public MyThread() {
            super("DemoThread-" + threadNo++);
        }

        @Override
        public void run() {
            for (int i = 0; i < MAX_TURN; i++) {
                Print.cfo(this.getName() + ", 轮次:" + i);
            }
            Print.cfo(this.getName() + " 运行结束");
        }
    }

    public static void main(String[] args) {
        MyThread myThread = null;
        try {
            for (int i = 0; i < 2; i++) {
                myThread = new MyThread();
                myThread.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Print.cfo(Thread.currentThread().getName());
    }
}
