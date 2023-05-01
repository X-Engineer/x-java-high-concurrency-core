package com.crazymakercircle.visiable;

import com.crazymakercircle.util.Print;

/**
 * start()规则的具体内容：
 * 如果线程A执行ThreadB.start()操作启动线程B，那么线程A的ThreadB.start()操作先行发生于线程B中的任意操作。反过来说，如果主线程A启动子线程B后，线程B能看到线程A在启动操作前的任何操作。
 *
 *
 */
public class StartExample {
    private int x = 0;
    private int y = 1;
    private boolean flag = false;

    public static void main(String[] args) throws InterruptedException {
        Thread.currentThread().setName("线程A");
        StartExample startExample = new StartExample();

        Thread threadB = new Thread(startExample::writer, "线程B");
        //线程B启动前，线程A进行了多个内存操作
        Print.tcfo("开始赋值操作");
        startExample.x = 10;
        startExample.y = 20;
        startExample.flag = true;

        threadB.start(); //启动线程B
        Print.tcfo("线程结束");
    }

    public void writer() {
        Print.tcfo("x:" + x);
        Print.tcfo("y:" + y);
        Print.tcfo("flag:" + flag);
    }
}
