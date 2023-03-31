package com.crazymakercircle.mutithread.basic.use.nzc;

import java.util.concurrent.TimeUnit;

/**
 * @project: x-java-high-concurrency-core
 * @ClassName: ParentTest
 * @author: nzcer
 * @creat: 2023/3/31 14:42
 * @description:
 * 问题1：Main 线程中创建其他耗时的线程后自己结束了，耗时线程会跟着结束吗？
 * 回答1：主线程，只是个普通的非守护线程，用来启动应用程序，不能设置成守护线程；除此之外，它跟其他非守护线程没有什么不同。主线程执行结束，其他线程一样可以正常执行。
 * 问题2：Main 线程中创建其他耗时的守护线程后自己结束了，耗时的守护线程会跟着结束吗？
 * 回答2：在这种情况下，的确主线程退出后子线程就立刻结束了，但是这是属于 JVM 的底层实现机制，并不是说主线程和子线程之间存在依赖关系。
 */
public class ParentTest {
    public static void main(String[] args) {
        System.out.println("parent thread begin");

        PlainChildThread t1 = new PlainChildThread("thread1");
        PlainChildThread t2 = new PlainChildThread("thread2");
        // 下面两行注释打开，则是设置子线程为守护线程
        //t1.setDaemon(true);
        //t2.setDaemon(true);
        t1.start();
        t2.start();

        System.out.println("parent thread over ");
    }
}

class PlainChildThread extends Thread {
    private String name = null;

    public PlainChildThread(String name) {
        super(name);
        this.name = name;
    }

    @Override
    public void run() {
        System.out.println(this.name + "--child thead begin");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        if ("thread1".equals(this.name)) {
            int cnt = 0;
            while (cnt < 10) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(getName() + ":" + cnt++);
            }
        }
        System.out.println(this.name + "--child thead over");
    }
}