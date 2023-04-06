package com.crazymakercircle.producerandcomsumer.store;

import com.crazymakercircle.util.Print;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

//共享数据区，类定义

/**
 * 在上一个版本的生产者-消费者问题的实现中，由于NotSafeDataBuffer线程安全问题，导致数据区的amount属性和dataList的长度在数据值上差别巨大。
 * 解决线程安全问题很简单，为临界区代码加上synchronized关键字即可，主要修改的是涉及操作两个临界区资源amount和dataList的代码，具体为DataBuffer的add(…)和fetch()方法。
 *
 * 虽然线程安全问题顺利解决了，但是以上解决方式使用了SafeDataBuffer实例的对象锁作为同步锁，这样一来，所有的生产、消费动作在执行过程中都需要抢占同一个同步锁，最终的结果是所有的生产、消费动作都被串行化了。
 * 高效率的生产者-消费者模式，生产、消费动作肯定不能串行执行，而是需要并行执行，而且并行化程度越高越好。如何既保障没有线程安全问题，又能提高生产、消费动作的并行化程度呢？这就是本书后续的实现版本需要解决的问题。
 * 如果需要开发出并行化程度更高的生产者-消费者模式实现版本，需要彻底地掌握和理解对象锁、synchronized等机制的内部原理，这就需要从Java对象的头部结构等基础知识学起。
 * @param <T>
 */
class SafeDataBuffer<T> {
    public static final int MAX_AMOUNT = 10;
    private List<T> dataList = new LinkedList<>();

    //保存数量
    private AtomicInteger amount = new AtomicInteger(0);

    /**
     * 向数据区增加一个元素
     */
    public synchronized void add(T element) throws Exception {
        if (amount.get() > MAX_AMOUNT) {
            Print.tcfo("队列已经满了！");
            return;
        }
        dataList.add(element);
        Print.tcfo(element + "");
        amount.incrementAndGet();

        //如果数据不一致，抛出异常
        if (amount.get() != dataList.size()) {
            throw new Exception(amount + "!=" + dataList.size());
        }
    }

    /**
     * 从数据区取出一个元素
     */
    public synchronized T fetch() throws Exception {
        if (amount.get() <= 0) {
            Print.tcfo("队列已经空了！");
            return null;
        }
        T element = dataList.remove(0);
        Print.tcfo(element + "");
        amount.decrementAndGet();
        //如果数据不一致，抛出异常
        if (amount.get() != dataList.size()) {
            throw new Exception(amount + "!=" + dataList.size());
        }
        return element;
    }
}