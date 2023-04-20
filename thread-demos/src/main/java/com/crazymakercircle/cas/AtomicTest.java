package com.crazymakercircle.cas;

import com.crazymakercircle.im.common.bean.User;
import com.crazymakercircle.util.Print;
import com.crazymakercircle.util.ThreadUtil;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

import static com.crazymakercircle.util.ThreadUtil.sleepMilliSeconds;

/**
 * Atomic操作翻译成中文是指一个不可中断的操作，即使在多个线程一起执行Atomic类型操作的时候，一个操作一旦开始，就不会被其他线程中断。所谓Atomic类，指的是具有原子操作特征的类。
 * 根据操作的目标数据类型，可以将JUC包中的原子类分为4类：基本原子类、数组原子类、原子引用类和字段更新原子类。
 * - 基本原子类的功能是通过原子方式更新Java基础类型变量的值
 * - 数组原子类的功能是通过原子方式更数组中的某个元素的值
 * - 引用原子类主要包括以下三个：
 *   ● AtomicReference：引用类型原子类
 *   ● AtomicMarkableReference：带有更新标记位的原子引用类型，可以解决使用AtomicBoolean进行原子更新时可能出现的ABA问题
 *   ● AtomicStampedReference：带有更新版本号的原子引用类型，可以解决使用AtomicInteger进行原子更新时可能出现的ABA问题
 * - 字段更新原子类，主要包括：
 *    ● AtomicIntegerFieldUpdater：原子更新整型字段的更新器
 *    ● AtomicLongFieldUpdater：原子更新长整型字段的更新器
 *    ● AtomicReferenceFieldUpdater：原子更新引用类型中的字段
 *
 * 基础原子类（以AtomicInteger为例）主要通过CAS自旋+volatile的方案实现，既保障了变量操作的线程安全性，又避免了synchronized重量级锁的高开销，使得Java程序的执行效率大为提升。
 * AtomicInteger源码中的主要方法都是通过CAS自旋实现的。CAS自旋的主要操作为：如果一次CAS操作失败，获取最新的value值后，再次进行CAS操作，直到成功。
 * 另外，AtomicInteger所包装的内部value成员是一个使用关键字volatile修饰的内部成员。关键字volatile的原理比较复杂，简单地说，该关键字可以保证任何线程在任何时刻总能拿到该变量的最新值，其目的在于保障变量值的线程可见性。
 */
public class AtomicTest {

    private static final int THREAD_COUNT = 10;

    /**
     * 基础原子类AtomicInteger常用的方法如下:
     *    public final int get() //获取当前的值
     *    public final int getAndSet(int newValue)               //获取当前的值，然后设置新的值
     *    public final int getAndIncrement()                             //获取当前的值，然后自增
     *    public final int getAndDecrement()                             //获取当前的值，然后自减
     *    public final int getAndAdd(int delta)                  //获取当前的值，并加上预期的值
     *    boolean compareAndSet(int expect, int update)  //通过CAS方式设置整数值
     */
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

    /**
     * 在多线程环境下，如果涉及基本数据类型的并发操作，不建议采用synchronized重量级锁进行线程同步，而是建议优先使用基础原子类保障并发操作的线程安全性
     * @param args
     * @throws InterruptedException
     */
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

    /**
     * 数组原子类AtomicIntegerArray的使用示例
     * //获取 index=i 位置元素的值
     * public final int get(int i)
     *
     * //返回 index=i 位置当前的值，并将其设置为新值：newValue
     * public final int getAndSet(int i, int newValue)
     *
     * //获取 index=i 位置元素的值，并让该位置的元素自增
     * public final int getAndIncrement(int i)
     *
     * //获取 index=i 位置元素的值，并让该位置的元素自减
     * public final int getAndDecrement(int i)
     *
     * //获取 index=i 位置元素的值，并加上预期的值
     * public final int getAndAdd(int delta)
     *
     * //如果输入的数值等于预期值，就以原子方式将位置i的元素值设置为输入值（update）
     * boolean compareAndSet(int expect, int update)
     *
     * //最终将位置i的元素设置为newValue
     * //lazySet()方法可能导致其他线程在之后的一小段时间内还是可以读到旧的值
     * public final void lazySet(int i, int newValue)
     */
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

    /**
     * 基础的原子类型只能保证一个变量的原子操作，当需要对多个变量进行操作时，CAS无法保证原子性操作，这时可以用AtomicReference（原子引用类型）保证对象引用的原子性。
     * 简单来说，如果需要同时保障对多个变量操作的原子性，就可以把多个变量放在一个对象中进行操作。
     * <p>
     * 使用原子引用类型AtomicReference包装了User对象之后，只能保障User引用的原子操作，对被包装的User对象的字段值修改时不能保证原子性
     *
     * @throws InterruptedException
     */
    @Test
    public void testAtomicReference() throws InterruptedException {
        //包装的原子对象
        AtomicReference<User> userRef = new AtomicReference<>();
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

    /**
     * 如果需要保障对象某个字段（或者属性）更新操作的原子性，就需要用到属性更新原子类。属性更新原子类有以下三个：
     * ● AtomicIntegerFieldUpdater：保障整型字段的更新操作的原子性
     * ● AtomicLongFieldUpdater：保障长整型字段的更新操作的原子性
     * ● AtomicReferenceFieldUpdater：保障引用字段的更新操作的原子性。
     * <p>
     * 使用属性更新原子类保障属性安全更新的流程大致需要两步：
     * 第一步，更新的对象属性必须使用public volatile修饰符
     * 第二步，因为对象的属性修改类型原子类都是抽象类，所以每次使用都必须调用静态方法newUpdater()创建一个更新器，并且需要设置想要更新的类和属性。
     *
     * @throws InterruptedException
     */
    @Test
    public void testAtomicIntegerFieldUpdater() throws InterruptedException {
        AtomicIntegerFieldUpdater<User> integerFieldUpdater = AtomicIntegerFieldUpdater.newUpdater(User.class, "age");
        User user = new User("1", "张三");
        Print.tco(integerFieldUpdater.getAndIncrement(user)); // age 初始为 0，该行代码之后，age = 1
        Print.tco(integerFieldUpdater.getAndAdd(user, 100));//1
        Print.tco(integerFieldUpdater.get(user));// 101
    }

}


