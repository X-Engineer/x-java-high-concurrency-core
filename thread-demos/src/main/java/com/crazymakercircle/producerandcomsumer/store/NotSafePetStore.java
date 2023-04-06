package com.crazymakercircle.producerandcomsumer.store;

import com.crazymakercircle.petstore.actor.Consumer;
import com.crazymakercircle.petstore.actor.Producer;
import com.crazymakercircle.petstore.goods.Goods;
import com.crazymakercircle.petstore.goods.IGoods;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 尼恩@疯狂创客圈.
 * 在向数据缓冲区进行元素的增加或者提取时，多个线程在并发执行对amount、dataList两个成员操作时次序已经混乱，导致出现数据不一致和线程安全问题
 * （1）数据缓冲区静态实例。以元素类型为IGoods，定义了一个不安全的NotSafeDataBuffer数据缓冲区实例。
 * （2）生产者动作静态实例。这是一个Callable<IGoods>类型的匿名对象，其具体的动作为：首先调用Goods.produceOne()产生一个随机的商品，然后通过调用notSafeDataBuffer.add()方法将这个随机商品加入数据缓冲区实例中，完成生产者的动作。
 * （3）消费者动作静态实例。这也是一个Callable<IGoods>类型的匿名对象，其具体的动作为：调用notSafeDataBuffer.fetch()方法从数据区取出一个商品，完成消费者的动作。
 */
public class NotSafePetStore {
    //共享数据区，实例对象
    private static NotSafeDataBuffer<IGoods> notSafeDataBuffer = new NotSafeDataBuffer();

    //生产者执行的动作
    static Callable<IGoods> produceAction = () ->
    {
        //首先生成一个随机的商品
        IGoods goods = Goods.produceOne();
        //将商品加上共享数据区
        try {
            notSafeDataBuffer.add(goods);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return goods;
    };
    //消费者执行的动作
    static Callable<IGoods> consumerAction = () ->
    {
        // 从PetStore获取商品
        IGoods goods = null;
        try {
            goods = notSafeDataBuffer.fetch();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return goods;
    };


    public static void main(String[] args) throws InterruptedException {
        System.setErr(System.out);

        // 同时并发执行的线程数
        final int THREAD_TOTAL = 20;
        //线程池，用于多线程模拟测试
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_TOTAL);
        for (int i = 0; i < 5; i++) {
            //生产者线程每生产一个商品，间隔500ms
            threadPool.submit(new Producer(produceAction, 500));
            //消费者线程每消费一个商品，间隔1500ms
            threadPool.submit(new Consumer(consumerAction, 1500));
        }
    }

}

