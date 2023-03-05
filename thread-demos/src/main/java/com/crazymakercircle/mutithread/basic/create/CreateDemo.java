package com.crazymakercircle.mutithread.basic.create;

import com.crazymakercircle.util.Print;

import static com.crazymakercircle.util.ThreadUtil.getCurThreadName;

/**
 * 通过继承 Thread 类创建多线程
 * Created by 尼恩@疯狂创客圈.
 */

public class CreateDemo {

    public static final int MAX_TURN = 5;


    static int threadNo = 1;

    /**
     * 这里为什么要将DemoThread设计成静态内部类呢？主要是为了方便访问外部类的成员属性和方法，和线程的使用没有任何关系。
     * 如果将DemoThread设计成外部类，最终的执行结果是一样的。
     */
    static class DemoThread extends Thread {

        public DemoThread() {
            super("Mall-" + threadNo++);
        }

        @Override
        public void run() {
            for (int i = 1; i < MAX_TURN; i++) {
                Print.cfo(getName() + ", 轮次：" + i);
            }
            Print.cfo(getName() + " 运行结束.");
        }
    }


    public static void main(String[] args) throws InterruptedException {
        Thread thread = null;
        //方法一：使用Thread子类创建和启动线程
        for (int i = 0; i < 2; i++) {
            thread = new DemoThread();
            thread.start();
        }

        Print.cfo(getCurThreadName() + " 运行结束.");
    }
}