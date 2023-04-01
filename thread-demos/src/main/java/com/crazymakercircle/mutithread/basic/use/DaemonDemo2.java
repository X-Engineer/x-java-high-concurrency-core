package com.crazymakercircle.mutithread.basic.use;

import com.crazymakercircle.util.Print;

import static com.crazymakercircle.util.ThreadUtil.getCurThreadName;
import static com.crazymakercircle.util.ThreadUtil.sleepMilliSeconds;

/**
 * Created by 尼恩@疯狂创客圈.
 * （1）守护线程必须在启动前将其守护状态设置为true，启动之后不能再将用户线程设置为守护线程，否则JVM会抛出一个InterruptedException异常。具体来说，如果线程为守护线程，就必须在线程实例的start()方法调用之前调用线程实例的setDaemon(true)，设置其daemon实例属性值为true。
 * （2）守护线程存在被JVM强行终止的风险，所以在守护线程中尽量不去访问系统资源，如文件句柄、数据库连接等。守护线程被强行终止时，可能会引发系统资源操作不负责任的中断，从而导致资源不可逆的损坏。
 * （3）守护线程创建的线程也是守护线程。在守护线程中创建的线程，新的线程都是守护线程。在创建之后，如果通过调用setDaemon(false)将新的线程显式地设置为用户线程，新的线程可以调整成用户线程。
 */

public class DaemonDemo2 {

    public static final int SLEEP_GAP = 500;
    public static final int MAX_TURN = 5;


    static class NormalThread extends Thread {
        static int threadNo = 1;

        public NormalThread() {
            super("normalThread-" + threadNo);
            threadNo++;
        }

        public void run() {
            for (int i = 0; ; i++) {
                sleepMilliSeconds(SLEEP_GAP);
                Print.synTco(getName() + ", 守护状态为:" + isDaemon());

            }

        }

    }


    public static void main(String args[]) throws InterruptedException {
        Thread daemonThread = new Thread(() ->
        {
            for (int i = 0; i < 5; i++) {
                Thread normalThread = new NormalThread();
//                normalThread.setDaemon(false);
                normalThread.start();
            }
        }, "daemonThread");
        daemonThread.setDaemon(true);
        daemonThread.start();
        //这里，一定不能让main线程结束，否则看不到结果
        sleepMilliSeconds(SLEEP_GAP);

        Print.synTco(getCurThreadName() + " 运行结束.");
    }


}