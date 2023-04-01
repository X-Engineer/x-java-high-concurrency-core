package com.crazymakercircle.mutithread.basic.use;

import com.crazymakercircle.util.Print;

import static com.crazymakercircle.util.ThreadUtil.getCurThread;
import static com.crazymakercircle.util.ThreadUtil.sleepMilliSeconds;

/**
 * Created by 尼恩@疯狂创客圈.
 * 从是否为守护线程的角度，对Java线程进行分类，分为用户线程和守护线程。守护线程和用户线程的本质区别是：
 * 二者与JVM虚拟机进程终止的方向不同。用户线程和JVM进程是主动关系，如果用户线程全部终止，JVM虚拟机进程也随之终止；守护线程和JVM进程是被动关系，如果JVM进程终止，所有的守护线程也随之终止。
 * 守护线程提供服务，是守护者，用户线程享受服务，是被守护者。只有用户线程全部终止了，相当于没有了被守护者，守护线程也就没有工作可做了，也就可以全部终止了。
 * 当然，用户线程全部终止，JVM进程也就没有继续的必要了。反过来说，只要有一个用户线程没有终止，JVM进程也不会退出。
 */

public class DaemonDemo {
    public static final int SLEEP_GAP = 500; //每一轮的睡眠时长
    public static final int MAX_TURN = 4; //用户线程执行轮次

    //守护线程实现类
    static class DaemonThread extends Thread {

        public DaemonThread() {
            super("daemonThread");
        }

        public void run() {
            Print.synTco("--daemon线程开始.");

            for (int i = 1; ; i++) {
                Print.synTco("--轮次：" + i + "--守护状态为:" + isDaemon());
                // 线程睡眠一会
                sleepMilliSeconds(SLEEP_GAP);
            }
        }

    }


    public static void main(String args[]) throws InterruptedException {

        Thread daemonThread = new DaemonThread();
        daemonThread.setDaemon(true);
        daemonThread.start();

        Thread userThread = new Thread(() ->
        {
            Print.synTco(">>用户线程开始.");
            for (int i = 1; i <= MAX_TURN; i++) {
                Print.synTco(">>轮次：" + i + " -守护状态为:" + getCurThread().isDaemon());
                sleepMilliSeconds(SLEEP_GAP);
            }
            Print.synTco(">>用户线程结束.");
        }, "userThread");
        userThread.start();
        //主线程合入userThread，等待userThread执行完成
//        userThread.join();
        Print.synTco(" 守护状态为:" + getCurThread().isDaemon());

        Print.synTco(" 运行结束.");
    }
}