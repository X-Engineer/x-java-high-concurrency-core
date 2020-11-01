package com.crazymakercircle.designmodel.masterworker;

import com.crazymakercircle.util.ThreadUtil;

import java.util.concurrent.TimeUnit;

public class MasterWorkerTest
{
    static class SimpleTask extends Task<Integer>
    {
        @Override
        protected Integer doExecute()
        {
            return getId();
        }
    }

    public static void main(String[] args)
    {
        //创建Master ，包含四个worker，并启动master的执行线程
        Master<SimpleTask, Integer> master = new Master<>(4);

        //定期向master提交任务
        ThreadUtil.scheduleAtFixedRate(() -> master.submit(
                new SimpleTask()),
                2, TimeUnit.SECONDS);

        //定期从master提取结果
        ThreadUtil.scheduleAtFixedRate(
                () -> master.printResult(),
                5, TimeUnit.SECONDS);

    }

}
