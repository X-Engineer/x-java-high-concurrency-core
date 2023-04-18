package com.crazymakercircle.cas;

import com.crazymakercircle.util.JvmUtil;
import com.crazymakercircle.util.Print;
import com.crazymakercircle.util.ThreadUtil;
import org.junit.Test;
import org.openjdk.jol.info.ClassLayout;
import sun.misc.Unsafe;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 这里使用CAS无锁编程算法实现一个轻量级的安全自增实现版本：
 * 总计10个线程并行运行，每个线程通过CAS自旋对一个共享数据进行自增运算，并且每个线程需要成功自增运算1000次。
 *
 * 从输出结果可以看出，调用Unsafe.objectFieldOffset(…)方法所获取到的value属性的偏移量为12。
 * 其实通过 JOL 输出的结果可以看出，一个TestCompareAndSwap对象的Object Header占用了12字节，
 * 而value属性的内存位置紧挨在Object Header之后，所以value属性的相对偏移量值为12。
 */
public class TestCompareAndSwap {
    // 模拟CAS 算法
    static class OptimisticLockingPlus {
        private static final int THREAD_COUNT = 10;

        //值
        volatile private int value;

        //不安全类
        private static final Unsafe unsafe = JvmUtil.getUnsafe();

        //value 的内存偏移（相对于对象头）
        private static final long valueOffset;

        private static final AtomicLong failure = new AtomicLong(0);

        static {
            try {
                //取得内存偏移
                valueOffset = unsafe.objectFieldOffset(
                        OptimisticLockingPlus.class.getDeclaredField("value"));

                Print.tco("valueOffset:=" + valueOffset);
            } catch (Exception ex) {
                throw new Error(ex);
            }
        }

        public final boolean unSafeCompareAndSet(int oldValue, int newValue) {
            return unsafe.compareAndSwapInt(this, valueOffset, oldValue, newValue);
        }


        // 无锁编程：安全的自增方法
        public void selfPlus() {
            // 获取旧值
            int oldValue = value;
            int i = 0;
            //如果操作失败则自旋，一直到操作成功
            do {
                oldValue = value;
                //统计无效的自旋次数
                if (i++ > 1) {
                    failure.incrementAndGet();
                }

            } while (!unSafeCompareAndSet(oldValue, oldValue + 1));
        }


        public static void main(String[] args) throws InterruptedException {
            final OptimisticLockingPlus cas = new OptimisticLockingPlus();
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
            for (int i = 0; i < THREAD_COUNT; i++) {
                // 创建10个线程,模拟多线程环境
                ThreadUtil.getMixedTargetThreadPool().submit(() ->
                {

                    for (int j = 0; j < 1000; j++) {
                        cas.selfPlus();
                    }
                    latch.countDown();

                });
            }
            latch.await();
            Print.tco("累加之和：" + cas.value);
            Print.tco("失败次数：" + cas.failure.get());
        }
    }

    @Test
    public void printObjectStruct() {
        //创建一个对象
        OptimisticLockingPlus object = new OptimisticLockingPlus();
        //给成员赋值
        object.value = 100;
        //通过JOL工具输出内存布局
        String printable = ClassLayout.parseInstance(object).toPrintable();
        Print.fo("object = " + printable);
    }

}
