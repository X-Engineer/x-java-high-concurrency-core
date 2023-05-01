package com.crazymakercircle.visiable;

import com.crazymakercircle.util.Print;

/**
 * join()规则的具体内容：
 * 如果线程A执行threadB.join()操作并成功返回，那么线程B中的任意操作先行发生于线程A的ThreadB.join()操作。join()规则和start()规则刚好相反，线程A等待子线程B完成后，当前线程B的赋值操作，线程A都能够看到。
 */
public class JoinExample {
    private int x = 0;
    private int y = 1;
    private boolean flag = false;

    public static void main(String[] args) throws InterruptedException {
        Thread.currentThread().setName("线程A");
        JoinExample joinExample = new JoinExample();

        Thread threadB = new Thread(joinExample::writer, "线程B");
        threadB.start();

        threadB.join();//线程A join线程B

        Print.tcfo("x:" + joinExample.x);
        Print.tcfo("y:" + joinExample.y);
        Print.tcfo("flag:" + joinExample.flag);
        Print.tcfo("本线程结束");
    }

    public void writer() {
        Print.tcfo("开始赋值操作");
        this.x = 100;
        this.y = 200;
        this.flag = true;
    }
}

