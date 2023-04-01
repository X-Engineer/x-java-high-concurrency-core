package com.crazymakercircle.mutithread.basic.create.nzc;

import com.crazymakercircle.util.Print;

/**
 * @project: x-java-high-concurrency-core
 * @ClassName: CreateThreadByRunnable
 * @author: nzcer
 * @creat: 2023/3/18 14:07
 * @description:
 */
public class CreateThreadByRunnable {
    static int MAX_TURN = 5;
    static int threadNo = 1;

    static String getThreadName() {
        return Thread.currentThread().getName();
    }

    static class MyRunnable implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < MAX_TURN; i++) {
                Print.cfo(getThreadName() + ", 轮次:" + i);
            }
            Print.cfo(getThreadName() + " 运行结束");
        }
    }

    public static void main(String[] args) {
        MyRunnable myRunnable = new MyRunnable();
        for (int i = 0; i < 2; i++) {
            Thread t = new Thread(myRunnable, "RunnableThread-" + i);
            t.start();
        }

        for (int i = 0; i < 2; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Print.cfo(getThreadName() + "使用实现Runnable的匿名类创建和启动线程");
                }
            }, "AnonymousInnerThread-" + i).start();
        }

        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                Print.cfo(getThreadName() + " 使用实现lambda表达式创建和启动线程");
            }, "lambda-" + i).start();
        }
    }
}
