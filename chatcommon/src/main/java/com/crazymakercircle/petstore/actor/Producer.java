package com.crazymakercircle.petstore.actor;

import com.crazymakercircle.util.Print;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static com.crazymakercircle.util.ThreadUtil.sleepMilliSeconds;

/**
 * 生产者任务的定义
 * 生产者-消费者模式在本书中有多个不同版本的实现，这些版本的区别在于数据缓冲区（DataBuffer）类以及相应的生产、消费动作（Action）不同，而生产者类、消费者类的执行逻辑是相同的。
 * “分离变与不变”是软件设计的一个基本原则。现在将生产者类、消费者类与具体的生产、消费动作解耦，从而使得生产者类、消费者类的代码在后续可以复用。
 * Created by 尼恩@疯狂创客圈.
 */
public class Producer implements Runnable {
    //生产的时间间隔，产一次等待的时间，默认为200ms
    public static final int PRODUCE_GAP = 200;

    //总次数
    // 注意：
    // 不是单个的次数
    // 是所有生产者的总的生产次数
    static final AtomicInteger TURN = new AtomicInteger(0);

    //生产者对象编号
    static final AtomicInteger PRODUCER_NO = new AtomicInteger(1);

    //生产者名称
    String name = null;

    //生产的动作
    Callable action = null;

    int gap = PRODUCE_GAP;

    public Producer(Callable action, int gap) {
        this.action = action;
        this.gap = gap;
        if (this.gap <= 0) {
            this.gap = PRODUCE_GAP;
        }
        name = "生产者-" + PRODUCER_NO.incrementAndGet();

    }

    public Producer(Callable action) {
        this.action = action;
        this.gap = PRODUCE_GAP;
        name = "生产者-" + PRODUCER_NO.incrementAndGet();

    }

    @Override
    public void run() {
        while (true) {

            try {
                //执行生产动作
                Object out = action.call();
                //输出生产的结果
                if (null != out) {
                    Print.tcfo("第" + TURN.get() + "轮生产：" + out);
                }
                //每一轮生产之后，稍微等待一下
                sleepMilliSeconds(gap);

                //增加生产轮次
                TURN.incrementAndGet();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}