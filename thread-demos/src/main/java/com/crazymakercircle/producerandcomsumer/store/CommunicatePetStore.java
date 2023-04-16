package com.crazymakercircle.producerandcomsumer.store;

import com.crazymakercircle.petstore.actor.Consumer;
import com.crazymakercircle.petstore.actor.Producer;
import com.crazymakercircle.petstore.goods.Goods;
import com.crazymakercircle.petstore.goods.IGoods;
import com.crazymakercircle.util.JvmUtil;
import com.crazymakercircle.util.Print;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 为了避免空轮询导致CPU时间片浪费，提高生产者-消费者实现版本的性能，接下来演示使用“等待-通知”的方式在生产者与消费者之间进行线程间通信。
 * 此实现版本大致需要定义以下三个同步对象：
 * （1）LOCK_OBJECT：用于临界区同步，临界区资源为数据缓冲区的dataList变量和amount变量。
 * （2）NOT_FULL：用于数据缓冲区的未满条件等待和通知。生产者在添加元素前需要判断数据区是否已满，如果是，生产者就进入NOT_FULL的同步区等待被通知，只要消费者消耗一个元素，数据区就是未满的，进入NOT_FULL的同步区发送通知。
 * （3）NOT_EMPTY：用于数据缓冲区的非空条件等待和通知。消费者在消耗元素前需要判断数据区是否已空，如果是，消费者就进入NOT_EMPTY的同步区等待被通知，只要生产者添加一个元素，数据区就是非空的，生产者会进入NOT_EMPTY的同步区发送通知。
 *
 * 调用wait()和notify()系列方法进行线程通信的要点如下：
 * （1）调用某个同步对象locko的wait()和notify()类型方法前，必须要取得这个锁对象的监视锁，所以wait()和notify()类型方法必须放在synchronized(locko)同步块中，如果没有获得监视锁，JVM就会报IllegalMonitorStateException异常。
 * （2）调用wait()方法时使用while进行条件判断，如果是在某种条件下进行等待，对条件的判断就不能使用if语句做一次性判断，而是使用while循环进行反复判断。只有这样才能在线程被唤醒后继续检查wait的条件，并在条件没有满足的情况下继续等待。
 */
public class CommunicatePetStore {

    public static final int MAX_AMOUNT = 10; //数据区长度


    //共享数据区，类定义
    static class DateBuffer<T> {
        //保存数据
        private List<T> dataList = new LinkedList<>();
        //保存数量
        private volatile int amount = 0;

        private final Object LOCK_OBJECT = new Object();
        private final Object NOT_FULL = new Object();
        private final Object NOT_EMPTY = new Object();

        // 向数据区增加一个元素
        public void add(T element) throws Exception {
            synchronized (NOT_FULL) {
                while (amount >= MAX_AMOUNT) {
                    Print.tcfo("队列已经满了！");
                    //等待未满通知
                    NOT_FULL.wait();
                }
            }
            synchronized (LOCK_OBJECT) {

                if (amount < MAX_AMOUNT) { // 加上双重检查，模拟双检锁在单例模式中应用
                    dataList.add(element);
                    amount++;
                }
            }
            synchronized (NOT_EMPTY) {
                //发送未空通知
                NOT_EMPTY.notify();
            }


        }

        /**
         * 从数据区取出一个商品
         */
        public T fetch() throws Exception {
            synchronized (NOT_EMPTY) {
                while (amount <= 0) {
                    Print.tcfo("队列已经空了！");
                    //等待未空通知
                    NOT_EMPTY.wait();
                }
            }

            T element = null;
            synchronized (LOCK_OBJECT) {
                if (amount > 0) {  // 加上双重检查，模拟双检锁在单例模式中应用
                    element = dataList.remove(0);
                    amount--;
                }
            }

            synchronized (NOT_FULL) {
                //发送未满通知
                NOT_FULL.notify();
            }
            return element;
        }
    }


    public static void main(String[] args) throws InterruptedException {
        Print.cfo("当前进程的ID是" + JvmUtil.getProcessID());
        System.setErr(System.out);
        //共享数据区，实例对象
        DateBuffer<IGoods> dateBuffer = new DateBuffer<>();

        //生产者执行的动作
        Callable<IGoods> produceAction = () ->
        {
            //首先生成一个随机的商品
            IGoods goods = Goods.produceOne();
            //将商品加上共享数据区
            dateBuffer.add(goods);
            return goods;
        };
        //消费者执行的动作
        Callable<IGoods> consumerAction = () ->
        {
            // 从PetStore获取商品
            IGoods goods = null;
            goods = dateBuffer.fetch();
            return goods;
        };
        // 同时并发执行的线程数
        final int THREAD_TOTAL = 20;
        //线程池，用于多线程模拟测试
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_TOTAL);

        //假定共11条线程，其中有10个消费者，但是只有1个生产者；
        final int CONSUMER_TOTAL = 11;
        final int PRODUCE_TOTAL = 1;

        for (int i = 0; i < PRODUCE_TOTAL; i++) {
            //生产者线程每生产一个商品，间隔50ms
            threadPool.submit(new Producer(produceAction, 50));
        }
        for (int i = 0; i < CONSUMER_TOTAL; i++) {
            //消费者线程每消费一个商品，间隔100ms
            threadPool.submit(new Consumer(consumerAction, 100));
        }

    }

}

