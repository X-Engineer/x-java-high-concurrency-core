package com.crazymakercircle.innerlock;

import com.crazymakercircle.util.Print;
import org.junit.Test;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

import java.util.concurrent.CountDownLatch;

import static com.crazymakercircle.util.ThreadUtil.sleepMilliSeconds;

/**
 * Created by 尼恩@疯狂创客圈.
 * 总结一下synchronized的执行过程，大致如下：
 * （1）线程抢锁时，JVM首先检测内置锁对象Mark Word中的biased_lock（偏向锁标识）是否设置成1，lock（锁标志位）是否为01，如果都满足，确认内置锁对象为可偏向状态。
 * （2）在内置锁对象确认为可偏向状态之后，JVM检查Mark Word中的线程ID是否为抢锁线程ID，如果是，就表示抢锁线程处于偏向锁状态，抢锁线程快速获得锁，开始执行临界区代码。
 * （3）如果Mark Word中的线程ID并未指向抢锁线程，就通过CAS操作竞争锁。如果竞争成功，就将Mark Word中的线程ID设置为抢锁线程，偏向标志位设置为1，锁标志位设置为01，然后执行临界区代码，此时内置锁对象处于偏向锁状态。
 * （4）如果CAS操作竞争失败，就说明发生了竞争，撤销偏向锁，进而升级为轻量级锁。
 * （5）JVM使用CAS将锁对象的Mark Word替换为抢锁线程的锁记录指针，如果成功，抢锁线程就获得锁。如果替换失败，就表示其他线程竞争锁，JVM尝试使用CAS自旋替换抢锁线程的锁记录指针，如果自旋成功（抢锁成功），那么锁对象依然处于轻量级锁状态。
 * （6）如果JVM的CAS替换锁记录指针自旋失败，轻量级锁就膨胀为重量级锁，后面等待锁的线程也要进入阻塞状态。
 * 总体来说，偏向锁是在没有发生锁争用的情况下使用的；一旦有了第二个线程争用锁，偏向锁就会升级为轻量级锁；如果锁争用很激烈，轻量级锁的CAS自旋到达阈值后，轻量级锁就会升级为重量级锁。
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
     * 偏向锁主要解决无竞争下的锁性能问题，所谓的偏向就是偏心，即锁会偏向于当前已经占有锁的进程。
     * 在实际场景中，如果一个同步块（或方法）没有多个线程竞争，而且总是由同一个线程多次重入获取锁，如果每次还有阻塞线程，唤醒CPU从用户态转为核心态，那么对于CPU是一种资源的浪费，为了解决这类问题，就引入了偏向锁的概念。
     *
     * 偏向锁的核心原理是：如果不存在线程竞争的一个线程获得了锁，那么锁就进入偏向状态，此时Mark Word的结构变为偏向锁结构，锁对象的锁标志位（lock）被改为01，偏向标志位（biased_lock）被改为1，
     * 然后线程的ID记录在锁对象的Mark Word中（使用CAS操作完成）。以后该线程获取锁时判断一下线程ID和标志位，就可以直接进入同步块，连CAS操作都不需要，这样就省去了大量有关锁申请的操作，从而也就提升了程序的性能。
     *
     * 假如有多个线程来竞争偏向锁，此对象锁已经有所偏向，其他的线程发现偏向锁并不是偏向自己，就说明存在了竞争，尝试撤销偏向锁（很可能引入安全点），然后膨胀到轻量级锁。
     * 1.偏向锁的撤销
     * 偏向锁撤销的开销花费还是挺大的，其大概过程如下：
     * （1）在一个安全点停止拥有锁的线程。
     * （2）遍历线程的栈帧，检查是否存在锁记录。如果存在锁记录，就需要清空锁记录，使其变成无锁状态，并修复锁记录指向的Mark Word，清除其线程ID。
     * （3）将当前锁升级成轻量级锁。
     * （4）唤醒当前线程。
     * 所以，如果某些临界区存在两个及两个以上的线程竞争，那么偏向锁反而会降低性能。在这种情况下，可以在启动JVM时就把偏向锁的默认功能关闭。
     * 2.偏向锁的膨胀
     * 如果偏向锁被占据，一旦有第二个线程争抢这个对象，因为偏向锁不会主动释放，所以第二个线程可以看到内置锁偏向状态，这时表明在这个对象锁上已经存在竞争了。JVM检查原来持有该对象锁的占有线程是否依然存活，如果挂了，就可以将对象变为无锁状态，然后进行重新偏向，偏向为抢锁线程。
     * 如果JVM检查到原来的线程依然存活，就进一步检查占有线程的调用堆栈是否通过锁记录持有偏向锁。如果存在锁记录，就表明原来的线程还在使用偏向锁，发生锁竞争，撤销原来的偏向锁，将偏向锁膨胀（INFLATING）为轻量级锁。
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

    /**
     * 轻量级锁的演示案例
     *
     * 轻量级锁主要有两种：普通自旋锁和自适应自旋锁。
     * 轻量级锁也被称为非阻塞同步、乐观锁，因为这个过程并没有把线程阻塞挂起，而是让线程空循环等待。
     * - 所谓普通自旋锁，就是指当有线程来竞争锁时，抢锁线程会在原地循环等待，而不是被阻塞，直到那个占有锁的线程释放锁之后，这个抢锁线程才可以获得锁。
     * - 所谓自适应自旋锁，就是等待线程空循环的自旋次数并非是固定的，而是会动态地根据实际情况来改变自旋等待的次数，自旋次数由前一次在同一个锁上的自旋时间及锁的拥有者的状态来决定，总的思想是：根据上一次自旋的时间与结果调整下一次自旋的时间。
     *
     * 轻量级锁的问题在哪里呢？虽然大部分临界区代码的执行时间都是很短的，但是也会存在执行得很慢的临界区代码。临界区代码执行耗时较长，在其执行期间，其他线程都在原地自旋等待，会空消耗CPU。因此，如果竞争这个同步锁的线程很多，就会有多个线程在原地等待继续空循环消耗CPU（空自旋），这会带来很大的性能损耗。
     * 轻量级锁的本意是为了减少多线程进入操作系统底层的互斥锁（Mutex Lock）的概率，并不是要替代操作系统互斥锁。所以，在争用激烈的场景下，轻量级锁会膨胀为基于操作系统内核互斥锁实现的重量级锁。
     * @throws InterruptedException
     *
     * - 程序启动运行5秒之后，ObjectLock实例的锁状态为偏向锁
     * - 现在执行第一个抢锁线程，在抢占完成之后，ObjectLock实例的锁状态还是为偏向锁，只不过ObjectLock实例的Mark Word记录了第一个抢占线程的ID
     * - 接着开始第二个抢锁线程，在第二个线程抢锁成功之后，ObjectLock实例的锁状态为轻量级锁，ObjectLock实例Mark Word的lock标记位改为00（轻量级锁标志），其ptr_to_lock_record（锁记录指针）更新为抢锁线程栈帧中锁记录的地址，此时的锁为轻量级锁
     * - 轻量级锁被释放之后，ObjectLock实例变成无锁状态，其lock标记位改为01（无锁标志）
     *
     * [main|InnerLockTest.showLightweightLock]：抢占锁前, lock 的状态:
     * [ObjectLock.printObjectStruct]：lock = com.crazymakercircle.innerlock.ObjectLock object internals:
     *  OFFSET  SIZE                TYPE DESCRIPTION                               VALUE
     *       0     4                     (object header)                           05 00 00 00 (00000101 00000000 00000000 00000000) (5)
     *       4     4                     (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
     *       8     4                     (object header)                           5b 0f 01 f8 (01011011 00001111 00000001 11111000) (-134148261)
     *      12     4   java.lang.Integer ObjectLock.amount                         0
     * Instance size: 16 bytes
     * Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
     *
     * [Thread-0|InnerLockTest.lambda$showLightweightLock$1]：第一个线程占有锁, lock 的状态:
     * [ObjectLock.printObjectStruct]：lock = com.crazymakercircle.innerlock.ObjectLock object internals:
     *  OFFSET  SIZE                TYPE DESCRIPTION                               VALUE
     *       0     4                     (object header)                           05 e8 8b 2c (00000101 11101000 10001011 00101100) (747366405)
     *       4     4                     (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
     *       8     4                     (object header)                           5b 0f 01 f8 (01011011 00001111 00000001 11111000) (-134148261)
     *      12     4   java.lang.Integer ObjectLock.amount                         2
     * Instance size: 16 bytes
     * Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
     *
     * [Thread-1|InnerLockTest.lambda$showLightweightLock$2]：第二个线程占有锁, lock 的状态:
     * [ObjectLock.printObjectStruct]：lock = com.crazymakercircle.innerlock.ObjectLock object internals:
     *  OFFSET  SIZE                TYPE DESCRIPTION                               VALUE
     *       0     4                     (object header)                           98 ef ed 30 (10011000 11101111 11101101 00110000) (820899736)
     *       4     4                     (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
     *       8     4                     (object header)                           5b 0f 01 f8 (01011011 00001111 00000001 11111000) (-134148261)
     *      12     4   java.lang.Integer ObjectLock.amount                         1501
     * Instance size: 16 bytes
     * Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
     *
     * [main|InnerLockTest.showLightweightLock]：释放锁后, lock 的状态:
     * [ObjectLock.printObjectStruct]：lock = com.crazymakercircle.innerlock.ObjectLock object internals:
     *  OFFSET  SIZE                TYPE DESCRIPTION                               VALUE
     *       0     4                     (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
     *       4     4                     (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
     *       8     4                     (object header)                           5b 0f 01 f8 (01011011 00001111 00000001 11111000) (-134148261)
     *      12     4   java.lang.Integer ObjectLock.amount                         2000
     * Instance size: 16 bytes
     * Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
     */
    @Test
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

    /**
     * 在JVM中，每个对象都关联一个监视器，这里的对象包含Object实例和Class实例。监视器是一个同步工具，相当于一个许可证，拿到许可证的线程即可进入临界区进行操作，没有拿到则需要阻塞等待。重量级锁通过监视器的方式保障了任何时间只允许一个线程通过受到监视器保护的临界区代码。
     * VM中每个对象都会有一个监视器，监视器和对象一起创建、销毁。监视器相当于一个用来监视这些线程进入的特殊房间，其义务是保证（同一时间）只有一个线程可以访问被保护的临界区代码块。
     * 本质上，监视器是一种同步工具，也可以说是一种同步机制，主要特点是：
     * （1）同步。监视器所保护的临界区代码是互斥地执行的。一个监视器是一个运行许可，任一线程进入临界区代码都需要获得这个许可，离开时把许可归还。
     * （2）协作。监视器提供Signal机制，允许正持有许可的线程暂时放弃许可进入阻塞等待状态，等待其他线程发送Signal去唤醒；其他拥有许可的线程可以发送Signal，唤醒正在阻塞等待的线程，让它可以重新获得许可并启动执行。
     *
     * 在Hotspot虚拟机中，监视器是由C++类ObjectMonitor实现的。
     * ObjectMonitor的Owner（_owner）、WaitSet（_WaitSet）、Cxq（_cxq）、EntryList（_EntryList）这几个属性比较关键。
     * ObjectMonitor的WaitSet、Cxq、EntryList这三个队列存放抢夺重量级锁的线程，而ObjectMonitor的Owner所指向的线程即为获得锁的线程。
     * Cxq、EntryList、WaitSet这三个队列的说明如下：
     * （1）Cxq：竞争队列（Contention Queue），所有请求锁的线程首先被放在这个竞争队列中。
     * （2）EntryList：Cxq中那些有资格成为候选资源的线程被移动到EntryList中。
     * （3）WaitSet：某个拥有ObjectMonitor的线程在调用Object.wait()方法之后将被阻塞，然后该线程将被放置在WaitSet链表中。
     *
     * 重量级锁的演示案例
     * 在程序启动运行5秒之后，ObjectLock的锁状态为偏向锁，在程序运行的第二个阶段有一个线程占有锁，此时的ObjectLock实例的锁状态仍然为偏向锁
     * 在程序运行的第三个阶段开启了两个线程去抢占锁：
     * 此时ObjectLock实例的锁状态已经膨胀为轻量级锁，其lock标记为00。
     * 第二个抢锁线程比第一个抢锁线程晚启动100毫秒，此时ObjectLock实例的锁状态已经从轻量级锁膨胀为重量级锁，其lock标记为10，说明此时存在激烈的锁争用
     *
     *
     * [main|InnerLockTest.showHeavyweightLock]：抢占锁前, counter 的状态:
     * [main] INFO com.crazymakercircle.threadpool.SeqOrScheduledTargetThreadPoolLazyHolder - 线程池已经初始化
     * [ObjectLock.printObjectStruct]：lock = com.crazymakercircle.innerlock.ObjectLock object internals:
     *  OFFSET  SIZE                TYPE DESCRIPTION                               VALUE
     *       0     4                     (object header)                           05 00 00 00 (00000101 00000000 00000000 00000000) (5)
     *       4     4                     (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
     *       8     4                     (object header)                           5b 0f 01 f8 (01011011 00001111 00000001 11111000) (-134148261)
     *      12     4   java.lang.Integer ObjectLock.amount                         0
     * Instance size: 16 bytes
     * Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
     *
     * [Thread-0|InnerLockTest.lambda$showHeavyweightLock$3]：第一个线程占有锁, counter 的状态:
     * [ObjectLock.printObjectStruct]：lock = com.crazymakercircle.innerlock.ObjectLock object internals:
     *  OFFSET  SIZE                TYPE DESCRIPTION                               VALUE
     *       0     4                     (object header)                           05 38 c0 2f (00000101 00111000 11000000 00101111) (801126405)
     *       4     4                     (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
     *       8     4                     (object header)                           5b 0f 01 f8 (01011011 00001111 00000001 11111000) (-134148261)
     *      12     4   java.lang.Integer ObjectLock.amount                         1
     * Instance size: 16 bytes
     * Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
     *
     * [抢锁线程1|InnerLockTest.lambda$showHeavyweightLock$4]：占有锁, counter 的状态:
     * [ObjectLock.printObjectStruct]：lock = com.crazymakercircle.innerlock.ObjectLock object internals:
     *  OFFSET  SIZE                TYPE DESCRIPTION                               VALUE
     *       0     4                     (object header)                           88 ed dd 30 (10001000 11101101 11011101 00110000) (819850632)
     *       4     4                     (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
     *       8     4                     (object header)                           5b 0f 01 f8 (01011011 00001111 00000001 11111000) (-134148261)
     *      12     4   java.lang.Integer ObjectLock.amount                         1001
     * Instance size: 16 bytes
     * Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
     *
     * [抢锁线程2|InnerLockTest.lambda$showHeavyweightLock$4]：占有锁, counter 的状态:
     * [ObjectLock.printObjectStruct]：lock = com.crazymakercircle.innerlock.ObjectLock object internals:
     *  OFFSET  SIZE                TYPE DESCRIPTION                               VALUE
     *       0     4                     (object header)                           ca 84 c8 2a (11001010 10000100 11001000 00101010) (717784266)
     *       4     4                     (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
     *       8     4                     (object header)                           5b 0f 01 f8 (01011011 00001111 00000001 11111000) (-134148261)
     *      12     4   java.lang.Integer ObjectLock.amount                         1079
     * Instance size: 16 bytes
     * Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
     *
     * [main|InnerLockTest.showHeavyweightLock]：释放锁后, counter 的状态:
     * [ObjectLock.printObjectStruct]：lock = com.crazymakercircle.innerlock.ObjectLock object internals:
     *  OFFSET  SIZE                TYPE DESCRIPTION                               VALUE
     *       0     4                     (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
     *       4     4                     (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
     *       8     4                     (object header)                           5b 0f 01 f8 (01011011 00001111 00000001 11111000) (-134148261)
     *      12     4   java.lang.Integer ObjectLock.amount                         3000
     * Instance size: 16 bytes
     * Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
     * @throws InterruptedException
     */
    @Test
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

