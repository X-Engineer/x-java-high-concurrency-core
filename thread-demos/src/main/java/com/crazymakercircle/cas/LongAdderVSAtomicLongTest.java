package com.crazymakercircle.cas;
//...省略import

import com.crazymakercircle.util.Print;
import com.crazymakercircle.util.ThreadUtil;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Java 8提供了一个新的类LongAdder，以空间换时间的方式提升高并发场景下CAS操作的性能。
 * LongAdder的核心思想是热点分离，与ConcurrentHashMap的设计思想类似：将value值分离成一个数组，当多线程访问时，通过Hash算法将线程映射到数组的一个元素进行操作；而获取最终的value结果时，则将数组的元素求和。
 *
 * LongAdder和AtomicLong的对比实验，使用10个线程，每个线程累加1000次
 * 随着累加次数的增加，CAS操作的次数急剧增多，AtomicLong的性能急剧下降。从对比实验的结果可以看出，在CAS争用最为激烈的场景下，LongAdder的性能是AtomicLong的8倍
 *
 * AtomicLong使用内部变量value保存着实际的long值，所有的操作都是针对该value变量进行的。也就是说，在高并发环境下，value变量其实是一个热点，也就是N个线程竞争一个热点。重试线程越多，就意味着CAS的失败概率更高，从而进入恶性CAS空自旋状态。
 * LongAdder的基本思路是分散热点，将value值分散到一个数组中，不同线程会命中到数组的不同槽（元素）中，各个线程只对自己槽中的那个值进行CAS操作。这样热点就被分散了，冲突的概率就小很多
 * 如果要获得完整的LongAdder存储的值，只要将各个槽中的变量值累加，返回最终累加之后的值即可。
 *
 * LongAdder继承于Striped64类，base值和cells数组都在Striped64类中定义
 * Striped64内部包含一个base和一个Cell[]类型的cells数组，cells数组又叫哈希表。
 * 在没有竞争的情况下，要累加的数通过CAS累加到base上；如果有竞争的话，会将要累加的数累加到cells数组中的某个Cell元素里面。所以Striped64的整体值value为base+∑[0~n]cells。
 *
 * CAS 操做的弊端主要由以下三点：
 * 1.ABA 问题：ABA问题的解决思路是使用版本号。在变量前面追加上版本号，每次变量更新的时候将版本号加1，典型的有 AtomicStampedReference
 * 2.只能保证一个共享变量之间的原子性操作：一个比较简单的规避方法为：把多个共享变量合并成一个共享变量来操作，典型的有 AtomicReference
 * 3.开销问题：解决CAS恶性空自旋的有效方式之一是以空间换时间，较为常见的方案为：分散操作热点（LongAdder）、使用队列削峰（AQS，即抽象队列同步器）
 *
 * CAS在java.util.concurrent.atomic包中的原子类、Java AQS以及显式锁、CurrentHashMap等重要并发容器类的实现都有非常广泛的应用。
 * 在java.util.concurrent.atomic包的原子类（如AtomicXXX）中都使用了CAS来保障对数字成员进行操作的原子性。
 * java.util.concurrent的大多数类（包括显式锁、并发容器）都是基于AQS和AtomicXXX来实现的，其中AQS通过CAS保障它内部双向队列头部、尾部操作的原子性。
 */
public class LongAdderVSAtomicLongTest {
    // 每条线程的执行轮数
    final int TURNS = 100000000;

    @Test
    public void testAtomicLong() {
        // 并发任务数
        final int TASK_AMOUNT = 10;

        //线程池，获取CPU密集型任务线程池
        ExecutorService pool = ThreadUtil.getCpuIntenseTargetThreadPool();

        //定义一个原子对象
        AtomicLong atomicLong = new AtomicLong(0);

        // 线程同步倒数闩
        CountDownLatch countDownLatch = new CountDownLatch(TASK_AMOUNT);
        long start = System.currentTimeMillis();
        for (int i = 0; i < TASK_AMOUNT; i++) {
            pool.submit(() ->
            {
                try {
                    for (int j = 0; j < TURNS; j++) {
                        atomicLong.incrementAndGet();
                    }
                    // Print.tcfo("本线程累加完成");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //倒数闩，倒数一次
                countDownLatch.countDown();

            });
        }

        try {
            //等待倒数闩完成所有的倒数操作
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        float time = (System.currentTimeMillis() - start) / 1000F;
        //输出统计结果
        Print.tcfo("运行的时长为：" + time);
        Print.tcfo("累加结果为：" + atomicLong.get());
    }

    @Test
    public void testLongAdder() {
        // 并发任务数
        final int TASK_AMOUNT = 10;

        //线程池，获取CPU密集型任务线程池
        ExecutorService pool = ThreadUtil.getCpuIntenseTargetThreadPool();

        //定义一个LongAdder 对象
        LongAdder longAdder = new LongAdder();
        // 线程同步倒数闩
        CountDownLatch countDownLatch = new CountDownLatch(TASK_AMOUNT);
        long start = System.currentTimeMillis();
        for (int i = 0; i < TASK_AMOUNT; i++) {
            pool.submit(() ->
            {
                try {
                    for (int j = 0; j < TURNS; j++) {
                        longAdder.add(1);
                    }
                    // Print.tcfo("本线程累加完成");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //倒数闩，倒数一次
                countDownLatch.countDown();

            });
        }

        try {
            //等待倒数闩完成所有的倒数操作
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        float time = (System.currentTimeMillis() - start) / 1000F;
        //输出统计结果
        Print.tcfo("运行的时长为：" + time);
        Print.tcfo("累加结果为：" + longAdder.longValue());
    }
}

