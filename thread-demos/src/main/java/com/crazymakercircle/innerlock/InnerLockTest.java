package com.crazymakercircle.innerlock;

import com.crazymakercircle.util.Print;
import org.junit.Test;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

import java.util.concurrent.CountDownLatch;

import static com.crazymakercircle.util.ThreadUtil.sleepMilliSeconds;

/**
 * Created by 尼恩@疯狂创客圈.
 */
public class InnerLockTest {
    final int MAX_TREAD = 10;
    final int MAX_TURN = 1000;
    CountDownLatch latch = new CountDownLatch(MAX_TREAD);


    /**
     * JOL 输出对象布局结果：
     * [InnerLockTest.showNoLockObject]：# Running 64-bit HotSpot VM.
     * # Using compressed oop with 3-bit shift.
     * # Using compressed klass with 3-bit shift.
     * # Objects are 8 bytes aligned.
     * # Field sizes by type: 4, 1, 1, 2, 2, 4, 4, 8, 8 [bytes]
     * # Array element sizes: 4, 1, 1, 2, 2, 4, 4, 8, 8 [bytes]
     *
     * [InnerLockTest.showNoLockObject]：object status:
     * [ObjectLock.printSelf]：lock hexHash= 34 ae 76 62
     * [ObjectLock.printSelf]：lock binaryHash= 00110100 10101110 01110110 01100010
     * [ObjectLock.printSelf]：lock = com.crazymakercircle.innerlock.ObjectLock object internals:
     *  OFFSET  SIZE                TYPE DESCRIPTION                               VALUE
     *       0     4                     (object header)                           01 34 ae 76 (00000001 00110100 10101110 01110110) (1991128065)
     *       4     4                     (object header)                           62 00 00 00 (01100010 00000000 00000000 00000000) (98)
     *       8     4                     (object header)                           5b 0f 01 f8 (01011011 00001111 00000001 11111000) (-134148261)
     *      12     4   java.lang.Integer ObjectLock.amount                         0
     * Instance size: 16 bytes
     * Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
     *
     * 从运行结果来看，当前JVM的运行环境为64位虚拟机，对象以 8 字节对齐。运行结果中输出了ObjectLock的对象布局，所输出的ObjectLock对象为16字节，其中对象头（Object Header）占12字节，
     * 剩下的4字节由amount属性（字段）占用。由于16字节为8字节的倍数，因此没有对齐填充字节（JVM规定Java对象所占内存字节数必须为8的倍数，否则需要对齐填充）。
     *
     * 对象一旦生成了哈希码，JVM会将其记录在对象头的Mark Word中。当然，只有调用未重写的Object.hashcode()方法，或者调用System.IdentityHashCode(obj)方法时，其值才被记录到Mark Word中。
     * 如果调用的是重写的hashcode()方法，也不会记录到Mark Word中。
     *
     * 从以上用例的结果可以看出，对象的哈希码为“34 ae 76 62”，对象布局中的Mark Word所包含的哈希码也为“34 ae 76 62”，二者是一致的。
     * 由于Java内存中的哈希码采用的是大端模式，而JOL输出的对象布局中的哈希码采用的是小端模式，因此示例代码在输出哈希码之前先转成小端模式。
     * 34, ae, 76, 62 从左到右对应的地址是从低到高（数据的高字节保存在内存的高地址中，而数据的低字节保存在内存的低地址中）
     *
     */
    @Test
    public void showNoLockObject() throws InterruptedException {
        //输出JVM的信息
        Print.fo(VM.current().details());
        //创建一个对象
        ObjectLock objectLock = new ObjectLock();
        Print.fo("object status: ");
        /**
         * 在处理器（即CPU）的计算过程中，因为使用小端模式在数据类型转换的时候（尤其是指针转换）不用考虑地址问题，所以小端模式是处理器的主流字节存放模式。JVM所采用的字节存放模式是小端模式。
         * 由于所有网络协议都是采用大端模式来传输数据的，因此有时也会把大端模式称为“网络字节序”。当两台采用不同字节存放模式的主机通信时，在发送数据之前，都必须经过字节次序转换，转成“网络字节序”（大端模式）后再进行传输。
         */
        //输出对象的布局信息
        objectLock.printSelf();
    }

    static class  MyOrder{
        private long orderId;
        private long userId;
        private byte state;
        private long createMillis;
    }
    @org.junit.Test
    public void showObjectStructSize() throws InterruptedException {


        //输出JVM的信息
        Print.fo(VM.current().details());
        //创建一个对象
        MyOrder objectLock = new MyOrder();
        Print.fo("object status: ");
        //输出对象的布局信息

        String printable = ClassLayout.parseInstance(objectLock).toPrintable();
        //输出对象布局
        Print.fo("lock = " + printable);
    }


