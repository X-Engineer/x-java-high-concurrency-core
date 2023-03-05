package com.crazymakercircle.mutithread.basic.create;

import com.crazymakercircle.util.Print;

import static com.crazymakercircle.util.ThreadUtil.getCurThreadName;

/**
 * Created by 尼恩@疯狂创客圈.
 */

public class CreateDemo2 {
    public static final int MAX_TURN = 5;


    static int threadNo = 1;

    static class RunTarget implements Runnable  //① 实现Runnable接口
    {
        public void run()  //② 在这些写业务逻辑
        {
            for (int j = 1; j < MAX_TURN; j++) {
                /**
                 * run()方法实现版本中在获取当前线程的名称时，所用的方法是在外部类ThreadUtil中定义的getCurThreadName()静态方法，而不是Thread类的getName()实例方法。
                 * 原因是：这个RunTarget内部类和Thread类不再是继承关系，无法直接调用Thread类的任何实例方法。通过实现Runnable接口的方式创建的执行目标类，
                 * 如果需要访问线程的任何属性和方法，必须通过Thread.currentThread()获取当前的线程对象，通过当前线程对象间接访问。
                 */
                Print.cfo(getCurThreadName() + ", 轮次：" + j);
            }

            Print.cfo(getCurThreadName() + " 运行结束.");
        }
    }

    public static void main(String args[]) throws InterruptedException {
        Thread thread = null;

        //方法2.1：使用实现Runnable的实现类创建和启动线程

        for (int i = 0; i < 2; i++) {
            Runnable target = new RunTarget();
            thread = new Thread(target, "RunnableThread" + threadNo++);
            thread.start();
        }

        //方法2.2：使用实现Runnable的匿名类创建和启动线程

        for (int i = 0; i < 2; i++) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 1; j < MAX_TURN; j++) {
                        Print.cfo(getCurThreadName() + ", 轮次：" + j);
                    }
                    Print.cfo(getCurThreadName() + " 运行结束.");
                }
            }, "RunnableThread" + threadNo++);
            thread.start();
        }
        //方法2.3：使用实现lambor表达式创建和启动线程
        for (int i = 0; i < 2; i++) {
            thread = new Thread(() ->
            {
                for (int j = 1; j < MAX_TURN; j++) {
                    Print.cfo(getCurThreadName() + ", 轮次：" + j);
                }
                Print.cfo(getCurThreadName() + " 运行结束.");
            }, "RunnableThread" + threadNo++);
            thread.start();
        }
        Print.cfo(getCurThreadName() + " 运行结束.");
    }
}