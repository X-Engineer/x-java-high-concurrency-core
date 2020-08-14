package com.crazymakercircle.petstore.actor;

import com.crazymakercircle.util.Print;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消费者任务的定义
 * Created by 尼恩@疯狂创客圈.
 */
public class ConsumerTask implements Runnable
{

    //消费的时间间隔
    public static final int CONSUME_GAP = 1000;


    //消费总次数
    static AtomicInteger turn = new AtomicInteger(0);

    //消费者对象编号
    static AtomicInteger consumerNO = new AtomicInteger(1);

    //消费者名称
    String name;

    //消费的动作
    Callable action = null;

    //消费一次等待的时间，默认为1000ms
    int gap = 1000;

    public ConsumerTask(Callable action, int gap)
    {
        this.action = action;
        this.gap = gap;
        if(this.gap<=0)
        {
            this.gap=CONSUME_GAP;
        }
        name = "消费者-" + consumerNO.incrementAndGet();

    }

    @Override
    public void run()
    {
        while (true)
        {
            turn.incrementAndGet();
            try
            {
                Thread.sleep(gap);
                Object out = action.call();
                if (null != out)
                {
                    Print.tcfo("第" + turn.get() + "轮消费：" + out);
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}