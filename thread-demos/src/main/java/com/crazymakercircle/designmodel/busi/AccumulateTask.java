package com.crazymakercircle.designmodel.busi;

import com.crazymakercircle.util.Print;

import java.util.concurrent.RecursiveTask;

public class AccumulateTask extends RecursiveTask<Integer>
{

    private static final int THRESHOLD = 2;
    private int start;
    private int end;


    public AccumulateTask(int start, int end)
    {
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute()
    {
        int sum = 0;
        boolean canCompute = (end - start) <= THRESHOLD;

        if (canCompute)
        {//任务已经足够小，Recursive结束，可以直接计算并返回结果
            for (int i = start; i <= end; i++)
            {
                sum += i;
            }
            Print.tcfo("执行任务，计算" + start + "到" + end + "的和，结果是：" + sum);

        } else
        { //任务过大，需要切割
            Print.tcfo("切割任务：将" + start + "到" + end + "的和一分为二");
            int middle = (start + end) / 2;
            //切割成两个子任务
            AccumulateTask lTask = new AccumulateTask(start, middle);
            AccumulateTask rTask = new AccumulateTask(middle + 1, end);
            //执行子任务
            lTask.fork();
            rTask.fork();
            //等待子任务的完成，并获取执行结果
            int leftResult = lTask.join();
            int rightResult = rTask.join();
            //合并子任务
            sum = leftResult + rightResult;
        }
        return sum;
    }
}