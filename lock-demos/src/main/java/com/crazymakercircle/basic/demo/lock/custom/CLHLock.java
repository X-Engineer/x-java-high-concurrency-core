package com.crazymakercircle.basic.demo.lock.custom;

import com.crazymakercircle.util.Print;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class CLHLock implements Lock
{

    static private class QNode
    {
        /**
         * true表示该线程需要获取锁，且需占用锁
         * 为false表示线程释放了锁，且不占用锁
         */
        private volatile boolean locked = false;

        //测试专用：节点编号
        static AtomicInteger nodeSum = new AtomicInteger(0);
        int nodeNO;

        public QNode()
        {
            nodeNO = nodeSum.incrementAndGet();
        }
    }

    /**
     * 锁等待队列的尾部
     */
    private AtomicReference<QNode> tail = new AtomicReference<>(null);
    /**
     * 指向前驱节点
     */
    private static ThreadLocal<QNode> preNodeLocal = ThreadLocal.withInitial(() -> null);

    /**
     * 指向当前节点
     */
    private static ThreadLocal<QNode> curNodeLocal = ThreadLocal.withInitial(QNode::new);

    public CLHLock()
    {
    }

    @Override
    public void lock()
    {
        //获取线程局部变量中的当前节点
        QNode curNode = this.curNodeLocal.get();
        if (null == curNode)
        {
            curNode = new QNode();
            this.curNodeLocal.set(curNode);
        }
        Print.tcfo("节点编号为：" + curNode.nodeNO);
        //设置自己的状态为locked=true表示需要获取锁
        curNode.locked = true;

        //1 取出链表尾部的之前的节点 preNode
        //2 链表的尾部设置为本线程的 curNode
        QNode preNode = tail.getAndSet(curNode);

        //把前驱节点保持在线程本地变量
        preNodeLocal.set(preNode);

        if (preNode != null)
        {
            //在前驱节点的locked字段上自旋，直到前驱节点释放锁资源
            while (preNode.locked)
            {
                //不让出CPU时间片，性能比较低
                Thread.yield();
            }
        }
    }

    @Override
    public void unlock()
    {
        QNode curNode = curNodeLocal.get();

        //释放锁操作时将自己的locked设置为false
        // 是的后继节点可以结束自旋，抢占到锁
        curNode.locked = false;

        //回收自己这个节点，从虚拟队列中删除
        curNodeLocal.set(null);
    }


    /**
     * Acquires the lock unless the current thread is
     * {@linkplain Thread#interrupt interrupted}.
     *
     * <p>Acquires the lock if it is available and returns immediately.
     *
     * <p>If the lock is not available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until
     * one of two things happens:
     *
     * <ul>
     * <li>The lock is acquired by the current thread; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
     * current thread, and interruption of lock acquisition is supported.
     * </ul>
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while acquiring the
     * lock, and interruption of lock acquisition is supported,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>The ability to interrupt a lock acquisition in some
     * implementations may not be possible, and if possible may be an
     * expensive operation.  The programmer should be aware that this
     * may be the case. An implementation should document when this is
     * the case.
     *
     * <p>An implementation can favor responding to an interrupt over
     * normal method return.
     *
     * <p>A {@code Lock} implementation may be able to detect
     * erroneous use of the lock, such as an invocation that would
     * cause deadlock, and may throw an (unchecked) exception in such
     * circumstances.  The circumstances and the exception type must
     * be documented by that {@code Lock} implementation.
     *
     * @throws InterruptedException if the current thread is
     *                              interrupted while acquiring the lock (and interruption
     *                              of lock acquisition is supported)
     */
    @Override
    public void lockInterruptibly() throws InterruptedException
    {
        throw new IllegalStateException(
                "方法 'lockInterruptibly' 尚未实现!");
    }

    /**
     * Acquires the lock only if it is free at the time of invocation.
     *
     * <p>Acquires the lock if it is available and returns immediately
     * with the value {@code true}.
     * If the lock is not available then this method will return
     * immediately with the value {@code false}.
     *
     * <p>A typical usage idiom for this method would be:
     * <pre> {@code
     * Lock lock = ...;
     * if (lock.tryLock()) {
     *   try {
     *     // manipulate protected state
     *   } finally {
     *     lock.unlock();
     *   }
     * } else {
     *   // perform alternative actions
     * }}</pre>
     * <p>
     * This usage ensures that the lock is unlocked if it was acquired, and
     * doesn't try to unlock if the lock was not acquired.
     *
     * @return {@code true} if the lock was acquired and
     * {@code false} otherwise
     */
    @Override
    public boolean tryLock()
    {
        throw new IllegalStateException(
                "方法 'tryLock' 尚未实现!");

    }

    /**
     * Acquires the lock if it is free within the given waiting time and the
     * current thread has not been {@linkplain Thread#interrupt interrupted}.
     *
     * <p>If the lock is available this method returns immediately
     * with the value {@code true}.
     * If the lock is not available then
     * the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until one of three things happens:
     * <ul>
     * <li>The lock is acquired by the current thread; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
     * current thread, and interruption of lock acquisition is supported; or
     * <li>The specified waiting time elapses
     * </ul>
     *
     * <p>If the lock is acquired then the value {@code true} is returned.
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while acquiring
     * the lock, and interruption of lock acquisition is supported,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then the value {@code false}
     * is returned.
     * If the time is
     * less than or equal to zero, the method will not wait at all.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>The ability to interrupt a lock acquisition in some implementations
     * may not be possible, and if possible may
     * be an expensive operation.
     * The programmer should be aware that this may be the case. An
     * implementation should document when this is the case.
     *
     * <p>An implementation can favor responding to an interrupt over normal
     * method return, or reporting a timeout.
     *
     * <p>A {@code Lock} implementation may be able to detect
     * erroneous use of the lock, such as an invocation that would cause
     * deadlock, and may throw an (unchecked) exception in such circumstances.
     * The circumstances and the exception type must be documented by that
     * {@code Lock} implementation.
     *
     * @param time the maximum time to wait for the lock
     * @param unit the time unit of the {@code time} argument
     * @return {@code true} if the lock was acquired and {@code false}
     * if the waiting time elapsed before the lock was acquired
     * @throws InterruptedException if the current thread is interrupted
     *                              while acquiring the lock (and interruption of lock
     *                              acquisition is supported)
     */
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException
    {
        throw new IllegalStateException(
                "方法 'tryLock' 尚未实现!");
    }


    /**
     * Returns a new {@link Condition} instance that is bound to this
     * {@code Lock} instance.
     *
     * <p>Before waiting on the condition the lock must be held by the
     * current thread.
     * A call to {@link Condition#await()} will atomically release the lock
     * before waiting and re-acquire the lock before the wait returns.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>The exact operation of the {@link Condition} instance depends on
     * the {@code Lock} implementation and must be documented by that
     * implementation.
     *
     * @return A new {@link Condition} instance for this {@code Lock} instance
     * @throws UnsupportedOperationException if this {@code Lock}
     *                                       implementation does not support conditions
     */
    @Override
    public Condition newCondition()
    {
        throw new IllegalStateException(
                "方法 'newCondition' 尚未实现!");
    }
}