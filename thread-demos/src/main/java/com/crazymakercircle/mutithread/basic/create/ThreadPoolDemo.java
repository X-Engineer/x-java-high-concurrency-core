package com.crazymakercircle.mutithread.basic.create;

import com.crazymakercircle.mutithread.basic.create3.CreateThreadPoolDemo;
import com.crazymakercircle.util.Print;
import com.crazymakercircle.util.ThreadUtil;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import static com.crazymakercircle.util.ThreadUtil.*;

/**
 * Created by 尼恩@疯狂创客圈.
 * 线程适用的场景一般是：存在相当比例非CPU耗时操作，如IO、网络操作，需要尽量提高并行化比率以提升CPU的利用率
 * 确定线程池的线程数
 * IO 密集型任务：线程数通常配置为 CPU 核数 * 2
 * CPU 密集型任务：线程数通常配置为 CPU 核数
 * 混合型任务：最佳线程数目 = (线程等待时间/线程CPU时间 + 1) * CPU核数
 * 举一个例子：比如在Web服务器处理HTTP请求时，假设平均线程CPU运行时间为100毫秒，而线程等待时间（比如包括DB操作、RPC操作、缓存操作等）为900毫秒，
 * 如果CPU核数为8，那么根据上面这个公式，估算如下：
 *      (900毫秒 + 100毫秒) / 100毫秒 * 8 = 10 * 8 = 80
 */

public class ThreadPoolDemo {
    public static final int MAX_TURN = 5;


    static int threadNo = 1;

    class RunTarget implements Runnable  //① 实现Runnable接口
    {
        public void run()  //② 在这些写业务逻辑
        {
            for (int j = 1; j < MAX_TURN; j++) {
                Print.cfo(getCurThreadName() + ", 轮次：" + j);
            }

            Print.cfo(getCurThreadName() + " 运行结束.");
        }
    }

    @Test
    public void testIoIntenseTargetThreadPool() {
        ThreadPoolExecutor pool = ThreadUtil.getIoIntenseTargetThreadPool();
        for (int i = 0; i < 2; i++) {
            pool.submit(new RunTarget());
        }
        ThreadUtil.sleepMilliSeconds(Integer.MAX_VALUE);
    }

    @Test
    public void testCpuIntenseTargetThreadPool() {
        ThreadPoolExecutor pool = ThreadUtil.getCpuIntenseTargetThreadPool();
        for (int i = 0; i < 2; i++) {
            pool.submit(new RunTarget());
        }
        ThreadUtil.sleepMilliSeconds(Integer.MAX_VALUE);
    }

    @Test
    public void testMixedThreadPool() {
        System.getProperties().setProperty(MIXED_THREAD_AMOUNT, "80");
        // 获取自定义的混合线程池
        ExecutorService pool =
                ThreadUtil.getMixedTargetThreadPool();
        for (int i = 0; i < 1000; i++) {
            try {
                sleepMilliSeconds(10);
                pool.submit(new CreateThreadPoolDemo.TargetTask());

            } catch (RejectedExecutionException e) {
                e.printStackTrace();
            }
        }
        //等待10s
        sleepSeconds(10);
        Print.tco("关闭线程池");
    }

}