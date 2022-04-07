package com.crazymaker.common;

import com.crazymakercircle.threadpool.IoIntenseTargetThreadPoolLazyHolder;
import com.crazymakercircle.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.ThreadPoolExecutor;

import static com.crazymakercircle.util.ThreadUtil.getCurThreadName;

@Slf4j
public class ThreadPoolTest {


    @Test
    public void testIoIntenseTargetThreadPool() {
        ThreadPoolExecutor pool = IoIntenseTargetThreadPoolLazyHolder.getInnerExecutor();
        ;
        for (int i = 0; i < 2; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    for (int j = 1; j < 10; j++) {
                        log.info(getCurThreadName() + ", 轮次：" + j);
                    }
                    log.info(getCurThreadName() + " 运行结束.");
                }
            });

        }
        ThreadUtil.sleepMilliSeconds(1000);
    }

}
