package com.crazymakercircle.cas;

import com.crazymakercircle.im.common.bean.User;
import com.crazymakercircle.util.Print;
import com.crazymakercircle.util.ThreadUtil;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.*;

import static com.crazymakercircle.util.ThreadUtil.sleepMilliSeconds;

public class AtomicTest {

    private static final int THREAD_COUNT = 10;

    @Test
    public void atomicIntegerTest() {
        int temvalue = 0;
        //定义一个整数原子类实例，赋值到变量 i
        AtomicInteger i = new AtomicInteger(0);

        //取值，然后设置一个新值
        temvalue = i.getAndSet(3);
        //输出
        Print.fo("temvalue:" + temvalue + ";  i:" + i.get());//temvalue:0;  i:3

        //取值，然后自增
        temvalue = i.getAndIncrement();
        //输出
        Print.fo("temvalue:" + temvalue + ";  i:" + i.get());//temvalue:3;  i:4

        //取值，然后增加5
        temvalue = i.getAndAdd(5);
        //输出
        Print.fo("temvalue:" + temvalue + ";  i:" + i.get());//temvalue:4;  i:9

        //CAS交换
        boolean flag = i.compareAndSet(9, 100);
        //输出
        Print.fo("flag:" + flag + ";  i:" + i.get());//flag:true;  i:100
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        //定义一个整数原子类实例，赋值到变量 i
        AtomicInteger atomicInteger = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            // 创建10个线程,模拟多线程环境
            ThreadUtil.getMixedTargetThreadPool().submit(() ->
            {

                for (int j = 0; j < 1000; j++) {
                    atomicInteger.getAndIncrement();
                }
                latch.countDown();

            });
        }
        latch.await();
        Print.tco("累加之和：" + atomicInteger.get());
    }

    @Test
    public void testAtomicIntegerArray() {
        int tempvalue = 0;
        //建立原始的数组
        int[] array = {1, 2, 3, 4, 5, 6};

        //包装为原子数组
        AtomicIntegerArray i = new AtomicIntegerArray(array);
        //获取第0个元素，然后设置为2
        tempvalue = i.getAndSet(0, 2);
        //输出  tempvalue:1;  i:[2, 2, 3, 4, 5, 6]
        Print.fo("tempvalue:" + tempvalue + ";  i:" + i);

        //获取第0个元素，然后自增
        tempvalue = i.getAndIncrement(0);
        //输出  tempvalue:2;  i:[3, 2, 3, 4, 5, 6]
        Print.fo("tempvalue:" + tempvalue + ";  i:" + i);

        //获取第0个元素，然后增加一个delta 5
        tempvalue = i.getAndAdd(0, 5);
        //输出  tempvalue:3;  i:[8, 2, 3, 4, 5, 6]
        Print.fo("tempvalue:" + tempvalue + ";  i:" + i);
    }

    @Test
    public void testAtomicStampedReference() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(2);

        AtomicStampedReference<Integer> atomicStampedRef =
                new AtomicStampedReference<Integer>(1, 0);

        ThreadUtil.getMixedTargetThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                int stamp = atomicStampedRef.getStamp();
                Print.tco("before sleep 500: value=" + atomicStampedRef.getReference()
                        + " stamp=" + atomicStampedRef.getStamp());

                //等待500毫秒
                sleepMilliSeconds(500);
                success = atomicStampedRef.compareAndSet(1, 10,
                        stamp, stamp + 1);

                Print.tco("after sleep 500 cas 1: success=" + success
                        + " value=" + atomicStampedRef.getReference()
                        + " stamp=" + atomicStampedRef.getStamp());


                //增加标记值
                stamp++;
                success = atomicStampedRef.compareAndSet(10, 1,
                        stamp, stamp + 1);
                Print.tco("after  sleep 500 cas 2: success=" + success
                        + " value=" + atomicStampedRef.getReference()
                        + " stamp=" + atomicStampedRef.getStamp());

                latch.countDown();
            }
        });

        ThreadUtil.getMixedTargetThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                int stamp = atomicStampedRef.getStamp();
                // stamp = 0
                Print.tco("before sleep 1000: value=" + atomicStampedRef.getReference()
                        + " stamp=" + atomicStampedRef.getStamp());

                //等待1000毫秒
                sleepMilliSeconds(1000);
                //stamp = 1
                Print.tco("after sleep 1000: stamp = " + atomicStampedRef.getStamp());
                success = atomicStampedRef.compareAndSet(1, 20, stamp, stamp++);
                Print.tco("after cas 3 1000: success=" + success
                        + " value=" + atomicStampedRef.getReference()
                        + " stamp=" + atomicStampedRef.getStamp());
                latch.countDown();
            }
        });
        latch.await();

    }

    @Test
    public void testAtomicMarkableReference() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(2);

        AtomicMarkableReference<Integer> atomicRef =
                new AtomicMarkableReference<Integer>(1, false);

        ThreadUtil.getMixedTargetThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                int value = atomicRef.getReference();
                boolean mark = getMark(atomicRef);
                Print.tco("before sleep 500: value=" + value
                        + " mark=" + mark);

                //等待500毫秒
                sleepMilliSeconds(500);
                success = atomicRef.compareAndSet(1, 10,
                        mark, !mark);

                Print.tco("after sleep 500 cas 1: success=" + success
                        + " value=" + atomicRef.getReference()
                        + " mark=" + getMark(atomicRef));


                latch.countDown();
            }
        });

        ThreadUtil.getMixedTargetThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                int value = atomicRef.getReference();
                boolean mark = getMark(atomicRef);
                Print.tco("before sleep 1000: value=" + atomicRef.getReference()
                        + " mark=" + mark);

                //等待1000毫秒
                sleepMilliSeconds(1000);
                Print.tco("after sleep 1000: mark = " + getMark(atomicRef));
                success = atomicRef.compareAndSet(1, 20, mark, !mark);
                Print.tco("after cas 3 1000: success=" + success
                        + " value=" + atomicRef.getReference()
                        + " mark=" + getMark(atomicRef));
                latch.countDown();
            }
        });
        latch.await();

    }

    private boolean getMark(AtomicMarkableReference<Integer> atomicRef) {
        boolean[] markHolder = {false};
        int value = atomicRef.get(markHolder);
        return markHolder[0];
    }

    @Test
    public void testAtomicReference() throws InterruptedException {
        //包装的原子对象
        AtomicReference<User> userRef = new AtomicReference<User>();
        //待包装的User对象
        User user = new User("1", "张三");
        //为原子对象设置值
        userRef.set(user);
        Print.tco("userRef is:" + userRef.get());

        //要使用CAS替换的User对象
        User updateUser = new User("2", "李四");
        //使用CAS替换
        boolean success = userRef.compareAndSet(user, updateUser);
        Print.tco(" cas result is:" + success);
        Print.tco(" after cas,userRef is:" + userRef.get());
    }

    @Test
    public void testAtomicIntegerFieldUpdater() throws InterruptedException {
        AtomicIntegerFieldUpdater<User> a =
                AtomicIntegerFieldUpdater.newUpdater(User.class, "age");

        User user = new User("1", "张三");
        Print.tco(a.getAndIncrement(user));// 1
        Print.tco(a.getAndAdd(user, 100));// 101
        Print.tco(a.get(user));// 101
    }

}


