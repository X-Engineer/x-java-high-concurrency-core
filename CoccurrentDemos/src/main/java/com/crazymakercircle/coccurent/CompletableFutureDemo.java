package com.crazymakercircle.coccurent;

import com.crazymakercircle.util.Logger;
import com.crazymakercircle.util.ThreadUtil;

import java.util.concurrent.CompletableFuture;

import static com.crazymakercircle.util.ThreadUtil.sleepSeconds;

public class CompletableFutureDemo {
    private static final int SLEEP_GAP = 5;//等待3秒

    public static void main(String[] args) {

        // 任务 1
        CompletableFuture<Boolean> washJob =
                CompletableFuture.supplyAsync(() ->
                {
                    Logger.tcfo("洗茶杯 starting ");
                    //线程睡眠一段时间，代表清洗中
                    sleepSeconds(SLEEP_GAP/5);
                    Logger.tcfo("洗完了");

                    return true;
                });

        // 任务 2
        CompletableFuture<Boolean> hotJob =
                CompletableFuture.supplyAsync(() ->
                {
                    Logger.tcfo("烧开水  starting");
                    Logger.tcfo("烧开水");

                    //线程睡眠一段时间，代表烧水中
                    sleepSeconds(SLEEP_GAP);
                    Logger.tcfo("水开了");
                    return true;

                });


        // 任务 3：任务 1 和任务 2 完成后执行：泡茶
        CompletableFuture<String> drinkJob =
                washJob.thenCombine(hotJob, (hotOk, washOK) ->
                {
                    if (hotOk && washOK) {
                        Logger.tcfo("泡茶喝，茶喝完");
                        return "茶喝完了";
                    }
                    return "没有喝到茶";
                });

        ThreadUtil.sleepSeconds(1000000);
    }
}

