package com.crazymakercircle.thread;

import com.lmax.disruptor.Sequence;
import lombok.extern.slf4j.Slf4j;
import net.openhft.affinity.AffinityThreadFactory;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.methodType;

@Slf4j
public class ThreadSleep {

    private static final MethodHandle ON_SPIN_WAIT_METHOD_HANDLE;

    static {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        MethodHandle methodHandle = null;
        try {
            methodHandle = lookup.findStatic(Thread.class, "onSpinWait", methodType(void.class));
        } catch (final Exception ignore) {
        }

        ON_SPIN_WAIT_METHOD_HANDLE = methodHandle;
    }


    @Test
    public void testThreadSleep() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " 开始执行");
            try {
                TimeUnit.SECONDS.sleep(1);// 其内部也是调用的Thread.sleep实现的
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " 执行完毕");
        }).start();

        countDownLatch.await();

    }

    @Test
    public void testOnSpinWait() throws InterruptedException {


        long LIMIT = 1_000_000_000;
        Sequence[] a = {new Sequence(0), new Sequence(0), new Sequence(0), new Sequence(0)};
        CountDownLatch countDownLatch = new CountDownLatch(4);

        AffinityThreadFactory affinityThreadFactory = new AffinityThreadFactory("affinityWorker");

        ExecutorService affinityPool = Executors.newFixedThreadPool(4,
                affinityThreadFactory);
        affinityPool.submit(() -> {
            long begin = System.currentTimeMillis();
            System.out.println(" Thread.yield() 开始执行");
            while (a[0].incrementAndGet() < LIMIT) {
//            while (a[0].get() < LIMIT) {
//                a[0].set(a[0].get() + 1);
                Thread.yield();

            }
            System.out.println(" Thread.yield() 执行完毕"+(System.currentTimeMillis() - begin));

            countDownLatch.countDown();

        });

        affinityPool.submit(() -> {
            long begin = System.currentTimeMillis();
            System.out.println(" Thread.sleep() 开始执行");
            while (a[1].incrementAndGet() < LIMIT) {
//            while (a[1].get() < LIMIT) {
//                a[1].set(a[1].get() + 1);
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println( " Thread.sleep(0) 执行完毕  " + (System.currentTimeMillis() - begin));

            countDownLatch.countDown();

        });


        affinityPool.submit(() -> {
            long begin = System.currentTimeMillis();
            System.out.println( " ON_SPIN_WAIT_METHOD_HANDLE 空自旋  开始执行");
            while (a[2].incrementAndGet() < LIMIT) {
//
//            while (a[2].get() < LIMIT) {
//                a[2].set(a[2].get() + 1);
//                Thread.onSpinWait();

                // Call java.lang.Thread.onSpinWait() on Java SE versions that support it. Do nothing otherwise.
                // This should optimize away to either nothing or to an inlining of java.lang.Thread.onSpinWait()
                if (null != ON_SPIN_WAIT_METHOD_HANDLE) {
                    try {
                        ON_SPIN_WAIT_METHOD_HANDLE.invokeExact();
                    } catch (final Throwable ignore) {
                    }
                }
            }
            System.out.println( " ON_SPIN_WAIT_METHOD_HANDLE空自旋  执行完毕 " + (System.currentTimeMillis() - begin));

            countDownLatch.countDown();

        });

        affinityPool.submit(() -> {
            long begin = System.currentTimeMillis();
            System.out.println( " 空自旋 开始执行");
            while (a[3].incrementAndGet() < LIMIT) {
//                        while (a[3].get() < LIMIT) {
//                a[3].set(a[3].get() + 1);

                // 空自旋
            }

            System.out.println("空自旋 执行完毕 " + (System.currentTimeMillis() - begin));
            countDownLatch.countDown();

        });

        countDownLatch.await();

        affinityPool.shutdown();
        affinityPool.awaitTermination(1, TimeUnit.SECONDS);

    }

    /**
     * An      * {@link java.util.concurrent.atomic.AtomicInteger}* demo
     */
    static class AtomicInteger2 {
        private static final long serialVersionUID = 6214790243416807050L;

        // setup to use Unsafe.compareAndSwapInt for updates
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        private static final long valueOffset;

        static {
            try {
                valueOffset = unsafe.objectFieldOffset
                        (java.util.concurrent.atomic.AtomicInteger.class.getDeclaredField("value"));
            } catch (Exception ex) {
                throw new Error(ex);
            }
        }

        private volatile int value;

        /**
         * Creates a new AtomicInteger with the given initial value.
         *
         * @param initialValue the initial value
         */
        public AtomicInteger2(int initialValue) {
            value = initialValue;
        }

        public final boolean compareAndSet(int expect, int update) {
            return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
        }

        /**
         * Atomically increments by one the current value.
         *
         * @return the previous value
         */
        public final int getAndIncrement() {
            return unsafe.getAndAddInt(this, valueOffset, 1);
        }

        public final int getAndAddInt() {
            int loop = 1;
            int oldValue;
            do {
                loop++;
                oldValue = unsafe.getIntVolatile(this, valueOffset);
                if (loop > 10) {
                    //                Thread.onSpinWait();
                    if (null != ON_SPIN_WAIT_METHOD_HANDLE) {
                        try {
                            ON_SPIN_WAIT_METHOD_HANDLE.invokeExact();
                        } catch (final Throwable ignore) {
                        }
                    }
                }
                if(loop > 100)
                {
                    return oldValue;
                }
            } while (!unsafe.compareAndSwapInt(this, valueOffset, oldValue, oldValue + 1));
            return oldValue;
        }
    }
}
