package com.crazymakercircle.mutithread.basic.use;

import com.crazymakercircle.util.Print;

/**
 * 举一个例子，假设有两个线程A和B。现在线程A在执行过程中对另一个线程B的执行有依赖，具体的依赖为：
 * 线程A需要将线程B的执行流程合并到自己的执行流程中（至少表面如此），这就是线程合并，被动方线程B可以叫作被合并线程
 * Created by 尼恩@疯狂创客圈.
 */

public class JoinDemo {

    public static final int SLEEP_GAP = 5000;//睡眠时长
    public static final int MAX_TURN = 50;//睡眠次数

    static class SleepThread extends Thread {
        static int threadSeqNumber = 1;

        public SleepThread() {
            super("sleepThread-" + threadSeqNumber);
            threadSeqNumber++;
        }

        public void run() {
            try {
                Print.tco(getName() + " 进入睡眠.");
                // 线程睡眠一会
                Thread.sleep(SLEEP_GAP);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Print.tco(getName() + " 发生被异常打断.");
                return;
            }
            Print.tco(getName() + " 运行结束.");
        }

    }

    public static void main(String[] args) {
        Thread thread1 = new SleepThread();
        Print.tco("启动 thread1.");
        thread1.start();
        try {
            /**
             * 依赖的线程A叫作甲方线程，被依赖的线程B叫作乙方线程。简单理解线程合并就是甲方线程调用乙方线程的join()方法，
             * 在执行流程上将乙方线程合并到甲方线程。甲方线程等待乙方线程执行完成后，甲方线程再继续执行。
             * jps 获取 id
             * jstack id，查看 main 线程的 state
             */
            thread1.join();//合并线程1，不限时
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Print.tco("启动 thread2.");
        //启动第二条线程，并且进行限时合并，等待时间为1秒
        Thread thread2 = new SleepThread();
        thread2.start();
        try {
            thread2.join(1000);//限时合并，限时1秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Print.tco("线程运行结束.");
    }
}