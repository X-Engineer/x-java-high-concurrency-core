package com.crazymakercircle.disruptor;

import com.crazymakercircle.util.ThreadUtil;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LongEventSceneDemo {


    public static void handleEvent(LongEvent event, long sequence, boolean endOfBatch) {
        System.out.println(event.getValue());
    }

    @org.junit.Test
    public void testSimpleProducerDisruptorWithMethodRef() throws InterruptedException {
        // 消费者线程池
        Executor executor = Executors.newCachedThreadPool();
        // 环形队列大小，2的指数
        int bufferSize = 1024;

        // 构造  分裂者 （事件分发者）
        Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(LongEvent::new, bufferSize,
                executor,
                ProducerType.SINGLE,  //多个生产者
                new YieldingWaitStrategy());

        // 连接 消费者 处理器
        // 可以使用lambda来注册一个EventHandler
        disruptor.handleEventsWithWorkerPool(new LongEventHandler(),new LongEventHandler());
        // 开启 分裂者（事件分发）
        disruptor.start();

        // 获取环形队列，用于生产 事件
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();


        //3生产者，并发生产数据
        LongEventProducerWithTranslator producer = new LongEventProducerWithTranslator(ringBuffer);

        Thread thread = new Thread() {
            @Override
            public void run() {
                for (long i = 0; true; i++) {
                    //发布事件
                    producer.onData(i);
                    ThreadUtil.sleepSeconds(1);
                }
            }
        };
        thread.start();
        ThreadUtil.sleepSeconds(5);

    }


    @org.junit.Test
    public void testMultiProducerDisruptorWithMethodRef() throws InterruptedException {
        // 消费者线程池
        Executor executor = Executors.newCachedThreadPool();
        // 环形队列大小，2的指数
        int bufferSize = 1024;

        // 构造  分裂者 （事件分发者）
        Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(LongEvent::new, bufferSize,
                executor,
                ProducerType.MULTI,  //多个生产者
                new YieldingWaitStrategy());

        // 连接 消费者 处理器
        // 可以使用lambda来注册一个EventHandler
        disruptor.handleEventsWith(LongEventSceneDemo::handleEvent);
        // 开启 分裂者（事件分发）
        disruptor.start();

        // 获取环形队列，用于生产 事件
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();


        //3生产者，并发生产数据
        for (int l = 0; l < 3; l++) {
            LongEventProducerWithTranslator producer = new LongEventProducerWithTranslator(ringBuffer);

            Thread thread = new Thread() {
                @Override
                public void run() {
                    for (long i = 0; true; i++) {
                        //发布事件
                        producer.onData(i);
                        ThreadUtil.sleepSeconds(1);
                    }
                }
            };
            thread.setName("producer thread " + l);
            thread.start();
        }
        ThreadUtil.sleepSeconds(5);

    }
}
