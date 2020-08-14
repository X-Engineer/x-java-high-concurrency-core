package com.crazymakercircle.basic.demo.lock;

import com.crazymakercircle.basic.demo.lock.busi.PetStoreWithReadWriteLock;
import com.crazymakercircle.basic.demo.lock.busi.PetStoreWithStampedLock;
import com.crazymakercircle.petstore.actor.ConsumerTask;
import com.crazymakercircle.petstore.actor.ProducerTask;
import com.crazymakercircle.petstore.goods.Goods;
import com.crazymakercircle.petstore.goods.IGoods;
import com.crazymakercircle.util.Print;
import org.apache.commons.collections4.Predicate;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.LockSupport;

public class ReadWriteLockTest
{

    @org.junit.Test
    public void testShareLock() throws InterruptedException
    {
        // 排队总人数（请求总数）
        final int CLIENT_TOTAL = 10;
        // 可同时受理业务的窗口数量（同时并发执行的线程数）
        final int THREAD_TOTAL = 2;
        //线程池，用于多线程模拟测试
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_TOTAL);
        //创建并发信号量
        final Semaphore semaphore = new Semaphore(THREAD_TOTAL);
        final CountDownLatch countDownLatch = new CountDownLatch(CLIENT_TOTAL);
        for (int i = 0; i < CLIENT_TOTAL; i++)
        {
            final int count = i;
            threadPool.execute(() ->
            {
                try
                {
                    //抢占一个信号
                    semaphore.acquire(1);
                    //模拟业务操作： 处理排队业务
                    process(count);
                    //释放一个信号
                    semaphore.release(1);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        threadPool.shutdown();
    }

    /**
     * 模拟业务操作： 处理排队业务
     */
    private static void process(int i) throws InterruptedException
    {
        Print.tcfo("受理处理中。。。,服务号: " + i);
        Thread.sleep(1000);
    }


    @org.junit.Test
    public void testPetStoreWithReadWriteLock() throws InterruptedException
    {

        // 同时并发执行的线程数
        final int THREAD_TOTAL = 20;
        //线程池，用于多线程模拟测试
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_TOTAL);

        PetStoreWithReadWriteLock petStore = new PetStoreWithReadWriteLock();

        //生产者执行的动作
        Callable<IGoods> produceAction = () ->
        {
            //首先生成一个随机的商品
            IGoods goods = Goods.produceOne();

            //将商品加上共享数据区
            petStore.add(goods);
            return goods;
        };
        //消费者执行的动作
        Callable<IGoods> consumerAction = () ->
        {
            // 从PetStore获取商品
            IGoods goods = petStore.fetch();
            return goods;
        };

        Print.cfo("启动生产者和消费者");
        for (int i = 0; i < 2; i++)
        {
            //生产者线程每生产一个商品，间隔500ms
            threadPool.submit(new ProducerTask(produceAction, 500));
            //消费者线程每消费一个商品，间隔1500ms
            threadPool.submit(new ConsumerTask(consumerAction, 1500));
        }

        //查询的条件
        Predicate<IGoods> predicate =
                goods -> goods.getType().equals(IGoods.Type.FOOD);

        //查询任务
        Callable searchTask = () ->
        {
            while (true)
            {
                // 从PetStore获取商品
                List<IGoods> goodsList = petStore.search(predicate);
                Print.cfo("宠物店中宠物食品数为：" + goodsList.size());
                LockSupport.parkNanos(500 * 1000L * 1000L);
            }
        };
        Print.cfo("启动查询线程");
        for (int i = 0; i < 10; i++)
        {
            threadPool.submit(searchTask);
        }
        Thread.sleep(Integer.MAX_VALUE);
        //threadPool.shutdown();
    }

    @org.junit.Test
    public void testPetStoreWithStampedLock() throws InterruptedException
    {

        // 同时并发执行的线程数
        final int THREAD_TOTAL = 20;
        //线程池，用于多线程模拟测试
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_TOTAL);

        PetStoreWithStampedLock petStore = new PetStoreWithStampedLock();

        //生产者执行的动作
        Callable<IGoods> produceAction = () ->
        {
            //首先生成一个随机的商品
            IGoods goods = Goods.produceOne();

            //将商品加上共享数据区
            petStore.add(goods);
            return goods;
        };
        //消费者执行的动作
        Callable<IGoods> consumerAction = () ->
        {
            // 从PetStore获取商品
            IGoods goods = petStore.fetch();
            return goods;
        };

        Print.cfo("启动生产者和消费者");
        for (int i = 0; i < 2; i++)
        {
            //生产者线程每生产一个商品，间隔500ms
            threadPool.submit(new ProducerTask(produceAction, 500));
            //消费者线程每消费一个商品，间隔1500ms
            threadPool.submit(new ConsumerTask(consumerAction, 1500));
        }

        //查询的条件
        Predicate<IGoods> predicate =
                goods -> goods.getType().equals(IGoods.Type.FOOD);

        //查询任务
        Runnable searchTask = () ->
        {
            while (true)
            {
                // 从PetStore获取商品
                List<IGoods> goodsList = petStore.search(predicate);
                Print.cfo("宠物店中宠物食品数为：" + goodsList.size());
                LockSupport.parkNanos(500 * 1000L * 1000L);
            }
        };
        Print.cfo("启动查询线程");
        for (int i = 0; i < 10; i++)
        {
            threadPool.submit(searchTask);
        }
        Thread.sleep(Integer.MAX_VALUE);
        //threadPool.shutdown();
    }

}
