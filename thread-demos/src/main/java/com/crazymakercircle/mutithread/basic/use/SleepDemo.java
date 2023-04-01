package com.crazymakercircle.mutithread.basic.use;

import com.crazymakercircle.util.Logger;
import com.crazymakercircle.util.Print;
import org.junit.Test;

import static com.crazymakercircle.util.JvmUtil.getProcessID;
import static com.crazymakercircle.util.ThreadUtil.getCurThreadName;

/**
 * 线程的 sleep 操作
 * sleep的作用是让目前正在执行的线程休眠，让CPU去执行其他的任务。从线程状态来说，就是从执行状态变成限时阻塞状态。Sleep()方法定义在Thread类中，是一组静态方法
 * sleep()方法会有InterruptException受检异常抛出，如果调用了sleep()方法，就必须进行异常审查，捕获InterruptedException异常，或者再次通过方法声明存在InterruptedException异常。
 * Created by 尼恩@疯狂创客圈.
 */

public class SleepDemo {

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
                for (int i = 1; i < MAX_TURN; i++) {
                    Print.tco(getName() + ", 睡眠轮次：" + i);
                    // 线程睡眠一会
                    Thread.sleep(SLEEP_GAP);
                }
            } catch (InterruptedException e) {
                Print.tco(getName() + " 发生异常被中断.");

            }
            Print.tco(getName() + " 运行结束.");
        }

    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            Thread thread = new SleepThread();
            thread.start();
        }
        Print.tco(getCurThreadName() + " 运行结束.");
    }


    @Test
    public void sleepForever() {
        //获取进程id，避免去任务管理器查找
        Logger.cfo("进程ID=" + getProcessID());
        try {
            //main线程，无限制等待
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}