    /**
     * 偏向锁的演示案例
     * @throws InterruptedException
     */
    @Test
    public void showBiasedLock() throws InterruptedException {
        Print.tcfo(VM.current().details());
        //JVM延迟偏向锁
        //为什么要等待5秒呢？因为JVM在启动的时候会延迟启用偏向锁机制。JVM默认把偏向锁延迟了4000毫秒，这就解释了为什么演示案例要等待5秒才能看到对象锁的偏向状态。
        //为什么偏向锁会延迟？因为JVM在启动的时候需要加载资源，这些对象加上偏向锁没有任何意义，不启用偏向锁能减少大量偏向锁撤销的成本。
        sleepMilliSeconds(5000);// 如果注释这一行，那么抢占锁之前，lock 的状态是无锁状态，占有锁的时候，lock 的状态是轻量级锁

        ObjectLock lock = new ObjectLock();

        /**
         * 通过ObjectLock的对象结构可以发现：biased_lock（偏向锁）状态已经启用，值为1；lock（锁）状态的值为01。lock和biased_lock组合在一起为101，表明当前的ObjectLock实例处于偏向锁状态。
         * ObjectLock实例的对象头中的内容“a4 00 01 f8”为其Class Pointer（类对象指针），这里的长度为32位，是由于开启了指针压缩所导致的。从输出的结果也能看出，对oop（普通对象）、klass（类对象）指针都进行了压缩
         */
        Print.tcfo("抢占锁前, lock 的状态: ");
        lock.printObjectStruct();

        sleepMilliSeconds(5000);
        CountDownLatch latch = new CountDownLatch(1);
        Runnable runnable = () ->
        {
            for (int i = 0; i < MAX_TURN; i++) {
                synchronized (lock) {
                    lock.increase();
                    if (i == MAX_TURN / 2) {
                        Print.tcfo("占有锁, lock 的状态: ");
                        lock.printObjectStruct();
                        //读取字符串型输入,阻塞线程
//                        Print.consoleInput();
                    }
                }
                //每一次循环等待10ms
                sleepMilliSeconds(10);
            }
            latch.countDown();
        };
        new Thread(runnable, "biased-demo-thread").start();
        //等待加锁线程执行完成
        latch.await();
        Print.tcfo("释放锁后, lock 的状态: ");
        lock.printObjectStruct();


    }

    @org.junit.Test
    public void showLightweightLock() throws InterruptedException {

        Print.tcfo(VM.current().details());
        //JVM延迟偏向锁
        sleepMilliSeconds(5000);

        ObjectLock lock = new ObjectLock();

        Print.tcfo("抢占锁前, lock 的状态: ");
        lock.printObjectStruct();

        sleepMilliSeconds(5000);
        CountDownLatch latch = new CountDownLatch(2);
        Runnable runnable = () ->
        {
            for (int i = 0; i < MAX_TURN; i++) {
                synchronized (lock) {
                    lock.increase();
                    if (i == 1) {
                        Print.tcfo("第一个线程占有锁, lock 的状态: ");
                        lock.printObjectStruct();
                    }
                }

            }
            //循环完毕
            latch.countDown();

            //线程虽然释放锁，但是一直存在
            for (int j = 0; ; j++) {
                //每一次循环等待1ms
                sleepMilliSeconds(1);
            }
        };
        new Thread(runnable).start();


        sleepMilliSeconds(1000); //等待1s

        Runnable lightweightRunnable = () ->
        {
            for (int i = 0; i < MAX_TURN; i++) {
                synchronized (lock) {
                    lock.increase();
                    if (i == MAX_TURN / 2) {
                        Print.tcfo("第二个线程占有锁, lock 的状态: ");
                        lock.printObjectStruct();
                    }
                    //每一次循环等待1ms
                    sleepMilliSeconds(1);
                }
            }
            //循环完毕
            latch.countDown();
        };
        new Thread(lightweightRunnable).start();
        //等待加锁线程执行完成
        latch.await();
        sleepMilliSeconds(2000);  //等待2s
        Print.tcfo("释放锁后, lock 的状态: ");
        lock.printObjectStruct();


    }

    @org.junit.Test
    public void showHeavyweightLock() throws InterruptedException {

        Print.tcfo(VM.current().details());
        //JVM延迟偏向锁
        sleepMilliSeconds(5000);

        ObjectLock counter = new ObjectLock();

        Print.tcfo("抢占锁前, counter 的状态: ");
        counter.printObjectStruct();

        sleepMilliSeconds(5000);
        CountDownLatch latch = new CountDownLatch(3);
        Runnable runnable = () ->
        {
            for (int i = 0; i < MAX_TURN; i++) {
                synchronized (counter) {
                    counter.increase();
                    if (i == 0) {
                        Print.tcfo("第一个线程占有锁, counter 的状态: ");
                        counter.printObjectStruct();
                    }
                }

            }
            //循环完毕
            latch.countDown();

            //线程虽然释放锁，但是一直存在
            for (int j = 0; ; j++) {
                //每一次循环等待1ms
                sleepMilliSeconds(1);
            }
        };
        new Thread(runnable).start();


        sleepMilliSeconds(1000); //等待2s

        Runnable lightweightRunnable = () ->
        {
            for (int i = 0; i < MAX_TURN; i++) {
                synchronized (counter) {
                    counter.increase();
                    if (i == 0) {
                        Print.tcfo("占有锁, counter 的状态: ");
                        counter.printObjectStruct();
                    }
                    //每一次循环等待10ms
                    sleepMilliSeconds(1);
                }
            }
            //循环完毕
            latch.countDown();
        };
        new Thread(lightweightRunnable, "抢锁线程1").start();
        sleepMilliSeconds(100);  //等待2s
        new Thread(lightweightRunnable, "抢锁线程2").start();

        //等待加锁线程执行完成
        latch.await();
        sleepMilliSeconds(2000);  //等待2s
        Print.tcfo("释放锁后, counter 的状态: ");
        counter.printObjectStruct();
    }

}

