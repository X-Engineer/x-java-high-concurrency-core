package com.crazymakercircle.petstore.actor;

import com.crazymakercircle.util.Print;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 生产者任务的定义
 * Created by 尼恩@疯狂创客圈.
 */
public class ProducerTask implements Runnable
{
    //生产的时间间隔
    public static final int PRODUCE_GAP = 1000;

    //总次数
    static AtomicInteger turn = new AtomicInteger(0);

    //生产者对象编号
    static AtomicInteger producerNO = new AtomicInteger(1);

    //生产者名称
    String name;

    //生产的动作
    Callable action = null;


    //生产一次等待的时间，默认为1000ms
    int gap = 1000;

    public ProducerTask(Callable action, int gap)
    {
        this.action = action;
        this.gap = gap;
        if (this.gap <= 0)
        {
            this.gap = PRODUCE_GAP;
        }
        name = "生产者-" + producerNO.incrementAndGet();

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
                    Print.tcfo("第" + turn.get() + "轮生产：" + out);
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}