package com.crazymaker.common;

import com.crazymakercircle.cocurrent.DisruptorTaskScheduler;
import com.crazymakercircle.util.Print;
import com.crazymakercircle.util.ThreadUtil;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DisruptorTaskSchedulerTester
{
    private ExecutorService pool = ThreadUtil.getIoIntenseTargetThreadPool();

    /**
     * 测试用例：DisruptorTaskScheduler
     */
    @Test
    public void taskSchedulerTest() throws InterruptedException
    {
        /**
         * 提交的请求次数
         */
        int index = 10;
        while (--index > 0)
        {
            //使用固定20个线程的线程池发起请求
            int finalIndex = index;
            DisruptorTaskScheduler.add(() ->
            {
                try
                {
                    Thread.sleep(200);
                    Print.tcfo("index = " + finalIndex);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            });
        }
        Thread.sleep(Integer.MAX_VALUE);
    }


}
