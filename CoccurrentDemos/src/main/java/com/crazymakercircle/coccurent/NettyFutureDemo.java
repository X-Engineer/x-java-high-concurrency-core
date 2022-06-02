package com.crazymakercircle.coccurent;

import com.crazymakercircle.util.Logger;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.Callable;

/**
 * Created by 尼恩 at 疯狂创客圈
 */

public class NettyFutureDemo {

    public static final int SLEEP_GAP = 5000;


    public static String getCurThreadName() {
        return Thread.currentThread().getName();
    }

    static class HotWaterJob implements Callable<Boolean> //①
    {

        @Override
        public Boolean call() throws Exception //②
        {

            try {
                Logger.info("水壶加水");
                Logger.info("灌上凉水");
                Logger.info("放在火上");

                //线程睡眠一段时间，代表烧水中
                Thread.sleep(SLEEP_GAP);
                Logger.info("水开了");

            } catch (InterruptedException e) {
                Logger.info(" 发生异常被中断.");
                return false;
            }
            Logger.info(" 烧水工作，运行结束.");

            return true;
        }
    }

    static class WashJob implements Callable<Boolean> {

        @Override
        public Boolean call() throws Exception {


            try {
                Logger.info("洗茶壶");
                Logger.info("洗茶杯");
                Logger.info("拿茶叶");
                //线程睡眠一段时间，代表清洗中
                Thread.sleep(SLEEP_GAP / 5);
                Logger.info("洗完了");

            } catch (InterruptedException e) {
                Logger.info(" 清洗工作 发生异常被中断.");
                return false;
            }
            Logger.info(" 清洗工作  运行结束.");
            return true;
        }

    }

    //泡茶线程
    static class MainJob implements Runnable {

        volatile boolean waterOk = false;
        volatile boolean cupOk = false;
        int gap = SLEEP_GAP / 10;

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(gap);
                    Logger.info("读书中......");
                } catch (InterruptedException e) {
                    Logger.info(getCurThreadName() + "发生异常被中断.");
                }


            }
        }


        public void drinkTea() {
            if (waterOk && cupOk) {
                Logger.info("泡茶喝，茶喝完");
                this.waterOk = false;
                this.gap = SLEEP_GAP * 100;
            } else if (!waterOk) {
                Logger.info("烧水 没有完成，没有茶喝了");
            } else if (!cupOk) {
                Logger.info("洗杯子  没有完成，没有茶喝了");
            }

        }
    }

    public static void main(String args[]) {

        //新起一个线程，作为泡茶主线程
        MainJob mainJob = new MainJob();
        Thread mainThread = new Thread(mainJob);
        mainThread.setName("喝茶线程");
        mainThread.start();

        //烧水的业务逻辑
        Callable<Boolean> hotJob = new HotWaterJob();
        //清洗的业务逻辑
        Callable<Boolean> washJob = new WashJob();

        //创建 netty  线程池
        DefaultEventExecutorGroup npool = new DefaultEventExecutorGroup(2);

        //提交烧水的业务逻辑，取到异步任务
        io.netty.util.concurrent.Future<Boolean> hotFuture = npool.submit(hotJob);
        //绑定任务执行完成后的回调，到异步任务
        hotFuture.addListener(new GenericFutureListener() {
            @Override
            public void operationComplete(io.netty.util.concurrent.Future future) throws Exception {
                if (future.isSuccess()) {
                    mainJob.waterOk = true;
                    Logger.info("烧水 完成，尝试着去吃吃茶!");
                    mainJob.drinkTea();
                } else {
                    mainJob.waterOk = false;
                    Logger.info("烧水 失败啦!");
                }
            }
        });


        //提交清洗的业务逻辑，取到异步任务

        io.netty.util.concurrent.Future<Boolean> washFuture = npool.submit(washJob);
        //绑定任务执行完成后的回调，到异步任务

        washFuture.addListener(new GenericFutureListener() {
            @Override
            public void operationComplete(io.netty.util.concurrent.Future future) throws Exception {
                if (future.isSuccess()) {
                    mainJob.cupOk = true;
                    Logger.info("杯子洗 完成，尝试着去吃吃茶!");
                    mainJob.drinkTea();
                } else {
                    mainJob.cupOk = false;
                    Logger.info("杯子洗不了，没有茶喝了");

                }
            }
        });
    }


}